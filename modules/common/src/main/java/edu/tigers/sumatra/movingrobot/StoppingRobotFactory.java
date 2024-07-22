/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;


/**
 * A factory for robots that stop at the end of the moving horizon.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StoppingRobotFactory
{
	private final double speed;
	private final double reactionTime;
	private final double vLimit;
	private final double aLimit;
	private final double brkLimit;


	public static IMovingRobot create(
			IVector2 pos,
			IVector2 vel,
			double vLimit,
			double aLimit,
			double brkLimit,
			double radius,
			double reactionTime
	)
	{
		double speed = vel.getLength2();
		var factory = new StoppingRobotFactory(
				speed,
				reactionTime,
				vLimit,
				aLimit,
				brkLimit
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

		double t = tHorizon - tReaction;
		double vCur = speed;

		double distReaction = speed * tReaction;
		double tBrake = vCur / brkLimit;

		if (tBrake > t)
		{
			// can't stop in time, just calculate distance until t
			double dvAfterT = brkLimit * t;
			double vAfterT = vCur - dvAfterT;
			double distBrakePartially = vAfterT * t + 0.5 * dvAfterT * t;
			return new MovingOffsets(
					(distReaction + distBrakePartially) * 1000,
					(distReaction + distBrakePartially) * 1000
			);
		}

		double distBrake = 0.5 * vCur * tBrake;
		return new MovingOffsets(
				(distReaction + distance(speed, t)) * 1000,
				(distReaction + distBrake - distance(0, t - tBrake)) * 1000
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
