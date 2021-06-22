/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.math.AngleMath;
import lombok.Getter;


/**
 * Check if the target angle is reached by considering not only a fixed difference tolerance,
 * but also if the current angle is sufficiently long within the tolerance.
 */
public class TargetAngleReachedChecker
{
	@Getter
	private boolean reached = false;
	@Getter
	private boolean roughlyFocussed = false;

	private double outerAngleDiffTolerance;
	private final ChargingValue angleDiffChargingValue;


	/**
	 * Create a new checker.
	 *
	 * @param outerAngleDiffTolerance the min angle difference when target angle can be considered to be reached
	 * @param maxTime                 the max time until angle is considered reached when continuously within tolerance
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
	 * @param targetAngle  the desired target angle
	 * @param currentAngle the current angle
	 * @param curTimestamp the current timestamp of the frame
	 * @return diff the current angle difference
	 */
	public double update(final double targetAngle, double currentAngle, long curTimestamp)
	{
		double diff = Math.abs(AngleMath.difference(targetAngle, currentAngle));
		angleDiffChargingValue.update(curTimestamp);
		roughlyFocussed = diff <= outerAngleDiffTolerance;
		if (!roughlyFocussed)
		{
			angleDiffChargingValue.reset();
		}
		reached = diff < getCurrentTolerance();
		return diff;
	}


	public double getCurrentTolerance()
	{
		return angleDiffChargingValue.getValue();
	}


	public void setOuterAngleDiffTolerance(final double outerAngleDiffTolerance)
	{
		this.outerAngleDiffTolerance = outerAngleDiffTolerance;
	}
}
