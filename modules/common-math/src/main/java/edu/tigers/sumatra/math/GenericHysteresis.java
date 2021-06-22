/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;


/**
 * Generic hysteresis with a lower and an upper function.
 */
@Getter
@Setter
public class GenericHysteresis
{
	private final Supplier<Boolean> lowerFunction;
	private final Supplier<Boolean> upperFunction;

	private boolean upper = false;


	public GenericHysteresis(
			final Supplier<Boolean> lowerFunction,
			final Supplier<Boolean> upperFunction
	)
	{
		this.lowerFunction = lowerFunction;
		this.upperFunction = upperFunction;
	}


	/**
	 * Set the initial state to upper
	 *
	 * @return this instance
	 */
	public GenericHysteresis initiallyInUpperState()
	{
		this.upper = true;
		return this;
	}


	public void update()
	{
		boolean lower = !upper;
		if (lower && upperFunction.get())
		{
			upper = true;
		}
		if (upper && lowerFunction.get())
		{
			upper = false;
		}
	}


	public boolean isLower()
	{
		return !upper;
	}
}
