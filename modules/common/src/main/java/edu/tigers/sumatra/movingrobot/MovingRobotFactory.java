package edu.tigers.sumatra.movingrobot;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * A factory for moving robots.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MovingRobotFactory
{
	public static IMovingRobot acceleratingRobot(MovingRobotParams params)
	{
		return new AcceleratingRobot(params);
	}


	public static IMovingRobot stoppingRobot(MovingRobotParams params)
	{
		return new StoppingRobot(params);
	}


	public static IMovingRobot slowingDownRobot(MovingRobotParams params, double vLimitAtHorizon)
	{
		return new SlowingDownRobot(params, vLimitAtHorizon);
	}
}
