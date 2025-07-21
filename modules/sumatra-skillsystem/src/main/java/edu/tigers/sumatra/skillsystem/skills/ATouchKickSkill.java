/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import lombok.Setter;


/**
 * Abstract base class for touch kick skills.
 */
public abstract class ATouchKickSkill extends ABallHandlingSkill
{
	@Configurable(defValue = "1.2")
	protected static double maxBallSpeed = 1.2;

	@Setter
	protected KickParams desiredKickParams = KickParams.disarm();
	@Setter
	protected boolean adaptKickSpeedToRobotSpeed = true;


	/**
	 * Setter for instanceables. Setting desiredKickParams directly should be preferred.
	 *
	 * @param kickerDevice
	 */
	public void setKickerDevice(EKickerDevice kickerDevice)
	{
		this.desiredKickParams = KickParams.of(kickerDevice, desiredKickParams.getKickSpeed())
				.withDribblerMode(desiredKickParams.getDribblerMode());
	}


	/**
	 * Setter for instanceables. Setting desiredKickParams directly should be preferred.
	 *
	 * @param kickSpeed
	 */
	public void setKickSpeed(double kickSpeed)
	{
		this.desiredKickParams = KickParams.of(desiredKickParams.getDevice(), kickSpeed)
				.withDribblerMode(desiredKickParams.getDribblerMode());
	}


	public void setKickArmTime(double duration)
	{
		this.desiredKickParams = KickParams.armTime(desiredKickParams.getDevice(), duration)
				.withDribblerMode(desiredKickParams.getDribblerMode());
	}


	protected double getKickSpeed()
	{
		double kickSpeed = desiredKickParams.getKickSpeed();
		if (adaptKickSpeedToRobotSpeed)
		{
			kickSpeed = adaptKickSpeedToBotVel(target, kickSpeed);
		}
		return kickSpeed;
	}


	protected KickParams getArmedKickParams()
	{
		if (desiredKickParams.getArmDuration() > 0)
		{
			return desiredKickParams;
		}
		return KickParams.of(desiredKickParams.getDevice(), getKickSpeed());
	}
}
