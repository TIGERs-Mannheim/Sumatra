/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.RuleConstraints;


/**
 * Mutable kick parameters as input to kick skills
 */
public class KickParams
{
	private EKickerDevice device;
	private double kickSpeed;
	private double dribbleSpeed;
	
	@Configurable(comment = "Offset to speed limit", defValue = "0.2")
	private static double speedLimitOffset = 0.2;
	
	static
	{
		ConfigRegistration.registerClass("skills", KickParams.class);
	}
	
	
	public KickParams(final EKickerDevice device, final double kickSpeed, final double dribbleSpeed)
	{
		this.device = device;
		this.kickSpeed = kickSpeed;
		this.dribbleSpeed = dribbleSpeed;
	}
	
	
	private KickParams(final EKickerDevice device, final double kickSpeed)
	{
		this(device, kickSpeed, 0);
	}
	
	
	public static KickParams of(final EKickerDevice device, final double kickSpeed)
	{
		return new KickParams(device, kickSpeed);
	}
	
	
	public static KickParams maxStraight()
	{
		return new KickParams(EKickerDevice.STRAIGHT, RuleConstraints.getMaxBallSpeed());
	}
	
	
	public static KickParams maxChip()
	{
		return new KickParams(EKickerDevice.CHIP, RuleConstraints.getMaxBallSpeed());
	}
	
	
	public static KickParams straight(final double kickSpeed)
	{
		return new KickParams(EKickerDevice.STRAIGHT, kickSpeed);
	}
	
	
	public static KickParams chip(final double kickSpeed)
	{
		return new KickParams(EKickerDevice.CHIP, kickSpeed);
	}
	
	
	public static double limitKickSpeed(final double kickSpeed)
	{
		return applyFixedOffset(Math.min(RuleConstraints.getMaxBallSpeed(), kickSpeed));
	}
	
	
	private static double applyFixedOffset(final double kickSpeed)
	{
		return Math.max(0, kickSpeed - speedLimitOffset);
	}
	
	
	public void setDevice(final EKickerDevice device)
	{
		this.device = device;
	}
	
	
	public void setKickSpeed(final double kickSpeed)
	{
		this.kickSpeed = kickSpeed;
	}
	
	
	public EKickerDevice getDevice()
	{
		return device;
	}
	
	
	public double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	public double getDribbleSpeed()
	{
		return dribbleSpeed;
	}
	
	
	public void setDribbleSpeed(final double dribbleSpeed)
	{
		this.dribbleSpeed = dribbleSpeed;
	}
}

