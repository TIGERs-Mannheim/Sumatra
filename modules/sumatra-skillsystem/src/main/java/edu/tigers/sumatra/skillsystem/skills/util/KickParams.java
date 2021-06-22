/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.RuleConstraints;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * Immutable kicker and dribbler parameters.
 */
@Persistent
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KickParams
{
	EKickerDevice device;
	double kickSpeed;
	double dribbleSpeed;


	@SuppressWarnings("unused") // berkeley
	private KickParams()
	{
		this(EKickerDevice.STRAIGHT, 0);
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


	public KickParams withDribbleSpeed(double dribbleSpeed)
	{
		return new KickParams(device, kickSpeed, dribbleSpeed);
	}
}

