/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import com.sleepycat.persist.model.Persistent;


/**
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public abstract class AAnimationTimer implements IAnimationTimer
{
	private final double period;
	private final double offset;
	
	
	@SuppressWarnings("unused")
	protected AAnimationTimer()
	{
		period = 1;
		offset = 0;
	}
	
	
	/**
	 * @param period Time for a full period in [s]
	 */
	protected AAnimationTimer(final double period)
	{
		this.period = period;
		offset = 0;
	}
	
	
	/**
	 * @param period Time for a full period in [s]
	 * @param offset Offset for the timer in [s]. Can be used to de-synchronize multiple timers.
	 */
	protected AAnimationTimer(final double period, final double offset)
	{
		this.period = period;
		this.offset = offset;
	}
	
	
	protected double getRelativeTimerValue()
	{
		long iPeriod = (long) (period * 1e9f);
		long iOffset = (long) (offset * 1e9f);
		
		return ((System.nanoTime() + iOffset) % iPeriod) / ((double) iPeriod);
	}
}
