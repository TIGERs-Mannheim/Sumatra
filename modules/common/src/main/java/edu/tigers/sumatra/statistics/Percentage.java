/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statistics;

import com.sleepycat.persist.model.Persistent;


/**
 * Simple helper class for percentage calculation and data holding
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
@Persistent(version = 1)
public class Percentage
{
	private int current = 0;
	private int all = 1;
	
	
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
	public Percentage inc()
	{
		current = current + 1;
		return this;
	}
	
	
	/**
	 * Increments all value by one
	 *
	 * @return this for chaining
	 */
	public Percentage incAll()
	{
		all = all + 1;
		return this;
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
		return (double) current / all;
	}
	
	
	@Override
	public String toString()
	{
		return getCurrent() + "/" + getAll() + " = " + getPercent();
	}
}
