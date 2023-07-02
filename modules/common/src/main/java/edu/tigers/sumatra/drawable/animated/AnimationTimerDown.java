/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import com.sleepycat.persist.model.Persistent;


/**
 * This timer counts linearly from 1.0 to 0.0 and then restarts at 1.0.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class AnimationTimerDown extends AAnimationTimer
{
	@SuppressWarnings("unused")
	private AnimationTimerDown()
	{
		super();
	}
	
	
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
