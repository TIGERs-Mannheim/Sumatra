/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * A moving robot with a moving horizon
 */
@Getter
@RequiredArgsConstructor
public class MovingRobot
{
	private static final double LARGE_DISTANCE = 20;
	private static final BangBangTrajectoryFactory TRAJECTORY_FACTORY = new BangBangTrajectoryFactory();

	private final IVector2 pos;
	private final IVector2 dir;
	private final double maxHorizon;
	private final double radius;

	private final ITrajectory<Double> trajectoryForward;
	private final ITrajectory<Double> trajectoryBackward;


	public MovingRobot(IVector2 pos, IVector2 vel, double vMax, double acc, double maxHorizon, double radius)
	{
		this.pos = pos;
		this.dir = vel.normalizeNew();
		this.maxHorizon = maxHorizon;
		this.radius = radius;

		trajectoryForward = TRAJECTORY_FACTORY.single(0, LARGE_DISTANCE, vel.getLength2(), vMax, acc);
		trajectoryBackward = TRAJECTORY_FACTORY.single(0, -LARGE_DISTANCE, vel.getLength2(), vMax, acc);
	}


	/**
	 * @param tBot
	 * @param maxHorizon
	 * @param radius
	 */
	public static MovingRobot fromTrackedBot(final ITrackedBot tBot, final double maxHorizon, final double radius)
	{
		return new MovingRobot(
				tBot.getPos(),
				tBot.getVel(),
				tBot.getRobotInfo().getBotParams().getMovementLimits().getVelMax(),
				tBot.getRobotInfo().getBotParams().getMovementLimits().getAccMax(),
				maxHorizon,
				radius
		);
	}


	/**
	 * Get the horizon for possible movement of the robot for a given time horizon
	 *
	 * @param tHorizon the time horizon
	 * @return a circle specifying the horizon
	 */
	public ICircle getMovingHorizon(final double tHorizon)
	{
		double tLimitedHorizon = Math.max(0, Math.min(maxHorizon, tHorizon));

		double pForward = trajectoryForward.getPositionMM(tLimitedHorizon);
		double pBackward = trajectoryBackward.getPositionMM(tLimitedHorizon);

		double circleRadius = Math.abs(pForward - pBackward) / 2 + radius;
		IVector2 center = pos.addNew(dir.multiplyNew(pBackward + circleRadius - radius));
		return Circle.createCircle(center, circleRadius);
	}


	/**
	 * Get a tube with fixed width and length depending on the moving horizon.
	 *
	 * @param tHorizon the time horizon
	 * @return a tube specifying the movement horizon
	 */
	public ITube getMovingHorizonTube(final double tHorizon)
	{
		double tLimitedHorizon = Math.max(0, Math.min(maxHorizon, tHorizon));

		double pForward = trajectoryForward.getPositionMM(tLimitedHorizon);
		double pBackward = trajectoryBackward.getPositionMM(tLimitedHorizon);

		return Tube.create(
				dir.multiplyNew(pBackward).add(pos),
				dir.multiplyNew(pForward).add(pos),
				radius
		);
	}


	public double getMaxSpeed()
	{
		return Math.max(trajectoryForward.getMaxSpeed(), trajectoryBackward.getMaxSpeed());
	}
}
