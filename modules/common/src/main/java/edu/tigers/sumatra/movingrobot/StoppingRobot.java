/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A moving robot that tries to stop right at the given horizon.
 * For large horizons, it may accelerate up to the max velocity, as long as it can stop at the given horizon.
 */
public class StoppingRobot extends AMovingRobot
{
	private final double vLimit;
	private final double aLimit;
	private final double brkLimit;


	StoppingRobot(
			IVector2 pos,
			IVector2 vel,
			double radius,
			double reactionTime,
			double vLimit,
			double aLimit,
			double brkLimit
	)
	{
		super(pos, vel.normalizeNew(), radius, vel.getLength2(), reactionTime);
		this.vLimit = vLimit;
		this.aLimit = aLimit;
		this.brkLimit = brkLimit;
	}


	@Override
	MovingOffsets forwardBackwardOffset(double t)
	{
		double tBrake = speed / brkLimit;

		if (tBrake > t)
		{
			// can't stop in time, just calculate distance until t
			double dvAfterT = brkLimit * t;
			double vAfterT = speed - dvAfterT;
			double distBrakePartially = vAfterT * t + 0.5 * dvAfterT * t;
			return new MovingOffsets(
					distBrakePartially * 1000,
					distBrakePartially * 1000
			);
		}

		double distBrake = 0.5 * speed * tBrake;
		double forward = distance(speed, t);
		double backward = distBrake - distance(0, t - tBrake);
		return new MovingOffsets(
				forward * 1000,
				backward * 1000
		);
	}


	private double distance(
			double vCur,
			double t
	)
	{
		double tBrake = vCur / brkLimit;

		if (tBrake > t)
		{
			// can't stop in time, just calculate distance until t
			double dvAfterT = brkLimit * t;
			double vAfterT = vCur - dvAfterT;
			return vAfterT * t + 0.5 * dvAfterT * t;
		}

		double tRemaining = t - tBrake;
		double tAccToMaxVel = Math.max(0, vLimit - vCur) / aLimit;
		double tAccToMaxVelAndBack = Math.min(tRemaining, tAccToMaxVel * 2);
		double tConst = tRemaining - tAccToMaxVelAndBack;

		double vMax = vCur + 0.5 * tAccToMaxVelAndBack * aLimit;

		double distBrake = 0.5 * vCur * tBrake;
		double distAcc = vCur * tAccToMaxVelAndBack + (vMax - vCur) * tAccToMaxVelAndBack * 0.5;
		double distConst = vMax * tConst;

		return distBrake + distAcc + distConst;
	}
}
