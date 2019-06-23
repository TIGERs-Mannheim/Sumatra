/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * Drawable of a circle
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Embeddable
public class DrawableCircle extends Circle implements IDrawableShape
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private Color	color	= Color.red;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
		this.color = color;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void paintShape(Graphics2D g)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 center = FieldPanel.transformToGuiCoordinates(center());
		final float radius = FieldPanel.scaleXLength(radius());
		
		g.setColor(getColor());
		g.setStroke(new BasicStroke(1));
		g.drawOval((int) (center.x() - radius), (int) (center.y() - radius), (int) radius * 2, (int) radius * 2);
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
	public void setColor(Color color)
	{
		this.color = color;
	}
}
