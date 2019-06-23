/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import com.sleepycat.persist.model.Persistent;


/**
 * Simple helper class for percentage calculation and data holding
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
@Persistent
public class Percentage
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private int		current	= 0;
	private int		all		= 1;
	private double	percent	= 0.0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Returns current value of this Percentage
	 * 
	 * @return
	 */
	public int getCurrent()
	{
		return current;
	}
	
	
	/**
	 * Increments current value by one
	 */
	public void inc()
	{
		current = current + 1;
		percent = (double) current / all;
	}
	
	
	/**
	 * @param all , base value, do not update with all<=0
	 */
	public void setAll(final int all)
	{
		this.all = all;
		percent = (double) current / all;
	}
	
	
	/**
	 * @return the percent
	 */
	public double getPercent()
	{
		return percent;
	}
}
