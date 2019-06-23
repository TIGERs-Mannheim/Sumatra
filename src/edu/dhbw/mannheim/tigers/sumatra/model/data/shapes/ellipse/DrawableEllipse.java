/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 18, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ColorWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * This is the drawable ellipse that fits to the normal Ellipse
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class DrawableEllipse extends Ellipse implements IDrawableShape
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private ColorWrapper	color			= new ColorWrapper(Color.red);
	private IVector2		curveStart	= Vector2.ZERO_VECTOR;
	private float			curvelength	= 0;
	private boolean		fill			= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private DrawableEllipse()
	{
		super(AVector2.ZERO_VECTOR, 1, 1);
	}
	
	
	/**
	 * @param ellipse
	 * @param color
	 */
	public DrawableEllipse(final IEllipse ellipse, final Color color)
	{
		super(ellipse);
		this.color = new ColorWrapper(color);
	}
	
	
	/**
	 * @param ellipse
	 */
	public DrawableEllipse(final IEllipse ellipse)
	{
		super(ellipse);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		g.setStroke(new BasicStroke(1));
		// this may happen with old databases
		if (color == null)
		{
			// standard color
			g.setColor(Color.red);
		} else
		{
			g.setColor(color.getColor());
		}
		
		Rectangle r = getBounds(fieldPanel, invert);
		
		g.rotate(-getTurnAngle(), r.x + (r.width / 2), r.y + (r.height / 2));
		g.drawOval(r.x, r.y, r.width, r.height);
		if (fill)
		{
			g.fillOval(r.x, r.y, r.width, r.height);
		}
		g.rotate(getTurnAngle(), r.x + (r.width / 2), r.y + (r.height / 2));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the curvelength
	 */
	public final float getCurvelength()
	{
		return curvelength;
	}
	
	
	/**
	 * @return the curveStart
	 */
	public final IVector2 getCurveStart()
	{
		return curveStart;
	}
	
	
	/**
	 * Do not draw the complete ellipse, but only the curve specified by start and length
	 * 
	 * @param start point on the ellipse
	 * @param length how long should the curve be? may also be negative
	 */
	public final void setCurve(final IVector2 start, final float length)
	{
		curvelength = length;
		curveStart = start;
	}
	
	
	private Rectangle getBounds(final IFieldPanel fieldPanel, final boolean invert)
	{
		IVector2 center = fieldPanel.transformToGuiCoordinates(getCenter(), invert);
		int x = (int) (center.x() - fieldPanel.scaleXLength(getRadiusY()));
		int y = (int) (center.y() - fieldPanel.scaleYLength(getRadiusX()));
		int width = fieldPanel.scaleXLength(getRadiusY()) * 2;
		int height = fieldPanel.scaleYLength(getRadiusX()) * 2;
		return new Rectangle(x, y, width, height);
	}
	
	
	/**
	 * @param fill
	 */
	public void setFill(final boolean fill)
	{
		this.fill = fill;
	}
}
