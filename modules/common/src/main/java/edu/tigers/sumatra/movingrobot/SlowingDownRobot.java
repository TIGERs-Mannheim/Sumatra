package edu.tigers.sumatra.movingrobot;

import org.apache.commons.lang3.Validate;


/**
 * A moving robot that tries to stay below a given velocity right at the given horizon.
 * For large horizons, it may accelerate up to the max velocity, as long as it can break enough at the given horizon.
 */
public class SlowingDownRobot extends AMovingRobot
{
	private final double vLimitAtHorizon;


	SlowingDownRobot(
			MovingRobotParams params,
			double vLimitAtHorizon
	)
	{
		super(params);
		this.vLimitAtHorizon = vLimitAtHorizon;
	}


	@Override
	MovingOffsets forwardBackwardOffset(double t)
	{
		double tBrakeTillLimit = Math.max(0, speed - vLimitAtHorizon) / brkLimit;

		if (t < tBrakeTillLimit)
		{
			// can't slow down in time, just calculate distance until t
			double mmDistFullTimeBraking = (speed - 0.5 * brkLimit * t) * t * 1000;
			return new MovingOffsets(
					mmDistFullTimeBraking,
					mmDistFullTimeBraking
			);
		}

		double tBrake = speed / brkLimit;
		double tAccBackwards = vLimitAtHorizon / aLimit;
		double backward;
		if (tBrake >= t)
		{
			// can't brake to full stop in time, just calculate distance until t
			backward = (speed - 0.5 * brkLimit * t) * t;
		} else if (tBrake + tAccBackwards >= t)
		{
			// can't get back to vLimitAtHorizon in time, just calculate distance until t
			double tAccPartially = t - tBrake;
			double distBrake = 0.5 * speed * tBrake;
			backward = distBrake - 0.5 * aLimit * tAccPartially * tAccPartially;
		} else
		{
			double distBrake = 0.5 * speed * tBrake;
			double distAcc = 0.5 * vLimitAtHorizon * tAccBackwards;
			backward = distBrake - distAcc - maximizeDistance(vLimitAtHorizon, t - (tBrake + tAccBackwards));
		}

		double tAccForwards = Math.max(0, vLimitAtHorizon - speed) / aLimit;
		double forward;
		if (tBrakeTillLimit > 0)
		{
			// We are currently faster than vLimitAtHorizon -> we'll need to brake till the limit at some point
			double distBrake = (speed - 0.5 * brkLimit * tBrakeTillLimit) * tBrakeTillLimit;
			forward = distBrake + maximizeDistance(speed, t - tBrakeTillLimit);
		} else if (tAccForwards > t)
		{
			// can't accelerate to vLimitAtHorizon in time, just calculate distance until t
			Validate.isTrue(tBrakeTillLimit == 0);
			forward = (speed + 0.5 * aLimit * t) * t;
		} else
		{
			// Accelerate till vLimitAtHorizon, and maximize distance afterwards
			double distAcc = (speed + 0.5 * aLimit * tAccForwards) * tAccForwards;
			forward = distAcc + maximizeDistance(vLimitAtHorizon, t - tAccForwards);
		}

		return new MovingOffsets(
				forward * 1000,
				backward * 1000
		);
	}
}
