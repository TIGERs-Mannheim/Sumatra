/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import java.awt.Color;


/**
 * Actually not an animator. Uses a fixed value.
 */
public class ColorAnimatorFixed implements IColorAnimator
{
	private final Color color;


	public ColorAnimatorFixed(final Color color)
	{
		this.color = color;
	}


	@Override
	public Color getColor()
	{
		return color;
	}
}
