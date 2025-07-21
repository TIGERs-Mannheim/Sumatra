/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

/**
 * This timer counts linearly from 0.0 to 1.0 and then restarts at 0.0.
 */
public class AnimationTimerUp extends AAnimationTimer
{
	/**
	 * @param period Time for a full period in [s]
	 */
	public AnimationTimerUp(final double period)
	{
		super(period);
	}


	/**
	 * @param period Time for a full period in [s]
	 * @param offset Offset for the timer in [s]. Can be used to de-synchronize multiple timers.
	 */
	public AnimationTimerUp(final double period, final float offset)
	{
		super(period, offset);
	}


	@Override
	public double getTimerValue()
	{
		return getRelativeTimerValue();
	}
}
