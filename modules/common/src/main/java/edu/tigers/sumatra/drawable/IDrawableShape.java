/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * This interface makes a shape drawable
 */
public interface IDrawableShape
{
	/**
	 * Paint your shape
	 *
	 * @param g      handle to graphics object
	 * @param tool   helper tool for drawing
	 * @param invert needs inversion?
	 */
	default void paintShape(Graphics2D g, IDrawableTool tool, boolean invert)
	{
	}

	/**
	 * Paint in absolute coordinates, independent of the field.
	 *
	 * @param g      handle to graphics object
	 * @param width  currently visible width of the field panel
	 * @param height currently visible height of the field panel
	 */
	default void paintBorder(Graphics2D g, int width, int height)
	{
	}

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
}
