/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import com.sleepycat.persist.model.Persistent;


/**
 * This animator returns a varying number between specified min and max.
 * Changing according to the used animation timer.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class NumberAnimatorMinMax implements INumberAnimator
{
	private final float min;
	private final float max;
	private final IAnimationTimer timer;
	
	
	@SuppressWarnings("unused")
	private NumberAnimatorMinMax()
	{
		min = 0;
		max = 1;
		timer = null;
	}
	
	
	/**
	 * @param min
	 * @param max
	 * @param timer
	 */
	public NumberAnimatorMinMax(final float min, final float max, final IAnimationTimer timer)
	{
		this.min = min;
		this.max = max;
		this.timer = timer;
	}
	
	
	@Override
	public float getNumber()
	{
		float counterValue = timer.getTimerValue();
		
		return ((1.0f - counterValue) * min) + (counterValue * max);
	}
	
}
