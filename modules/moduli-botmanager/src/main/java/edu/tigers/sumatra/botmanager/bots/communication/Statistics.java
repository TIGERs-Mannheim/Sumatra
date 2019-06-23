/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.bots.communication;

import com.sleepycat.persist.model.Persistent;


/**
 * Communication statistics packet.
 * 
 * @author AndreR
 */
@Persistent
@SuppressWarnings("squid:ClassVariableVisibilityCheck")
public class Statistics
{
	/**
	 * Payload bytes.
	 */
	public int		payload	= 0;
	
	/**
	 * Raw bytes on wire.
	 */
	public int		raw		= 0;
	
	/**
	 * Number of high-level packets.
	 */
	public int		packets	= 0;
	
	private long	lastReset;
	
	
	/**
	 * Default constructor.
	 */
	public Statistics()
	{
		lastReset = System.nanoTime();
	}
	
	
	/**
	 * @param org
	 */
	public Statistics(final Statistics org)
	{
		payload = org.payload;
		raw = org.raw;
		packets = org.packets;
		lastReset = org.lastReset;
	}
	
	
	/**
	 * Reset statistics to zero.
	 */
	public void reset()
	{
		payload = 0;
		raw = 0;
		packets = 0;
		
		lastReset = System.nanoTime();
	}
	
	
	/**
	 * @return
	 */
	public long getLastResetTimestamp()
	{
		return lastReset;
	}
	
	
	/**
	 * @param stat
	 * @return
	 */
	public Statistics substract(final Statistics stat)
	{
		final Statistics ret = new Statistics();
		ret.payload = payload - stat.payload;
		ret.raw = raw - stat.raw;
		ret.packets = packets - stat.packets;
		
		return ret;
	}
	
	
	/**
	 * @param stat
	 * @return
	 */
	public Statistics add(final Statistics stat)
	{
		final Statistics ret = new Statistics();
		ret.payload = payload + stat.payload;
		ret.raw = raw + stat.raw;
		ret.packets = packets + stat.packets;
		
		return ret;
	}
	
	
	/**
	 * @return
	 */
	public double getOverheadPercentage()
	{
		if (raw == 0)
		{
			return 0;
		}
		
		return 1.0f - (((double) payload) / ((double) raw));
	}
	
	
	/**
	 * @param passedTime
	 * @return
	 */
	public double getLoadPercentage(final double passedTime)
	{
		if (passedTime <= 0)
		{
			return 0;
		}
		
		return (raw) / (28800.0f * passedTime);
	}
	
	
	/**
	 * @return
	 */
	public double getLoadPercentageWithLastReset()
	{
		return getLoadPercentage((System.nanoTime() - lastReset) / 1000000000.0);
	}
}
