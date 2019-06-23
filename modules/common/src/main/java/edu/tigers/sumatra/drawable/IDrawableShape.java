/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * This interface makes a shape drawable
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IDrawableShape
{
	/**
	 * Paint your shape
	 * 
	 * @param g
	 * @param tool
	 * @param invert
	 */
	void paintShape(Graphics2D g, IDrawableTool tool, boolean invert);
	
	
	/**
	 * @param drawDebug
	 */
	default void setDrawDebug(final boolean drawDebug)
	{
	}
	
	
	/**
	 * @param color
	 */
	default void setColor(final Color color)
	{
	}
}
