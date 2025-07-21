/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.RuleConstraints;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * Immutable kicker and dribbler parameters.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KickParams
{
	EKickerDevice device;
	double kickSpeed;
	EDribblerMode dribblerMode;
	double dribbleSpeed;
	double dribbleForce;
	double armDuration;


	private KickParams(final EKickerDevice device, final double kickSpeed)
	{
		this(device, kickSpeed, EDribblerMode.OFF, 0, 0, 0);
	}


	public static KickParams of(final EKickerDevice device, final double kickSpeed)
	{
		return new KickParams(device, kickSpeed);
	}


	public static KickParams maxStraight()
	{
		return new KickParams(EKickerDevice.STRAIGHT, RuleConstraints.getMaxKickSpeed());
	}


	public static KickParams maxChip()
	{
		return new KickParams(EKickerDevice.CHIP, RuleConstraints.getMaxKickSpeed());
	}


	public static KickParams straight(final double kickSpeed)
	{
		return new KickParams(EKickerDevice.STRAIGHT, kickSpeed);
	}


	public static KickParams chip(final double kickSpeed)
	{
		return new KickParams(EKickerDevice.CHIP, kickSpeed);
	}


	public static KickParams disarm()
	{
		return new KickParams(EKickerDevice.STRAIGHT, 0.0);
	}


	public static KickParams armTime(EKickerDevice device, double duration)
	{
		return new KickParams(device, 0, EDribblerMode.OFF, 0, 0, duration);
	}


	public KickParams withDribblerMode(EDribblerMode dribblerMode)
	{
		if (dribblerMode == EDribblerMode.OFF
				|| dribblerMode == EDribblerMode.DEFAULT
				|| dribblerMode == EDribblerMode.HIGH_POWER)
		{
			return new KickParams(device, kickSpeed, dribblerMode, 0, 0, armDuration);
		}
		throw new IllegalArgumentException(
				"Dribbler mode can not be set without additional parameters: " + dribblerMode);
	}


	public KickParams withDribbleSpeed(double dribbleSpeed, double dribbleForce)
	{
		return new KickParams(device, kickSpeed, EDribblerMode.MANUAL, dribbleSpeed, dribbleForce, armDuration);
	}
}

