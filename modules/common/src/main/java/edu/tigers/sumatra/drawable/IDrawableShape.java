/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * This interface makes a shape drawable
 */
@FunctionalInterface
public interface IDrawableShape
{
	/**
	 * Paint your shape
	 *
	 * @param g      handle to graphics object
	 * @param tool   helper tool for drawing
	 * @param invert needs inversion?
	 */
	void paintShape(Graphics2D g, IDrawableTool tool, boolean invert);


	/**
	 * @param color
	 */
	default IDrawableShape setColor(final Color color)
	{
		return this;
	}


	/**
	 * @param strokeWidth
	 */
	default IDrawableShape setStrokeWidth(double strokeWidth)
	{
		return this;
	}


	/**
	 * Fill the shape, if supported
	 *
	 * @param fill
	 */
	default IDrawableShape setFill(boolean fill)
	{
		return this;
	}

	/**
	 * @return is this shape a border text
	 */
	default boolean isBorderText()
	{
		return false;
	}
}
