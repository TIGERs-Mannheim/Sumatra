/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import com.sleepycat.persist.model.Persistent;


/**
 * This timer returns either 0.0 or 1.0.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class AnimationTimerOnOff extends AAnimationTimer
{
	private final float onPercentage;
	
	
	@SuppressWarnings("unused")
	private AnimationTimerOnOff()
	{
		super();
		onPercentage = 0.5f;
	}
	
	
	/**
	 * @param period Time for a full period in [s]
	 */
	public AnimationTimerOnOff(final float period)
	{
		super(period);
		onPercentage = 0.5f;
	}
	
	
	/**
	 * @param period Time for a full period in [s]
	 * @param offset Offset for the timer in [s]. Can be used to de-synchronize multiple timers.
	 */
	public AnimationTimerOnOff(final float period, final float offset)
	{
		super(period, offset);
		onPercentage = 0.5f;
	}
	
	
	/**
	 * @param period Time for a full period in [s]
	 * @param offset Offset for the timer in [s]. Can be used to de-synchronize multiple timers.
	 * @param onPercentage Percenate of how long the on-time is. Range 0.0 - 1.0
	 */
	public AnimationTimerOnOff(final float period, final float offset, final float onPercentage)
	{
		super(period, offset);
		this.onPercentage = onPercentage;
	}
	
	
	@Override
	public float getTimerValue()
	{
		if (getRelativeTimerValue() < onPercentage)
		{
			return 1.0f;
		}
		
		return 0.0f;
	}
}
