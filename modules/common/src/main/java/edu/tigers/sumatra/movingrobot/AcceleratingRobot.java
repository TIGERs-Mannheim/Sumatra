/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * A moving robot that is accelerating to the maximum velocity and keeps driving at this velocity.
 */
public class AcceleratingRobot extends AMovingRobot
{
	private final ITrajectory<Double> trajectoryForward;
	private final ITrajectory<Double> trajectoryBackward;


	AcceleratingRobot(
			IVector2 pos,
			IVector2 vel,
			double radius,
			double reactionTime,
			ITrajectory<Double> trajectoryForward,
			ITrajectory<Double> trajectoryBackward
	)
	{
		super(pos, vel.normalizeNew(), radius, vel.getLength2(), reactionTime);
		this.trajectoryForward = trajectoryForward;
		this.trajectoryBackward = trajectoryBackward;
	}


	@Override
	MovingOffsets forwardBackwardOffset(double t)
	{
		return new MovingOffsets(
				trajectoryForward.getPositionMM(t),
				trajectoryBackward.getPositionMM(t)
		);
	}
}
