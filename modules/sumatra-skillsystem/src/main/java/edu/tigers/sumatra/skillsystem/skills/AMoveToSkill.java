/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableBot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePlanarCurve;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableTrajectoryArea;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.EPathFinderShapesLayer;
import edu.tigers.sumatra.pathfinder.IPathFinder;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.pathfinder.finder.PathFinder;
import edu.tigers.sumatra.pathfinder.finder.PathFinderInput;
import edu.tigers.sumatra.pathfinder.finder.PathFinderInputProcessor;
import edu.tigers.sumatra.pathfinder.finder.PathFinderResult;
import edu.tigers.sumatra.pathfinder.finder.TrajPath;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.pathfinder.obstacles.input.DynamicMargin;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.DoubleChargingValue;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.trajectory.TrajectoryXyw;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;


@Log4j2
public abstract class AMoveToSkill extends AMoveSkill
{
	private static final DecimalFormat DF_2 = new DecimalFormat("#.##");

	@Configurable(defValue = "false")
	private static boolean debugShapes = false;

	@Configurable(defValue = "0.05", comment = "Time offset [s] to add to brake time to compensate reaction time")
	private static double brakeReactionTime = 0.05;

	@Configurable(defValue = "0.6", comment = "Velocity [m/s] to subtract from current velocity when calculating brake time")
	private static double brakeVelTolerance = 0.6;

	@Configurable(defValue = "0.5", comment = "Minimum velocity [m/s] to limit the max velocity to when no good path is found")
	private static double maxRobotSpeedLimiterMinVel = 0.5;

	@Configurable(defValue = "5", comment = "Rate [m/s²] that the limited velocity is increased")
	private static double maxRobotSpeedLimiterIncreaseRate = 5;

	@Configurable(defValue = "-5", comment = "Rate [m/s²] that the limited velocity is decreased")
	private static double maxRobotSpeedLimiterDecreaseRate = -5;


	@Getter
	private final MovementCon moveCon = new MovementCon();
	private final ObstacleGenerator obstacleGen = new ObstacleGenerator(moveCon);
	private final PathFinderInputProcessor inputProcessor = new PathFinderInputProcessor();
	private final IPathFinder finder = new PathFinder();
	private DoubleChargingValue maxRobotSpeedLimiter;

	@Getter
	private MoveConstraints moveConstraints = new MoveConstraints();

	@Getter(AccessLevel.PROTECTED)
	private IVector2 destination;
	@Getter(AccessLevel.PROTECTED)
	private Double targetAngle;
	private DynamicPosition lookAtTarget;

	@Getter
	@Setter
	private TrajectoryWithTime<IVector3> currentTrajectory;

	@Getter
	private double destinationReachedIn;

	@Setter
	private boolean comeToAStop = false;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		moveConstraints.resetLimits(getBot().getBotParams().getMovementLimits());
		double maxVel = getBot().getBotParams().getMovementLimits().getVelMaxFast();
		maxRobotSpeedLimiter = new DoubleChargingValue(
				maxVel,
				maxRobotSpeedLimiterIncreaseRate,
				maxRobotSpeedLimiterDecreaseRate,
				maxRobotSpeedLimiterMinVel,
				maxVel
		);
	}


	@Override
	protected void doExitActions()
	{
		super.doExitActions();
		currentTrajectory = null;
	}


	@Override
	public void doUpdate()
	{
		updateFields();

		MoveConstraints currentMoveConstraints = limitRobotSpeed(
				new MoveConstraints(moveConstraints).limit(getBot().getBotParams().getMovementLimits())
		);

		List<IObstacle> obstacles = obstacleGen.generateObstacles(getWorldFrame(), getBot().getBotId(), getGameState());

		PathFinderInput pathFinderInput = getPathFinderInput(currentMoveConstraints, obstacles);
		Optional<PathFinderResult> pathResult = finder.calcPath(pathFinderInput);
		pathResult.ifPresent(this::drawPathFinderResult);

		if (comeToAStop)
		{
			performBrake(currentMoveConstraints);
			destinationReachedIn = Double.POSITIVE_INFINITY;
		} else if (pathResult.isEmpty() || needToBrake(pathResult.get()))
		{
			maxRobotSpeedLimiter.setChargeMode(DoubleChargingValue.ChargeMode.DECREASE);
			performBrake(currentMoveConstraints);
			drawDirectPath(pathFinderInput, currentMoveConstraints);
			destinationReachedIn = Double.POSITIVE_INFINITY;
		} else
		{
			maxRobotSpeedLimiter.setChargeMode(DoubleChargingValue.ChargeMode.INCREASE);
			executePath(pathResult.get().getTrajectory(), currentMoveConstraints);
			destinationReachedIn = pathResult.get().getTrajectory().getTotalTime();
		}

		getShapes().get(ESkillShapesLayer.PATH_LIMITED_VEL).add(
				new DrawableAnnotation(getPos(), "limVel: " + DF_2.format(maxRobotSpeedLimiter.getValue()))
						.withOffsetX(150)
		);
		maxRobotSpeedLimiter.update(getWorldFrame().getTimestamp());
	}


	private void drawDirectPath(PathFinderInput pathFinderInput, MoveConstraints currentMoveConstraints)
	{
		getShapes().get(ESkillShapesLayer.PATH).add(
				new DrawablePlanarCurve(
						TrajectoryGenerator.generatePositionTrajectory(
								getTBot(),
								pathFinderInput.getDest(),
								currentMoveConstraints
						)
				).setColor(Color.magenta)
		);
	}


	private boolean needToBrake(PathFinderResult pathResult)
	{
		return pathResult.getFirstCollisionTime() <= brakeTime();
	}


	private double brakeTime()
	{
		double vel = Math.max(0, getTBot().getVel().getLength2() - brakeVelTolerance);
		return vel / getTBot().getMoveConstraints().getAccMaxDerived() + brakeReactionTime;
	}


	private void performBrake(MoveConstraints currentMoveConstraints)
	{
		getShapes().get(ESkillShapesLayer.PATH).add(
				new DrawableCircle(Circle.createCircle(getTBot().getPos(), 100))
						.setColor(Color.pink)
						.setStrokeWidth(30)
		);

		currentMoveConstraints.setAccMax(currentMoveConstraints.getBrkMax());
		currentMoveConstraints.setAccMaxW(DriveLimits.MAX_ACC_W);
		currentMoveConstraints.setJerkMax(DriveLimits.MAX_JERK);
		currentMoveConstraints.setJerkMaxW(DriveLimits.MAX_JERK_W);

		currentTrajectory = null;
		setLocalVelocity(Vector2.zero(), 0, currentMoveConstraints);
	}


	private PathFinderInput getPathFinderInput(MoveConstraints currentMoveConstraints, List<IObstacle> obstacles)
	{
		long timestamp = getWorldFrame().getTimestamp();
		PathFinderInput.PathFinderInputBuilder inputBuilder = PathFinderInput.fromBot(getTBot().getBotState());

		PathFinderInput input = inputBuilder
				.timestamp(timestamp)
				.moveConstraints(currentMoveConstraints)
				.obstacles(obstacles)
				.dest(destination)
				.build();

		inputProcessor.setShapeMap(debugShapes ? getShapes() : null);
		PathFinderInput adaptedInput = inputProcessor.processInput(input);
		drawObstacles(obstacles);

		Color color = getTBot().getBotId().getTeamColor().getColor();
		double radius = Geometry.getBotRadius() + 25;
		getShapes().get(ESkillShapesLayer.MOVE_TO_DEST).add(
				new DrawableBot(input.getDest(), targetAngle, color, radius, radius)
		);
		getShapes().get(ESkillShapesLayer.MOVE_TO_DEST).add(
				new DrawableCircle(Circle.createCircle(adaptedInput.getDest(), radius - 10)).setColor(color)
		);

		return adaptedInput;
	}


	private void drawObstacles(final List<IObstacle> obstacles)
	{
		if (debugShapes)
		{
			obstacles.forEach(
					obstacle -> getShapes().get(EPathFinderShapesLayer.obstacle(obstacle.getIdentifier()))
							.addAll(obstacle.getShapes()));
		} else
		{
			obstacles.forEach(
					obstacle -> getShapes().get(EPathFinderShapesLayer.ALL_OBSTACLES).addAll(obstacle.getShapes()));
		}
	}


	private MoveConstraints limitRobotSpeed(MoveConstraints currentMoveConstraints)
	{
		if (getGameState().isVelocityLimited())
		{
			return currentMoveConstraints
					.setVelMax(Math.min(moveConstraints.getVelMax(), getMaxStopSpeed()))
					.setVelMaxFast(Math.min(moveConstraints.getVelMaxFast(), getMaxStopSpeed()));
		}
		return currentMoveConstraints
				.setVelMax(Math.min(moveConstraints.getVelMax(), maxRobotSpeedLimiter.getValue()))
				.setVelMaxFast(Math.min(moveConstraints.getVelMaxFast(), maxRobotSpeedLimiter.getValue()));
	}


	private void executePath(
			TrajPath trajPath,
			IMoveConstraints currentMoveConstraints
	)
	{
		var dest = trajPath.getNextDestination(0);
		setTargetPose(dest, targetAngle, currentMoveConstraints);

		var trajW = TrajectoryGenerator.generateRotationTrajectory(getTBot(), targetAngle, currentMoveConstraints);
		var trajectoryXyw = new TrajectoryXyw(trajPath, trajW);
		currentTrajectory = new TrajectoryWithTime<>(trajectoryXyw, getWorldFrame().getTimestamp());

		getShapes().get(ESkillShapesLayer.PATH).add(
				new DrawablePlanarCurve(trajPath).setColor(Color.green)
		);

		getShapes().get(ESkillShapesLayer.PATH_DEBUG).add(
				new DrawableAnnotation(destination, "tt: " + DF_2.format(trajPath.getTotalTime()))
		);
		if (debugShapes)
		{
			getShapes().get(EPathFinderShapesLayer.PATH_COLLISION_AREA).add(
					new DrawableTrajectoryArea(trajPath, DynamicMargin::getExtraMargin)
			);
		}
	}


	private void drawPathFinderResult(PathFinderResult pathResult)
	{
		for (var collision : pathResult.getCollisions())
		{
			if (Double.isFinite(collision.getFirstCollisionTime()))
			{
				IVector2 collisionPos = pathResult.getTrajectory().getPositionMM(collision.getFirstCollisionTime());
				getShapes().get(ESkillShapesLayer.PATH).add(
						new DrawablePoint(collisionPos).setColor(Color.RED)
				);
			}
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
		finder.setShapeMap(debugShapes ? getShapes() : null);
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


	@Override
	public void setPrioMap(final PathFinderPrioMap prioMap)
	{
		moveCon.setPrioMap(prioMap);
	}
}
