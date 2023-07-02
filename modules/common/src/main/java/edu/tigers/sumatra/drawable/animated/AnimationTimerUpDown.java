/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import com.sleepycat.persist.model.Persistent;


/**
 * This timer counts linearly from 0.0 to 1.0 and then back to 0.0.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class AnimationTimerUpDown extends AAnimationTimer
{
	@SuppressWarnings("unused")
	private AnimationTimerUpDown()
	{
		super();
	}
	
	
	/**
	 * @param period Time for a full period in [s]
	 */
	public AnimationTimerUpDown(final double period)
	{
		super(period);
	}
	
	
	/**
	 * @param period Time for a full period in [s]
	 * @param offset Offset for the timer in [s]. Can be used to de-synchronize multiple timers.
	 */
	public AnimationTimerUpDown(final double period, final double offset)
	{
		super(period, offset);
	}
	
	
	@Override
	public double getTimerValue()
	{
		double rel = getRelativeTimerValue();
		
		if (rel < 0.5f)
		{
			// count up
			return rel * 2.0f;
		}
		
		// count down
		return (1.0f - rel) * 2.0f;
	}
}
