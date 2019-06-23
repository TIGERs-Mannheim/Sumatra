/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.wp.data.DynamicPosition;


@Persistent
public class KickTarget
{
	private final DynamicPosition target;
	private final double ballSpeedAtTarget;
	private final ChipPolicy chipPolicy;
	
	
	@SuppressWarnings("unused") // used by berkeley
	private KickTarget()
	{
		target = null;
		ballSpeedAtTarget = 0;
		chipPolicy = null;
	}
	
	
	public KickTarget(final DynamicPosition target, final double ballSpeedAtTarget, final ChipPolicy chipPolicy)
	{
		this.target = target;
		this.ballSpeedAtTarget = ballSpeedAtTarget;
		this.chipPolicy = chipPolicy;
	}
	
	
	public DynamicPosition getTarget()
	{
		return target;
	}
	
	
	public double getBallSpeedAtTarget()
	{
		return ballSpeedAtTarget;
	}
	
	
	public ChipPolicy getChipPolicy()
	{
		return chipPolicy;
	}
	
	public enum ChipPolicy
	{
		NO_CHIP,
		ALLOW_CHIP,
		FORCE_CHIP
	}
}
