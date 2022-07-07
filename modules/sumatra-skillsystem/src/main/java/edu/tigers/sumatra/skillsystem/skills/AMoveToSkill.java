/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.UninitializedID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.pathfinder.IPathFinderResult;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.pathfinder.PathFinderInput;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.pathfinder.obstacles.PenaltyAreaObstacle;
import edu.tigers.sumatra.pathfinder.traj.TrajPathFinder;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.trajectory.TrajectoryWrapper;
import edu.tigers.sumatra.trajectory.TrajectoryXyw;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Log4j2
public abstract class AMoveToSkill extends AMoveSkill
{
	@Configurable(defValue = "255")
	private static AObjectID debugForId = new UninitializedID();

	@Configurable(defValue = "0.5", comment = "A bot velocity [m/s] that is considered save for collisions")
	private static double safeVelForEmergencyBrake = 0.5;

	@Configurable(defValue = "0.1", comment = "A bot velocity [m/s] that is considered save for collisions")
	private static double safeVelNormalBrake = 0.1;

	@Configurable(defValue = "false", comment = "Adapt the brake acceleration dynamically to avoid overshooting (does not work well with real robot control)")
	private static boolean adaptBrakeAcc = false;

	@Configurable(defValue = "0.1", comment = "Time margin [s] to add before and after potential robot crossing point for emergency brake detection")
	private static double tUncertainMargin = 0.1;

	@Configurable(defValue = "0.005", comment = "Deadline [s] to wait for the path planning result, before giving up for this frame")
	private static double pathPlanningDeadline = 0.005;

	@Getter
	private final MovementCon moveCon = new MovementCon();
	private final ObstacleGenerator obstacleGen = new ObstacleGenerator(moveCon);
	private final TrajPathFinder finder = new TrajPathFinder();

	@Getter
	private MoveConstraints moveConstraints = new MoveConstraints();

	private IVector2 destination = null;
	private Double targetAngle = null;
	private DynamicPosition lookAtTarget = null;
	private Future<IPathFinderResult> outstandingResult;
	private IPathFinderResult lastPathResult;
	private boolean brakeActive;

	@Getter
	@Setter
	private TrajectoryWithTime<IVector3> currentTrajectory;

	@Getter
	private double destinationReachedIn;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		moveConstraints.resetLimits(getBot().getBotParams().getMovementLimits());
	}


	@Override
	public void doUpdate()
	{
		updateFields();

		MoveConstraints currentMoveConstraints = new MoveConstraints(moveConstraints)
				.limit(getBot().getBotParams().getMovementLimits());

		adaptBrakeAcc(currentMoveConstraints);
		limitSpeedOnStoppedGame(currentMoveConstraints);

		List<IObstacle> obstacles = obstacleGen.generateObstacles(getWorldFrame(), getBot().getBotId(), getGameState());
		drawObstacles(obstacles);

		if (criticalCollisionAhead())
		{
			performEmergencyBrake(currentMoveConstraints);
			return;
		}

		Future<IPathFinderResult> pathResultFuture = outstandingResult != null
				? outstandingResult
				: findNewPath(currentMoveConstraints, obstacles);
		IPathFinderResult pathResult = retrievePathFinderResult(pathResultFuture);
		if (pathResult == null)
		{
			outstandingResult = pathResultFuture;
			if (lastPathResult != null)
			{
				drawPath(lastPathResult);
				executePath(lastPathResult, currentMoveConstraints);
			}

			getShapes().get(ESkillShapesLayer.PATH_DEBUG).add(
					new DrawableCircle(Circle.createCircle(getTBot().getPos(), 100))
							.setColor(new Color(227, 147, 39))
							.setStrokeWidth(30)
			);

			return;
		}

		lastPathResult = pathResult;
		outstandingResult = null;
		destinationReachedIn = pathResult.getTrajectory().getTotalTime();
		getShapes().get(ESkillShapesLayer.PATH_FINDER_DEBUG).addAll(finder.getDebugShapes());
		drawPath(pathResult);

		if (needToBrake(pathResult))
		{
			boolean emergencyBrake = pathResult.getCollider().filter(IObstacle::isEmergencyBrakeFor).isPresent();
			if (emergencyBrake)
			{
				performEmergencyBrake(currentMoveConstraints);
			} else
			{
				performNormalBrake(currentMoveConstraints);
			}
		} else
		{
			executePath(pathResult, currentMoveConstraints);
		}
	}


	private boolean needToBrake(IPathFinderResult pathResult)
	{
		if (pathResult.getFirstCollisionTime() <= brakeTime())
		{
			return true;
		}
		boolean brakeInside = pathResult.getCollider().filter(IObstacle::isBrakeInside).isPresent();
		return brakeInside && pathResult.hasFrontCollision() && getVel().getLength2() > safeVelForEmergencyBrake;
	}


	private void performEmergencyBrake(MoveConstraints currentMoveConstraints)
	{
		getShapes().get(ESkillShapesLayer.PATH).add(
				new DrawableCircle(Circle.createCircle(getTBot().getPos(), 100))
						.setColor(Color.pink)
						.setStrokeWidth(30)
		);

		// brake fast to avoid collision
		currentMoveConstraints.setAccMax(currentMoveConstraints.getBrkMax());
		currentMoveConstraints.setAccMaxW(DriveLimits.MAX_ACC_W);
		currentMoveConstraints.setJerkMax(DriveLimits.MAX_JERK);
		currentMoveConstraints.setJerkMaxW(DriveLimits.MAX_JERK_W);

		performBrake(currentMoveConstraints);
	}


	private void performNormalBrake(MoveConstraints currentMoveConstraints)
	{
		getShapes().get(ESkillShapesLayer.PATH).add(
				new DrawableCircle(Circle.createCircle(getTBot().getPos(), 100))
						.setColor(Color.magenta)
						.setStrokeWidth(30)
		);

		performBrake(currentMoveConstraints);
	}


	private void performBrake(MoveConstraints currentMoveConstraints)
	{
		currentTrajectory = null;
		setLocalVelocity(Vector2.zero(), 0, currentMoveConstraints);
		brakeActive = true;
	}


	private Future<IPathFinderResult> findNewPath(MoveConstraints currentMoveConstraints, List<IObstacle> obstacles)
	{
		long timestamp = getWorldFrame().getTimestamp();
		PathFinderInput finderInput = currentTrajectory == null
				? PathFinderInput.fromBot(timestamp, getTBot().getCurrentState())
				: PathFinderInput.fromTrajectory(timestamp, currentTrajectory.synchronizeTo(timestamp));
		finderInput.setMoveConstraints(currentMoveConstraints);
		finderInput.setObstacles(obstacles);

		IVector2 adaptedDestination = getAdaptedDestination(obstacles);

		finderInput.setDest(adaptedDestination);
		finderInput.setTargetAngle(targetAngle);
		finderInput.setDebug(debugForId.equals(getBot().getBotId()));

		return finder.calcPath(finderInput);
	}


	private IVector2 getAdaptedDestination(List<IObstacle> obstacles)
	{
		return obstacles.stream()
				.filter(PenaltyAreaObstacle.class::isInstance)
				.map(PenaltyAreaObstacle.class::cast)
				.filter(o -> o.isPointCollidingWithObstacle(getPos(), 0, 0))
				.findAny()
				.map(o -> o.getPenaltyArea().withMargin(100).nearestPointOutside(getPos()))
				.orElse(destination);
	}


	private IPathFinderResult retrievePathFinderResult(Future<IPathFinderResult> pathResultFuture)
	{
		try
		{
			// In internal simulation mode, we do not want to be dependent on system resources
			long timeout = (long) (pathPlanningDeadline * 1000);
			return pathResultFuture.get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		} catch (ExecutionException e)
		{
			throw new IllegalStateException("Failed to calculate path", e);
		} catch (@SuppressWarnings("squid:S1166") TimeoutException e)
		{
			// ignore timeout, it is expected to happen more or less often
		}
		return null;
	}


	@Override
	protected void doExitActions()
	{
		super.doExitActions();
		currentTrajectory = null;
	}


	private void drawObstacles(final List<IObstacle> obstacles)
	{
		if (debugForId.isUninitializedID() || debugForId.equals(getBot().getBotId()))
		{
			final List<IDrawableShape> shapes = obstacles.stream()
					.flatMap(o -> o.getShapes().stream())
					.toList();
			getShapes().get(ESkillShapesLayer.TRAJ_PATH_OBSTACLES).addAll(shapes);
		}
	}


	private void updateFields()
	{
		moveCon.update(getTBot());
		if (destination == null)
		{
			destination = getTBot().getPos();
		}

		if (lookAtTarget != null)
		{
			lookAtTarget = lookAtTarget.update(getWorldFrame());
			targetAngle = lookAtTarget.getPos().subtractNew(getPos()).getAngle(0);
		} else if (targetAngle == null)
		{
			targetAngle = getTBot().getOrientation();
		}
	}


	private void adaptBrakeAcc(final MoveConstraints currentMoveConstraints)
	{
		if (!adaptBrakeAcc)
		{
			return;
		}
		double requiredBrakeAcc = requiredBrakeAcc();
		final Double requiredBrakeAngle = requiredBrakeAngle();
		if (requiredBrakeAcc > moveConstraints.getAccMaxDerived()
				&& requiredBrakeAcc < currentMoveConstraints.getBrkMax()
				&& requiredBrakeAngle < AngleMath.PI_QUART)
		{
			currentMoveConstraints.setAccMax(requiredBrakeAcc);
			currentMoveConstraints.setAccMaxFast(requiredBrakeAcc);
		} else
		{
			currentMoveConstraints.setAccMax(moveConstraints.getAccMax());
			currentMoveConstraints.setAccMaxFast(moveConstraints.getAccMaxFast());
		}
		getShapes().get(ESkillShapesLayer.DEBUG)
				.add(new DrawableAnnotation(getTBot().getPos(),
						String.format("brk:%.1fm/s²@%.1frad", requiredBrakeAcc, requiredBrakeAngle),
						Vector2.fromY(120)));
	}


	private Double requiredBrakeAngle()
	{
		final Vector2 moveDir = destination.subtractNew(getTBot().getPos());
		final IVector2 vel = getTBot().getVel();
		return moveDir.angleToAbs(vel).orElse(0.0);
	}


	private double requiredBrakeAcc()
	{
		double v = getTBot().getVel().getLength2();
		double d = getTBot().getPos().distanceTo(destination) / 1000.0;
		return v * v / d / 2;
	}


	private boolean criticalCollisionAhead()
	{
		double currentVel = getTBot().getVel().getLength2();
		if (currentVel < safeVelForEmergencyBrake)
		{
			return false;
		}
		double velDiff = currentVel - safeVelForEmergencyBrake;
		double brakeTime = velDiff / getTBot().getMoveConstraints().getAccMaxDerived();
		double brakeDist = currentVel * brakeTime / 2 * 1000;

		return criticalCollisionAhead(brakeTime, brakeDist);
	}


	private double brakeTime()
	{
		double currentVel = getTBot().getVel().getLength2();
		if (currentVel < safeVelNormalBrake)
		{
			return 0;
		}
		double velDiff = currentVel - safeVelNormalBrake;
		return velDiff / getTBot().getMoveConstraints().getAccMaxDerived();
	}


	private boolean criticalCollisionAhead(final double brakeTime, final double brakeDist)
	{
		ILineSegment ownBrakeLine = Lines.segmentFromOffset(getTBot().getPos(), getTBot().getVel().scaleToNew(brakeDist));
		getShapes().get(ESkillShapesLayer.CRITICAL_COLLISION).add(
				new DrawableLine(ownBrakeLine)
						.setColor(Color.gray)
						.setStrokeWidth(20)
		);

		var stepSize = brakeActive ? 0.01 : 0.05;
		var horizon = brakeTime + (brakeActive ? 0.2 : 0.0);
		for (ITrackedBot opponentBot : getWorldFrame().getOpponentBots().values())
		{
			var oppFurthestBrakePoint = opponentBot.getPos().addNew(opponentBot.getVel().multiplyNew(horizon * 1000));
			ILineSegment oppBrakeLine = Lines.segmentFromPoints(opponentBot.getPos(), oppFurthestBrakePoint);
			if (oppBrakeLine.distanceTo(ownBrakeLine) > Geometry.getBotRadius() * 2)
			{
				continue;
			}
			getShapes().get(ESkillShapesLayer.CRITICAL_COLLISION).add(
					new DrawableLine(oppBrakeLine)
							.setColor(Color.red)
							.setStrokeWidth(20)
			);

			for (double t = 0; t <= horizon; t += stepSize)
			{
				var ownPos = getTBot().getPosByTime(t);
				var tEarly = Math.max(0, t - tUncertainMargin);
				var tLate = t + (brakeActive ? tUncertainMargin * 2 : tUncertainMargin);
				var oppPosEarly = opponentBot.getPosByTime(tEarly);
				var oppPosLate = opponentBot.getPosByTime(tLate);
				var oppLine = Lines.segmentFromPoints(oppPosEarly, oppPosLate);
				var colliding = oppLine.distanceTo(ownPos) < Geometry.getBotRadius() * 2;
				if (colliding)
				{
					IVector2 closestPointToOwnPos = oppLine.closestPointOnLine(ownPos);
					ILineSegment ownLine = Lines.segmentFromPoints(ownPos, closestPointToOwnPos);
					getShapes().get(ESkillShapesLayer.CRITICAL_COLLISION).add(
							new DrawableLine(oppLine)
									.setColor(Color.cyan));
					getShapes().get(ESkillShapesLayer.CRITICAL_COLLISION).add(
							new DrawableLine(ownLine)
									.setColor(Color.magenta));
					getShapes().get(ESkillShapesLayer.CRITICAL_COLLISION).add(
							new DrawableCircle(Circle.createCircle(closestPointToOwnPos, Geometry.getBotRadius()))
									.setColor(Color.cyan));
					getShapes().get(ESkillShapesLayer.CRITICAL_COLLISION).add(
							new DrawableCircle(Circle.createCircle(ownPos, Geometry.getBotRadius()))
									.setColor(Color.magenta));
					return true;
				}
			}
		}

		return false;
	}


	private void limitSpeedOnStoppedGame(final MoveConstraints currentMoveConstraints)
	{
		if (getGameState().isVelocityLimited())
		{
			currentMoveConstraints.setVelMax(Math.min(moveConstraints.getVelMax(), getMaxStopSpeed()));
			currentMoveConstraints
					.setVelMaxFast(Math.min(moveConstraints.getVelMaxFast(), getMaxStopSpeed()));
		}
	}


	private void executePath(
			final IPathFinderResult pathFinderResult,
			final IMoveConstraints currentMoveConstraints
	)
	{
		var trajectory = pathFinderResult.getTrajectory().relocate(currentMoveConstraints, getPos(), getVel());

		var dest = trajectory.getNextDestination(0);
		setTargetPose(dest, targetAngle, currentMoveConstraints);

		var trajW = TrajectoryGenerator.generateRotationTrajectory(getTBot(), targetAngle, currentMoveConstraints);
		var trajectoryXyw = new TrajectoryXyw(trajectory, trajW);
		currentTrajectory = new TrajectoryWithTime<>(trajectoryXyw, getWorldFrame().getTimestamp());
		brakeActive = false;
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

		shapes.add(new DrawableBot(destination, targetAngle,
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
				.add(new DrawableAnnotation(destination,
						String.format("tt: %.2f", pathResult.getTrajectory().getTotalTime())));
	}


	/**
	 * @param destination to set
	 */
	protected void updateDestination(final IVector2 destination)
	{
		this.destination = destination;
	}


	/**
	 * @param angle [rad]
	 */
	protected void updateTargetAngle(final double angle)
	{
		targetAngle = angle;
		lookAtTarget = null;
	}


	/**
	 * Updates the angle the bot should look at.
	 *
	 * @param lookAtTarget to set
	 */
	protected void updateLookAtTarget(final DynamicPosition lookAtTarget)
	{
		this.lookAtTarget = lookAtTarget;
	}


	/**
	 * @param object to set
	 */
	protected void updateLookAtTarget(final ITrackedObject object)
	{
		updateLookAtTarget(new DynamicPosition(object));
	}


	/**
	 * Updates the angle the bot should look at.
	 *
	 * @param lookAtTarget to set
	 */
	protected void updateLookAtTarget(final IVector2 lookAtTarget)
	{
		updateLookAtTarget(new DynamicPosition(lookAtTarget));
	}


	protected IVector2 getDestination()
	{
		return destination;
	}


	protected Double getTargetAngle()
	{
		return targetAngle;
	}


	@Override
	public void setPrioMap(final PathFinderPrioMap prioMap)
	{
		moveCon.setPrioMap(prioMap);
	}


	@Override
	public void setExecutorService(ExecutorService executorService)
	{
		// In internal simulation mode, we do not want to be dependent on system resources
		if (!SumatraModel.getInstance().isSimulation())
		{
			finder.setExecutorService(executorService);
		}
	}
}
