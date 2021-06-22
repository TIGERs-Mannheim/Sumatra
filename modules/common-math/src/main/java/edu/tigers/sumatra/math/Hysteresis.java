/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * The hysteresis switches a binary state based on two thresholds
 */
@AllArgsConstructor
@Setter
@Getter
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


	public boolean isLower()
	{
		return !upper;
	}
}
