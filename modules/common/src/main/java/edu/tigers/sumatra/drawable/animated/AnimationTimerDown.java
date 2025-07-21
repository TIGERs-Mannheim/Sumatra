/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

/**
 * This timer counts linearly from 1.0 to 0.0 and then restarts at 1.0.
 */
public class AnimationTimerDown extends AAnimationTimer
{
	/**
	 * @param period Time for a full period in [s]
	 */
	public AnimationTimerDown(final double period)
	{
		super(period);
	}


	/**
	 * @param period Time for a full period in [s]
	 * @param offset Offset for the timer in [s]. Can be used to de-synchronize multiple timers.
	 */
	public AnimationTimerDown(final double period, final float offset)
	{
		super(period, offset);
	}


	@Override
	public double getTimerValue()
	{
		return 1.0f - getRelativeTimerValue();
	}
}
