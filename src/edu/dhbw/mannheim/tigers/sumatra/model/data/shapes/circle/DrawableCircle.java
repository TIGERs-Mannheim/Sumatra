/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ColorWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * Drawable of a circle
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class DrawableCircle extends Circle
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private ColorWrapper	color	= new ColorWrapper(Color.red);
	private boolean		fill	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * For some reason, ObjectDB wants a no-arg constructor with this class...
	 */
	@SuppressWarnings("unused")
	private DrawableCircle()
	{
		super(new Circle(AVector2.ZERO_VECTOR, 1));
	}
	
	
	/**
	 * @param circle
	 */
	public DrawableCircle(final ICircle circle)
	{
		super(circle);
	}
	
	
	/**
	 * @param circle
	 * @param color
	 */
	public DrawableCircle(final ICircle circle, final Color color)
	{
		super(circle);
		this.color = new ColorWrapper(color);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param color
	 */
	public DrawableCircle(final IVector2 center, final float radius, final Color color)
	{
		super(center, radius);
		this.color = new ColorWrapper(color);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 center = fieldPanel.transformToGuiCoordinates(center(), invert);
		final float radius = fieldPanel.scaleXLength(radius());
		
		g.setColor(getColor());
		g.setStroke(new BasicStroke(1));
		g.drawOval((int) (center.x() - radius), (int) (center.y() - radius), (int) radius * 2, (int) radius * 2);
		if (fill)
		{
			g.fillOval((int) (center.x() - radius), (int) (center.y() - radius), (int) radius * 2, (int) radius * 2);
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
		// this may happen with old databases
		if (color == null)
		{
			// standard color
			return Color.red;
		}
		return color.getColor();
	}
	
	
	/**
	 * @param color the color to set
	 */
	public void setColor(final Color color)
	{
		this.color = new ColorWrapper(color);
	}
	
	
	/**
	 * @param fill
	 */
	public void setFill(final boolean fill)
	{
		this.fill = fill;
	}
}
