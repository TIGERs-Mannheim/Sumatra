/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.Validate;


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

	private Runnable onUpperCallback = () -> {};
	private Runnable onLowerCallback = () -> {};


	/**
	 * @param lowerThreshold
	 * @param upperThreshold
	 */
	public Hysteresis(final double lowerThreshold, final double upperThreshold)
	{
		this.lowerThreshold = lowerThreshold;
		this.upperThreshold = upperThreshold;
		Validate.isTrue(lowerThreshold < upperThreshold);
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
			onUpperCallback.run();
		}
		if (upper && (value < lowerThreshold))
		{
			upper = false;
			onLowerCallback.run();
		}
	}


	public boolean isLower()
	{
		return !upper;
	}
}
