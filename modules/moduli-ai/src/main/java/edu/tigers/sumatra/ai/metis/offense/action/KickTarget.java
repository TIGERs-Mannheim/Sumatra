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
	private EKickTargetMode mode;
	
	
	@SuppressWarnings("unused") // used by berkeley
	private KickTarget()
	{
		target = null;
		ballSpeedAtTarget = 0;
		chipPolicy = null;
		mode = EKickTargetMode.PASS;
	}
	
	
	private KickTarget(final DynamicPosition target, final double ballSpeedAtTarget, final ChipPolicy chipPolicy,
			final EKickTargetMode mode)
	{
		this.target = target;
		this.ballSpeedAtTarget = ballSpeedAtTarget;
		this.chipPolicy = chipPolicy;
		this.mode = mode;
	}
	
	
	public static KickTarget goalShot(final DynamicPosition target)
	{
		return new KickTarget(target, -1, ChipPolicy.NO_CHIP, EKickTargetMode.GOAL_KICK);
	}
	
	
	public static KickTarget pass(final DynamicPosition target, final double ballSpeedAtTarget,
			final ChipPolicy chipPolicy)
	{
		return new KickTarget(target, ballSpeedAtTarget, chipPolicy, EKickTargetMode.PASS);
	}
	
	
	public static KickTarget kickInsBlaue(final DynamicPosition target, final double ballSpeedAtTarget,
			final ChipPolicy chipPolicy)
	{
		return new KickTarget(target, ballSpeedAtTarget, chipPolicy, EKickTargetMode.KICK_INS_BLAUE);
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
	
	
	public EKickTargetMode getMode()
	{
		return mode;
	}
	
	public enum ChipPolicy
	{
		NO_CHIP,
		ALLOW_CHIP,
		FORCE_CHIP
	}
}
