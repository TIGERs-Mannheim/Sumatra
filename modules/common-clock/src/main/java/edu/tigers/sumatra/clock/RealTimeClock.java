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
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RealTimeClock implements IClock
{
	
	@Override
	public long currentTimeMillis()
	{
		return System.currentTimeMillis();
	}
	
	
	@Override
	public long nanoTime()
	{
		return System.nanoTime();
	}
	
	
	@Override
	public void sleep(final long millis)
	{
		try
		{
			Thread.sleep(millis);
		} catch (InterruptedException err)
		{
		}
	}
	
}
