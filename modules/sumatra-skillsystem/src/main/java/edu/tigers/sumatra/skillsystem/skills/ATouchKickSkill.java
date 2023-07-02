/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import lombok.Setter;


/**
 * Abstract base class for touch kick skills.
 */
public abstract class ATouchKickSkill extends AMoveToSkill
{
	@Configurable(defValue = "0.7")
	protected static double maxBallSpeed = 0.7;

	protected final PositionValidator positionValidator = new PositionValidator();

	@Setter
	protected IVector2 target;
	@Setter
	protected double passRange;
	@Setter
	protected KickParams desiredKickParams = KickParams.disarm();
	@Setter
	protected boolean adaptKickSpeedToRobotSpeed = true;


	protected IVector2 getBallPos()
	{
		return getBall().getPos();
	}


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


	protected double getKickSpeed()
	{
		if (adaptKickSpeedToRobotSpeed)
		{
			return adaptKickSpeedToBotVel(target, desiredKickParams.getKickSpeed());
		}
		return desiredKickParams.getKickSpeed();
	}
}
