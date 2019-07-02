/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.botskills.AMoveBotSkill;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.UninitializedID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.IPathFinderResult;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.pathfinder.PathFinderInput;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.pathfinder.traj.TrajPathFinder;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWrapper;
import edu.tigers.sumatra.trajectory.TrajectoryXyw;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Shared skill state that handles path planning for a skill.
 */
public class MoveToState extends AState
{
	@Configurable(defValue = "255")
	private static AObjectID debugForId = new UninitializedID();

	@Configurable(defValue = "0.5", comment = "A bot velocity [m/s] that is considered save for collisions")
	private static double saveVel = 0.5;

	@Configurable(defValue = "true", comment = "Adapt the brake acceleration dynamically to avoid overshooting")
	private static boolean adaptBrakeAcc = true;

	static
	{
		ConfigRegistration.registerClass("skills", MoveToState.class);
	}


	private final AMoveSkill moveSkill;
	private TrajPathFinder finder = new TrajPathFinder();
	private ObstacleGenerator obstacleGen = new ObstacleGenerator();

	private double targetAngle;


	protected MoveToState(final AMoveSkill moveSkill)
	{
		this.moveSkill = moveSkill;
	}


	@Override
	public void doEntryActions()
	{
		targetAngle = getTBot().getOrientation();
	}


	@Override
	public void doUpdate()
	{
		obstacleGen.setUseBall(getMoveCon().isBallObstacle());
		obstacleGen.setUseTheirBots(getMoveCon().isTheirBotsObstacle());
		obstacleGen.setUseOurBots(getMoveCon().isOurBotsObstacle());
		obstacleGen.setUsePenAreaOur(getMoveCon().isPenaltyAreaForbiddenOur());
		obstacleGen.setUsePenAreaTheir(getMoveCon().isPenaltyAreaForbiddenTheir());
		obstacleGen.setUseGoalPosts(getMoveCon().isGoalPostObstacle());
		obstacleGen.setUseField(!getMoveCon().isDestinationOutsideFieldAllowed());
		obstacleGen.setIgnoredBots(getMoveCon().getIgnoredBots());
		obstacleGen.setCriticalFoeBots(getMoveCon().getCriticalFoeBots());
		getMoveCon().getMinDistToBall().ifPresent(obstacleGen::setSecDistBall);

		List<IObstacle> obstacles = obstacleGen.generateObstacles(getWorldFrame(), getBot().getBotId(),
				getMoveCon().getPrioMap(), getGameState());
		obstacles.addAll(getMoveCon().getCustomObstacles());
		if (!getMoveCon().isIgnoreGameStateObstacles())
		{
			obstacles.addAll(obstacleGen.generateGameStateObstacles(getWorldFrame(), getGameState()));
		}
		if (debugForId.isUninitializedID() || debugForId.equals(getBot().getBotId()))
		{
			final List<IDrawableShape> shapes = obstacles.stream()
					.flatMap(o -> o.getShapes().stream())
					.collect(Collectors.toList());
			getShapes().get(ESkillShapesLayer.TRAJ_PATH_OBSTACLES).addAll(shapes);
		}

		targetAngle = getMoveCon().getTargetAngle();

		PathFinderInput finderInput = PathFinderInput.fromBotOrTrajectory(getWorldFrame().getTimestamp(),
				getTBot());
		finderInput.setMoveConstraints(new MoveConstraints(getMoveCon().getMoveConstraints()));
		finderInput.setObstacles(obstacles);
		finderInput.setDest(getMoveCon().getDestination());
		finderInput.setTargetAngle(targetAngle);
		finderInput.setDebug(debugForId.equals(getBot().getBotId()));

		adaptBrakeAcc(finderInput.getMoveConstraints());

		limitSpeedOnStoppedGame(finderInput.getMoveConstraints());

		IPathFinderResult pathResult = finder.calcPath(finderInput);

		getShapes().get(ESkillShapesLayer.PATH_FINDER_DEBUG).addAll(finder.getDebugShapes());

		Optional<IObstacle> collider = pathResult.getCollider();

		if (getTBot().getVel().getLength2() > saveVel)
		{
			final boolean criticalCollisionAhead = criticalCollisionAhead();
			if (collider.isPresent() || criticalCollisionAhead)
			{
				boolean criticalCollision = collider.map(IObstacle::isCritical).orElse(false) || criticalCollisionAhead;
				Color color = criticalCollision ? Color.pink : Color.magenta;

				// brake fast to avoid collision
				DrawableCircle dCircle = new DrawableCircle(Circle.createCircle(getTBot().getPos(), 100), color);
				dCircle.setStrokeWidth(30);
				getShapes().get(ESkillShapesLayer.PATH).add(dCircle);

				MoveConstraints emergencyMoveConstraints = new MoveConstraints(finderInput.getMoveConstraints());
				if (criticalCollision)
				{
					emergencyMoveConstraints.setAccMax(emergencyMoveConstraints.getBrkMax());
					emergencyMoveConstraints.setAccMaxW(DriveLimits.MAX_ACC_W);
					emergencyMoveConstraints.setJerkMax(DriveLimits.MAX_JERK);
					emergencyMoveConstraints.setJerkMaxW(DriveLimits.MAX_JERK_W);
				}

				moveSkill.setLocalVelocity(Vector2.zero(), 0, emergencyMoveConstraints);
				getBot().setCurrentTrajectory(null);
			} else
			{
				executePath(pathResult.getTrajectory(), finderInput.getMoveConstraints());
			}
		} else
		{
			executePath(pathResult.getTrajectory(), finderInput.getMoveConstraints());
		}
		drawPath(pathResult);
	}


	private void adaptBrakeAcc(final MoveConstraints moveConstraints)
	{
		if (!adaptBrakeAcc)
		{
			return;
		}
		double requiredBrakeAcc = requiredBrakeAcc();
		final Double requiredBrakeAngle = requiredBrakeAngle();
		if (requiredBrakeAcc > moveConstraints.getAccMax()
				&& requiredBrakeAcc < moveConstraints.getBrkMax()
				&& requiredBrakeAngle < AngleMath.PI_QUART)
		{
			moveConstraints.setAccMax(requiredBrakeAcc);
		}
		getShapes().get(ESkillShapesLayer.DEBUG)
				.add(new DrawableAnnotation(getTBot().getPos(),
						String.format("brk:%.1fm/s²@%.1frad", requiredBrakeAcc, requiredBrakeAngle),
						Vector2.fromY(120)));
	}


	private Double requiredBrakeAngle()
	{
		final Vector2 moveDir = getMoveCon().getDestination().subtractNew(getTBot().getPos());
		final IVector2 vel = getTBot().getVel();
		return moveDir.angleToAbs(vel).orElse(0.0);
	}


	private double requiredBrakeAcc()
	{
		double v = getTBot().getVel().getLength2();
		double d = getTBot().getPos().distanceTo(getMoveCon().getDestination()) / 1000.0;
		return v * v / d / 2;
	}


	private boolean criticalCollisionAhead()
	{
		double currentVel = getTBot().getVel().getLength2();
		if (currentVel < saveVel)
		{
			return false;
		}
		double velDiff = currentVel - saveVel;
		double brakeTime = velDiff / getTBot().getMoveConstraints().getAccMax();
		double brakeDist = velDiff * brakeTime / 2;

		return criticalCollisionAhead(brakeTime, brakeDist);
	}


	private boolean criticalCollisionAhead(final double brakeTime, final double brakeDist)
	{
		final double hittingLength = brakeDist * 1000 + Geometry.getBotRadius();
		final Vector2 hittingOffset = getTBot().getVel().scaleToNew(hittingLength);
		final IVector2 stopPos = getTBot().getPos().addNew(hittingOffset);
		final ITube ownHittingTube = Tube.create(getTBot().getPos(), stopPos, Geometry.getBotRadius());

		getShapes().get(ESkillShapesLayer.PATH_DEBUG).add(new DrawableTube(ownHittingTube, Color.blue));

		for (ITrackedBot opponentBot : getWorldFrame().getFoeBots().values())
		{
			final IVector2 opponentFuturePos;
			final IVector2 opponentDir;
			if (opponentBot.getVel().getLength2() > 0.1)
			{
				opponentFuturePos = opponentBot.getPosByTime(brakeTime);
				opponentDir = opponentFuturePos.subtractNew(opponentBot.getPos());
			} else
			{
				opponentFuturePos = opponentBot.getPos();
				opponentDir = ownHittingTube.nearestPointOutside(opponentBot.getPos()).subtractNew(opponentBot.getPos());
			}

			final double margin = Geometry.getBotRadius();
			final IVector2 opponentHittingOffset = opponentDir.scaleToNew(margin);
			final Vector2 opponentStart = opponentBot.getPos().subtractNew(opponentHittingOffset);
			final Vector2 opponentEnd = opponentFuturePos.addNew(opponentHittingOffset);
			final ILineSegment opponentHittingLine = Lines.segmentFromPoints(opponentStart, opponentEnd);

			getShapes().get(ESkillShapesLayer.PATH_DEBUG).add(new DrawableLine(opponentHittingLine, Color.magenta));

			final List<IVector2> intersections = ownHittingTube.lineIntersections(opponentHittingLine);
			if (!intersections.isEmpty())
			{
				IVector2 intersection = getTBot().getPos().nearestTo(intersections);
				double ownDistToIntersection = getTBot().getPos().distanceTo(intersection) / 1000.0;
				double ownTimeToIntersection = ownDistToIntersection / getTBot().getVel().getLength2() / 2;
				double opponentDistToIntersection = opponentBot.getPos().distanceTo(intersection) / 1000.0;
				double opponentTimeToIntersection = opponentDistToIntersection / opponentBot.getVel().getLength2() / 2;
				double timeDiff = Math.abs(ownTimeToIntersection - opponentTimeToIntersection);
				getShapes().get(ESkillShapesLayer.PATH_DEBUG)
						.add(new DrawableAnnotation(intersection, String.format("%.1f", timeDiff)));
				if (timeDiff < 0.5)
				{
					return true;
				}
			}
		}

		return false;
	}


	private void limitSpeedOnStoppedGame(MoveConstraints moveConstraints)
	{
		if (getGameState().isVelocityLimited())
		{
			moveConstraints.setVelMax(Math.min(moveConstraints.getVelMax(),
					RuleConstraints.getStopSpeed() - AMoveToSkill.getStopSpeedTolerance()));
			moveConstraints.setVelMaxFast(Math.min(moveConstraints.getVelMax(),
					RuleConstraints.getStopSpeed() - AMoveToSkill.getStopSpeedTolerance()));
		}
	}


	private void executePath(final ITrajectory<IVector2> trajectory, final MoveConstraints moveConstraints)
	{
		ITrajectory<IVector3> traj = new TrajectoryXyw(trajectory,
				TrajectoryGenerator.generateRotationTrajectoryStub(getMoveCon().getTargetAngle()));

		IVector2 dest = trajectory.getNextDestination(0);
		setTargetPose(dest, targetAngle, moveConstraints, traj);
	}


	private void drawPath(final IPathFinderResult pathResult)
	{
		List<IDrawableShape> shapes = new ArrayList<>(3);
		double t = 0;
		if (pathResult.getCollisionDurationFront() > 0)
		{
			TrajectoryWrapper<IVector2> trajWrapperLower = new TrajectoryWrapper<>(pathResult.getTrajectory(), 0,
					Math.min(pathResult.getCollisionDurationFront(), pathResult.getTrajectory().getTotalTime()));
			shapes.add(new DrawableTrajectoryPath(trajWrapperLower, Color.red));
			t = pathResult.getCollisionDurationFront();
		}
		double tBeforeCollision = Math.min(pathResult.getCollisionLookahead(),
				pathResult.getTrajectory().getTotalTime() - pathResult.getCollisionDurationBack());
		if (t < tBeforeCollision)
		{
			TrajectoryWrapper<IVector2> trajWrapperLower = new TrajectoryWrapper<>(pathResult.getTrajectory(), t,
					tBeforeCollision);
			shapes.add(new DrawableTrajectoryPath(trajWrapperLower, Color.green));
			t = tBeforeCollision;
		}

		if (t < Math.min(pathResult.getTrajectory().getTotalTime(), pathResult.getCollisionLookahead()))
		{
			TrajectoryWrapper<IVector2> trajWrapperLower = new TrajectoryWrapper<>(pathResult.getTrajectory(), t,
					pathResult.getCollisionLookahead());
			shapes.add(new DrawableTrajectoryPath(trajWrapperLower, Color.red));
			t = pathResult.getCollisionLookahead();
		}

		if (t < pathResult.getTrajectory().getTotalTime())
		{
			TrajectoryWrapper<IVector2> trajWrapperLower = new TrajectoryWrapper<>(pathResult.getTrajectory(), t,
					pathResult.getTrajectory().getTotalTime());
			shapes.add(new DrawableTrajectoryPath(trajWrapperLower, Color.orange));
		}

		shapes.add(new DrawableBot(getMoveCon().getDestination(), getMoveCon().getTargetAngle(),
				getTBot().getBotId().getTeamColor().getColor(),
				Geometry.getBotRadius() + 25,
				Geometry.getBotRadius() + 25));

		if (pathResult.hasIntermediateCollision())
		{
			IVector2 collisionPos = pathResult.getTrajectory().getPositionMM(pathResult.getFirstCollisionTime());
			DrawableCircle dCircleCollision = new DrawableCircle(Circle.createCircle(collisionPos, 10), Color.RED);
			shapes.add(dCircleCollision);
		}

		getShapes().get(ESkillShapesLayer.PATH).addAll(shapes);
		getShapes().get(ESkillShapesLayer.PATH_DEBUG)
				.add(new DrawableAnnotation(getMoveCon().getDestination(),
						String.format("tt: %.2f", pathResult.getTrajectory().getTotalTime())));
	}


	protected ITrackedBot getTBot()
	{
		return moveSkill.getTBot();
	}


	protected GameState getGameState()
	{
		return moveSkill.getGameState();
	}


	protected ABot getBot()
	{
		return moveSkill.getBot();
	}


	protected WorldFrame getWorldFrame()
	{
		return moveSkill.getWorldFrame();
	}


	protected MovementCon getMoveCon()
	{
		return moveSkill.getMoveCon();
	}


	protected AMoveBotSkill setTargetPose(final IVector2 dest, final double targetAngle,
			final MoveConstraints moveConstraints, final ITrajectory<IVector3> trajectory)
	{
		return moveSkill.setTargetPose(dest, targetAngle, moveConstraints, trajectory);
	}


	protected ShapeMap getShapes()
	{
		return moveSkill.getShapes();
	}
}
