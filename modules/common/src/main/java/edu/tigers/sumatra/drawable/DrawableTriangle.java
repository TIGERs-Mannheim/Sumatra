/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 7, 2015
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.triangle.Triangle;


/**
 * A Rectangle with a color
 * 
 * @author MarkG
 */
@Persistent
public class DrawableTriangle extends Triangle implements IDrawableShape
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
	private DrawableTriangle()
	{
		super(AVector2.ZERO_VECTOR, new Vector2(1, 1), new Vector2(1 - 1));
	}
	
	
	/**
	 * @param a
	 * @param b
	 * @param c
	 */
	public DrawableTriangle(final IVector2 a, final IVector2 b, final IVector2 c)
	{
		super(a, b, c);
		setColor(Color.red);
	}
	
	
	/**
	 * @param a
	 * @param b
	 * @param c
	 * @param color
	 */
	public DrawableTriangle(final IVector2 a, final IVector2 b, final IVector2 c, final Color color)
	{
		super(a, b, c);
		setColor(color);
	}
	
	
	/**
	 * @param triangle
	 */
	public DrawableTriangle(final Triangle triangle)
	{
		super(triangle);
	}
	
	
	/**
	 * @param triangle
	 * @param color
	 */
	public DrawableTriangle(final Triangle triangle, final Color color)
	{
		super(triangle);
		setColor(color);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		final IVector2 a = tool.transformToGuiCoordinates(getCorners().get(0), invert);
		final IVector2 b = tool.transformToGuiCoordinates(getCorners().get(1), invert);
		final IVector2 c = tool.transformToGuiCoordinates(getCorners().get(2), invert);
		
		int[] x = { (int) a.x(), (int) b.x(), (int) c.x() };
		int[] y = { (int) a.y(), (int) b.y(), (int) c.y() };
		int number = 3;
		
		g.setColor(getColor());
		g.drawPolygon(x, y, number);
		if (fill)
		{
			g.fillPolygon(x, y, number);
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
