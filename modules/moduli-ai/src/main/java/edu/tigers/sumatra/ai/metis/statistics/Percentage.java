/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import com.sleepycat.persist.model.Persistent;


/**
 * Simple helper class for percentage calculation and data holding
 */
@Persistent(version = 1)
public class Percentage
{
	private int current = 0;
	private int all = 0;
	
	
	public Percentage()
	{
	}
	
	
	/**
	 * Constructor for setting current and all value
	 * 
	 * @param cur
	 * @param all
	 */
	public Percentage(final int cur, final int all)
	{
		current = cur;
		this.all = all;
	}
	
	
	/**
	 * Returns current value of this Percentage
	 * 
	 * @return
	 */
	public int getCurrent()
	{
		return current;
	}
	
	
	public int getAll()
	{
		return all;
	}
	
	
	/**
	 * Increments current value by one
	 * 
	 * @return this for chaining
	 */
	public void inc()
	{
		current++;
	}
	
	
	/**
	 * Increments all value by one
	 *
	 * @return this for chaining
	 */
	public void incAll()
	{
		all++;
	}
	
	
	/**
	 * @param all , base value, do not update with all<=0
	 */
	public void setAll(final int all)
	{
		this.all = all;
	}
	
	
	/**
	 * @return the percent
	 */
	public double getPercent()
	{
		if (all == 0)
		{
			return 0;
		}
		return (double) current / all;
	}
	
	
	@Override
	public String toString()
	{
		return String.valueOf(getPercent());
	}
}
