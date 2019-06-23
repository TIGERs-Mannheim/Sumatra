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

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2f;


/**
 * A simple drawable point
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawablePoint extends Vector2f implements IDrawableShape
{
	/** Size of a point in field unit [mm] */
	private static int	pointSize	= 25;
	private Color			color			= Color.red;
	private String			text			= "";
	
	
	@SuppressWarnings("unused")
	private DrawablePoint()
	{
		
	}
	
	
	/**
	 * @param point
	 * @param color
	 */
	public DrawablePoint(final IVector2 point, final Color color)
	{
		super(point);
		this.color = color;
	}
	
	
	/**
	 * @param point
	 */
	public DrawablePoint(final IVector2 point)
	{
		super(point);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 transPoint = tool.transformToGuiCoordinates(this, invert);
		int guiPointSize = tool.scaleXLength(pointSize);
		
		final int drawingX = (int) transPoint.x() - (guiPointSize / 2);
		final int drawingY = (int) transPoint.y() - (guiPointSize / 2);
		
		g.setColor(getColor());
		g.fillOval(drawingX, drawingY, guiPointSize, guiPointSize);
		g.drawString(text, drawingX, drawingY);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the color
	 */
	public Color getColor()
	{
		// this may happen with old databases
		if (color == null)
		{
			// standard color
			return Color.red;
		}
		return color;
	}
	
	
	/**
	 * @param color the color to set
	 */
	@Override
	public void setColor(final Color color)
	{
		this.color = color;
	}
	
	
	/**
	 * @return the text
	 */
	public final String getText()
	{
		return text;
	}
	
	
	/**
	 * @param text the text to set
	 */
	public final void setText(final String text)
	{
		this.text = text;
	}
	
	
	/**
	 * @param size of the point
	 */
	public void setSize(final int size)
	{
		pointSize = size;
	}
}
