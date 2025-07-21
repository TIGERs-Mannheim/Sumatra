/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

/**
 * Actually not an animator. Uses a fixed value.
 */
public class NumberAnimatorFixed implements INumberAnimator
{
	private final double number;


	/**
	 * @param number
	 */
	public NumberAnimatorFixed(final double number)
	{
		this.number = number;
	}


	@Override
	public double getNumber()
	{
		return number;
	}
}
