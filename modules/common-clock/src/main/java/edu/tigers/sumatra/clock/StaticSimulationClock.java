/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.clock;

import java.util.concurrent.TimeUnit;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class StaticSimulationClock implements IClock
{
	private long	nanotime		= 0;
	private long	milliOffset	= System.currentTimeMillis();
	
	
	/**
	 * Reset time offsets. Be careful as application could misbehave because of unreasonable time changes
	 */
	public final void syncWithRealTime()
	{
		milliOffset = System.currentTimeMillis();
		nanotime = System.nanoTime();
	}
	
	
	/**
	 * @param nanotime
	 */
	public final void setNanoTime(final long nanotime)
	{
		this.nanotime = nanotime;
	}
	
	
	@Override
	public long currentTimeMillis()
	{
		return milliOffset + TimeUnit.NANOSECONDS.toMillis(nanotime);
	}
	
	
	@Override
	public long nanoTime()
	{
		return nanotime;
	}
	
	
	/**
	 * @param nanos
	 */
	public void step(final long nanos)
	{
		nanotime += nanos;
	}
	
	
	@Override
	public void sleep(final long millis)
	{
		throw new IllegalStateException("Sleeping is not supported for this implementation.");
	}
}
