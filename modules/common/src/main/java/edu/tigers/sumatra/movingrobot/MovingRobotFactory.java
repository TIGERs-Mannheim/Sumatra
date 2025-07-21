/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * A factory for moving robots.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MovingRobotFactory
{
	private static final double LARGE_DISTANCE = 20;
	private static final BangBangTrajectoryFactory TRAJECTORY_FACTORY = new BangBangTrajectoryFactory();


	public static IMovingRobot acceleratingRobot(
			IVector2 pos,
			IVector2 vel,
			double vMax,
			double acc,
			double radius,
			double reactionTime
	)
	{
		double speed = vel.getLength2();
		return new AcceleratingRobot(
				pos,
				vel,
				radius,
				reactionTime,
				TRAJECTORY_FACTORY.single(0, LARGE_DISTANCE, speed, vMax, acc),
				TRAJECTORY_FACTORY.single(0, -LARGE_DISTANCE, speed, vMax, acc)
		);
	}


	public static IMovingRobot stoppingRobot(
			IVector2 pos,
			IVector2 vel,
			double vLimit,
			double aLimit,
			double brkLimit,
			double radius,
			double reactionTime
	)
	{
		return new StoppingRobot(
				pos,
				vel,
				radius,
				reactionTime,
				vLimit,
				aLimit,
				brkLimit
		);
	}
}
