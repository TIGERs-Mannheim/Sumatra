/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.RequiredArgsConstructor;


/**
 * A factory for robot that accelerate for a certain time horizon.
 */
@RequiredArgsConstructor
public class AcceleratingRobotFactory
{
	private static final double LARGE_DISTANCE = 20;
	private static final BangBangTrajectoryFactory TRAJECTORY_FACTORY = new BangBangTrajectoryFactory();

	private final double speed;
	private final double reactionTime;

	private final ITrajectory<Double> trajectoryForward;
	private final ITrajectory<Double> trajectoryBackward;


	public static IMovingRobot create(
			IVector2 pos,
			IVector2 vel,
			double vMax,
			double acc,
			double radius,
			double reactionTime
	)
	{
		double speed = vel.getLength2();
		var factory = new AcceleratingRobotFactory(
				speed,
				reactionTime,
				TRAJECTORY_FACTORY.single(0, LARGE_DISTANCE, speed, vMax, acc),
				TRAJECTORY_FACTORY.single(0, -LARGE_DISTANCE, speed, vMax, acc)
		);
		return new MovingRobotImpl(
				pos,
				vel.normalizeNew(),
				radius,
				speed,
				factory::forwardBackwardOffset
		);
	}


	private MovingOffsets forwardBackwardOffset(double tHorizon, double tAdditionalReaction)
	{
		double tReaction = Math.min(reactionTime + tAdditionalReaction, tHorizon);

		double distReaction = speed * tReaction * 1000;
		return new MovingOffsets(
				distReaction + trajectoryForward.getPositionMM(tHorizon - tReaction),
				distReaction + trajectoryBackward.getPositionMM(tHorizon - tReaction)
		);
	}
}
