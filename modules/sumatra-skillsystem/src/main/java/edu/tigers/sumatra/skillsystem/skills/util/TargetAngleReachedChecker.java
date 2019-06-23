/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.math.AngleMath;


/**
 * Check if the target angle is reached by considering not only a fixed difference tolerance,
 * but also if either the current angle has crossed the target angle or the current angle is sufficiently long within
 * the tolerance.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TargetAngleReachedChecker
{
	private boolean reached = false;
	private boolean lastSignPositive = true;
	
	private final double outerAngleDiffTolerance;
	private final ChargingValue angleDiffChargingValue;
	
	private boolean respectSign = true;
	
	
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
	 */
	public void update(final double targetAngle, double currentAngle, long curTimestamp)
	{
		double diff = AngleMath.difference(targetAngle, currentAngle);
		boolean curSignPositive = Math.signum(diff) > 0;
		angleDiffChargingValue.update(curTimestamp);
		if (Math.abs(diff) > outerAngleDiffTolerance)
		{
			reached = false;
			angleDiffChargingValue.reset();
		} else if ((respectSign && lastSignPositive != curSignPositive) // crossed target orientation
				|| Math.abs(diff) < angleDiffChargingValue.getValue()) // timeout
		{
			reached = true;
		}
		lastSignPositive = curSignPositive;
	}
	
	
	public boolean isReached()
	{
		return reached;
	}
	
	
	public void setRespectSign(final boolean respectSign)
	{
		this.respectSign = respectSign;
	}
}
