/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.10.2010
 * Author(s): Yakisoba
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman.data;


/**
 *
 */
public class UnregisteredBot
{
	/** timestamps are in local java nanoTime */
	private final long	oldTimestamp;
	private long			newTimestamp;
	private WPCamBot		visionBot;
	private int				count;
								
								
	/**
	 * @param time
	 * @param visionbot
	 */
	public UnregisteredBot(final long time, final WPCamBot visionbot)
	{
		oldTimestamp = time;
		setNewTimestamp(time);
		visionBot = visionbot;
		setCount(0);
	}
	
	
	/**
	 * @param time
	 * @param visionbot
	 */
	public void addBot(final long time, final WPCamBot visionbot)
	{
		setNewTimestamp(time);
		visionBot = visionbot;
		setCount(getCount() + 1);
	}
	
	
	/**
	 * @return the oldTimestamp
	 */
	public long getOldTimestamp()
	{
		return oldTimestamp;
	}
	
	
	/**
	 * @return the newTimestamp
	 */
	public long getNewTimestamp()
	{
		return newTimestamp;
	}
	
	
	/**
	 * @param newTimestamp the newTimestamp to set
	 */
	public void setNewTimestamp(final long newTimestamp)
	{
		this.newTimestamp = newTimestamp;
	}
	
	
	/**
	 * @return the visionBot
	 */
	public WPCamBot getVisionBot()
	{
		return visionBot;
	}
	
	
	/**
	 * @return the count
	 */
	public int getCount()
	{
		return count;
	}
	
	
	/**
	 * @param count the count to set
	 */
	private void setCount(final int count)
	{
		this.count = count;
	}
}
