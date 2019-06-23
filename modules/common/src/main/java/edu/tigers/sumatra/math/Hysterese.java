/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.math;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Hysterese
{
	private final double	lowerThreshold;
	private final double	upperThreshold;
	
	private boolean		upper	= false;
	
	
	/**
	 * @param lowerThreshold
	 * @param upperThreshold
	 */
	public Hysterese(final double lowerThreshold, final double upperThreshold)
	{
		super();
		this.lowerThreshold = lowerThreshold;
		this.upperThreshold = upperThreshold;
		assert lowerThreshold < upperThreshold;
	}
	
	
	/**
	 * @param value
	 */
	public void update(final double value)
	{
		boolean lower = !upper;
		if (lower && (value > upperThreshold))
		{
			upper = true;
		}
		if (upper && (value < lowerThreshold))
		{
			upper = false;
		}
	}
	
	
	/**
	 * @return
	 */
	public boolean isLower()
	{
		return !upper;
	}
	
	
	/**
	 * @return
	 */
	public boolean isUpper()
	{
		return upper;
	}
	
}
