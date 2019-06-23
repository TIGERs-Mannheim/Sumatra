/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.clock;

/**
 * Our own clock util class to be able to fake timing for faster simulation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Deprecated
public final class SumatraClock
{
	private static IClock clock = new RealTimeClock();
	
	
	private SumatraClock()
	{
	}
	
	
	/**
	 * Change the global clock implementation
	 * 
	 * @param clock
	 */
	public static void setClock(final IClock clock)
	{
		SumatraClock.clock = clock;
	}
	
	
	/**
	 * @return
	 */
	public static long currentTimeMillis()
	{
		return clock.currentTimeMillis();
	}
	
	
	/**
	 * @return
	 */
	public static long nanoTime()
	{
		return clock.nanoTime();
	}
	
	
	/**
	 * @param millis
	 */
	public static void sleep(final long millis)
	{
		clock.sleep(millis);
	}
	
	
	/**
	 * @return the clock
	 */
	public static final IClock getClock()
	{
		return clock;
	}
}
