/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.math.AngleMath;


/**
 * Check if the target angle is reached by considering not only a fixed difference tolerance,
 * but also if the current angle is sufficiently long within the tolerance.
 */
public class TargetAngleReachedChecker
{
	private boolean reached = false;

	private double outerAngleDiffTolerance;
	private final ChargingValue angleDiffChargingValue;


	/**
	 * Create a new checker.
	 *
	 * @param outerAngleDiffTolerance the min angle difference when target angle can be considered to be reached
	 * @param maxTime the max time until angle is considered reached when continuously within tolerance
	 */
	public TargetAngleReachedChecker(double outerAngleDiffTolerance, final double maxTime)
	{
		this.outerAngleDiffTolerance = outerAngleDiffTolerance;
		double chargeRate = outerAngleDiffTolerance / maxTime;
		angleDiffChargingValue = ChargingValue.aChargingValue().withDefaultValue(0).withChargeRate(chargeRate)
				.withLimit(outerAngleDiffTolerance).build();
	}


	/**
	 * Update with latest data
	 *
	 * @param targetAngle the desired target angle
	 * @param currentAngle the current angle
	 * @param curTimestamp the current timestamp of the frame
	 * @return diff the current angle difference
	 */
	public double update(final double targetAngle, double currentAngle, long curTimestamp)
	{
		double diff = Math.abs(AngleMath.difference(targetAngle, currentAngle));
		angleDiffChargingValue.update(curTimestamp);
		if (diff > outerAngleDiffTolerance)
		{
			reached = false;
			angleDiffChargingValue.reset();
		} else if (diff < getCurrentTolerance())
		{
			reached = true;
		}
		return diff;
	}


	public double getCurrentTolerance()
	{
		return angleDiffChargingValue.getValue();
	}


	public boolean isReached()
	{
		return reached;
	}


	public void setOuterAngleDiffTolerance(final double outerAngleDiffTolerance)
	{
		this.outerAngleDiffTolerance = outerAngleDiffTolerance;
	}
}
