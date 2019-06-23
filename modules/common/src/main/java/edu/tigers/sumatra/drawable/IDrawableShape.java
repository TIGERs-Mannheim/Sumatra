/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.*;


/**
 * This interface makes a shape drawable
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@FunctionalInterface
public interface IDrawableShape
{
	/**
	 * Paint your shape
	 * 
	 * @param g handle to graphics object
	 * @param tool helper tool for drawing
	 * @param invert needs inversion?
	 */
	void paintShape(Graphics2D g, IDrawableTool tool, boolean invert);
	
	
	/**
	 * @param color
	 */
	default void setColor(final Color color)
	{
	}
	
	
	/**
	 * @param strokeWidth
	 */
	default void setStrokeWidth(double strokeWidth)
	{
	}
}
