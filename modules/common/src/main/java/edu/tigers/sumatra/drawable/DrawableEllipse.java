/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.ellipse.Ellipse;
import edu.tigers.sumatra.math.ellipse.IEllipse;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * This is the drawable ellipse that fits to the normal Ellipse
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class DrawableEllipse extends ADrawableWithStroke
{
	private IEllipse	ellipse;
	private boolean	fill	= false;
	
	
	@SuppressWarnings("unused")
	private DrawableEllipse()
	{
		ellipse = Ellipse.createEllipse(Vector2f.ZERO_VECTOR, 1, 1);
	}
	
	
	/**
	 * @param ellipse
	 * @param color
	 */
	public DrawableEllipse(final IEllipse ellipse, final Color color)
	{
		this.ellipse = ellipse;
		setColor(color);
	}
	
	
	/**
	 * @param ellipse
	 */
	public DrawableEllipse(final IEllipse ellipse)
	{
		this.ellipse = ellipse;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);
		
		Rectangle r = getBounds(tool, invert);
		
		double rot = tool.getFieldTurn().getAngle() + ellipse.getTurnAngle();
		
		g.rotate(-rot, r.x + (r.width / 2.0), r.y + (r.height / 2.0));
		if (fill)
		{
			g.fillOval(r.x, r.y, r.width, r.height);
		} else
		{
			g.drawOval(r.x, r.y, r.width, r.height);
		}
		g.rotate(rot, r.x + (r.width / 2.0), r.y + (r.height / 2.0));
	}
	
	
	private Rectangle getBounds(final IDrawableTool fieldPanel, final boolean invert)
	{
		IVector2 center = fieldPanel.transformToGuiCoordinates(ellipse.center(), invert);
		int x = (int) (center.x() - fieldPanel.scaleXLength(ellipse.getRadiusY()));
		int y = (int) (center.y() - fieldPanel.scaleYLength(ellipse.getRadiusX()));
		int width = fieldPanel.scaleXLength(ellipse.getRadiusY()) * 2;
		int height = fieldPanel.scaleYLength(ellipse.getRadiusX()) * 2;
		return new Rectangle(x, y, width, height);
	}
	
	
	@Override
	public void setFill(final boolean fill)
	{
		this.fill = fill;
	}
	
}
