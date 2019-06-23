/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * A simple drawable point
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawablePoint extends Vector2f implements IDrawableShape
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long	serialVersionUID	= 4412181486791350865L;
	private static int			pointSize			= 4;
	private ColorWrapper			color					= new ColorWrapper(Color.red);
	private String					text					= "";
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
		this.color = new ColorWrapper(color);
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
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 transPoint = fieldPanel.transformToGuiCoordinates(this, invert);
		final int drawingX = (int) transPoint.x() - (pointSize / 2);
		final int drawingY = (int) transPoint.y() - (pointSize / 2);
		
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
