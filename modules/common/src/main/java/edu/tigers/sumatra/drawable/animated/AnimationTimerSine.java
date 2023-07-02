/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;


/**
 * This timer counts from 0.0 to 1.0 continuously with a sine wave.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class AnimationTimerSine extends AAnimationTimer
{
	@SuppressWarnings("unused")
	private AnimationTimerSine()
	{
		super();
	}
	
	
	/**
	 * @param period Time for a full period in [s]
	 */
	public AnimationTimerSine(final double period)
	{
		super(period);
	}
	
	
	/**
	 * @param period Time for a full period in [s]
	 * @param offset Offset for the timer in [s]. Can be used to de-synchronize multiple timers.
	 */
	public AnimationTimerSine(final double period, final float offset)
	{
		super(period, offset);
	}
	
	
	@Override
	public double getTimerValue()
	{
		return ((SumatraMath.sin(getRelativeTimerValue() * AngleMath.PI_TWO) * 0.5f) + 0.5f);
	}
}
