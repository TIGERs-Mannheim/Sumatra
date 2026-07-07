package edu.tigers.sumatra.movingrobot;


/**
 * A moving robot that tries to stop right at the given horizon.
 * For large horizons, it may accelerate up to the max velocity, as long as it can stop at the given horizon.
 */
public class StoppingRobot extends AMovingRobot
{
	protected StoppingRobot(MovingRobotParams params)
	{
		super(params);
	}


	@Override
	MovingOffsets forwardBackwardOffset(double t)
	{
		double tBrake = speed / brkLimit;
		double tRemaining = t - tBrake;

		if (tRemaining <= 0)
		{
			// can't stop in time, just calculate distance until t
			double distBrakePartially = (speed - 0.5 * brkLimit * t) * t;
			return new MovingOffsets(
					distBrakePartially * 1000,
					distBrakePartially * 1000
			);
		}

		double distBrake = 0.5 * speed * tBrake;
		double forward = distBrake + maximizeDistance(speed, tRemaining);
		double backward = distBrake - maximizeDistance(0, tRemaining);

		return new MovingOffsets(
				forward * 1000,
				backward * 1000
		);
	}
}
