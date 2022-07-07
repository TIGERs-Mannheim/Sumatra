/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.ellipse.Ellipse;
import edu.tigers.sumatra.math.ellipse.IEllipse;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;


/**
 * This is the drawable ellipse that fits to the normal Ellipse
 */
@Persistent(version = 1)
public class DrawableEllipse extends ADrawableWithStroke
{
	private IEllipse ellipse;
	private boolean fill = false;


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
		double rot = tool.transformToGuiAngle(ellipse.getTurnAngle(), invert);

		g.rotate(-rot, r.getCenterX(), r.getCenterY());
		if (fill)
		{
			g.fillOval(r.x, r.y, r.width, r.height);
		} else
		{
			g.drawOval(r.x, r.y, r.width, r.height);
		}
		g.rotate(rot, r.getCenterX(), r.getCenterY());
	}


	private Rectangle getBounds(final IDrawableTool fieldPanel, final boolean invert)
	{
		IVector2 center = fieldPanel.transformToGuiCoordinates(ellipse.center(), invert);
		int radiusX = fieldPanel.scaleGlobalToGui(ellipse.getRadiusX());
		int radiusY = fieldPanel.scaleGlobalToGui(ellipse.getRadiusY());
		int x = (int) (center.x() - radiusX);
		int y = (int) (center.y() - radiusY);
		int width = radiusX * 2;
		int height = radiusY * 2;
		return new Rectangle(x, y, width, height);
	}


	@Override
	public DrawableEllipse setFill(final boolean fill)
	{
		this.fill = fill;
		return this;
	}
}
