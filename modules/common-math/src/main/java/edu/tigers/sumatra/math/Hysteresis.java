/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

/**
 * The hysteresis switches a binary state based on two thresholds
 */
public class Hysteresis
{
	private double lowerThreshold;
	private double upperThreshold;
	
	private boolean upper = false;
	
	
	/**
	 * @param lowerThreshold
	 * @param upperThreshold
	 */
	public Hysteresis(final double lowerThreshold, final double upperThreshold)
	{
		super();
		this.lowerThreshold = lowerThreshold;
		this.upperThreshold = upperThreshold;
		assert lowerThreshold < upperThreshold;
	}
	
	
	/**
	 * Set the initial state to upper
	 * 
	 * @return this instance
	 */
	public Hysteresis initiallyInUpperState()
	{
		this.upper = true;
		return this;
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
	
	
	public void setLowerThreshold(final double lowerThreshold)
	{
		this.lowerThreshold = lowerThreshold;
	}
	
	
	public void setUpperThreshold(final double upperThreshold)
	{
		this.upperThreshold = upperThreshold;
	}
}
