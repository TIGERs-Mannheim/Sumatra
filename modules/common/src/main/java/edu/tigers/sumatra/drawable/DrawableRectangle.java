/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 7, 2015
 * Author(s): tilman
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.rectangle.IRectangle;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;


/**
 * A Rectangle with a color
 * 
 * @author tilman
 */
@Persistent(version = 1)
public class DrawableRectangle extends Rectangle implements IDrawableShape
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private Color		color;
	private boolean	fill	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * For db only
	 */
	@SuppressWarnings("unused")
	private DrawableRectangle()
	{
		super(new Rectangle(AVector2.ZERO_VECTOR, new Vector2(1, 1)));
	}
	
	
	/**
	 * @param rec
	 */
	public DrawableRectangle(final IRectangle rec)
	{
		super(rec);
		setColor(Color.red);
	}
	
	
	/**
	 * A Rectangle that can be drawn, duh!
	 * 
	 * @param rec
	 * @param color
	 */
	public DrawableRectangle(final IRectangle rec, final Color color)
	{
		super(rec);
		setColor(color);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 topLeft = tool.transformToGuiCoordinates(topLeft(), invert);
		final IVector2 bottomRight = tool.transformToGuiCoordinates(bottomRight(), invert);
		
		int x = (int) (topLeft.x() < bottomRight.x() ? topLeft.x() : bottomRight.x());
		int y = (int) (topLeft.y() < bottomRight.y() ? topLeft.y() : bottomRight.y());
		
		int width = Math.abs((int) (bottomRight.x() - topLeft.x()));
		int height = Math.abs((int) (bottomRight.y() - topLeft.y()));
		
		g.setColor(getColor());
		g.drawRect(x, y, width, height);
		if (fill)
		{
			g.fillRect(x, y, width, height);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the color
	 */
	public Color getColor()
	{
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
	 * @param fill
	 */
	public void setFill(final boolean fill)
	{
		this.fill = fill;
	}
}
