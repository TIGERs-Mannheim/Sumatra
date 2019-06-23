/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * A simple drawable point
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Embeddable
public class DrawablePoint extends Vector2f implements IDrawableShape
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long	serialVersionUID	= 4412181486791350865L;
	private static int			pointSize			= 4;
	private Color					color					= Color.red;
	private String					text					= "";
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
	public void paintShape(Graphics2D g)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 transPoint = FieldPanel.transformToGuiCoordinates(this);
		final int drawingX = (int) transPoint.x() - pointSize;
		final int drawingY = (int) transPoint.y() - pointSize;
		
		g.setColor(getColor());
		g.fillOval(drawingX, drawingY, pointSize, pointSize);
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
		return color;
	}
	
	
	/**
	 * @param color the color to set
	 */
	public void setColor(Color color)
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
	public final void setText(String text)
	{
		this.text = text;
	}
	
	
	/**
	 * @param size of the point
	 * 
	 */
	public void setSize(int size)
	{
		pointSize = size;
	}
}
