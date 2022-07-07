/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * Drawable of a circle
 */
@Persistent
public class DrawableCircle extends ADrawableWithStroke
{
	private ICircle circle;
	private boolean fill = false;


	/**
	 * For some reason, ObjectDB wants a no-arg constructor with this class...
	 */
	@SuppressWarnings("unused")
	private DrawableCircle()
	{
		circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);
	}


	/**
	 * @param circle
	 */
	public DrawableCircle(final ICircle circle)
	{
		this.circle = circle;
	}


	/**
	 * @param circle
	 * @param color
	 */
	public DrawableCircle(final ICircle circle, final Color color)
	{
		this.circle = circle;
		setColor(color);
	}


	/**
	 * @param center
	 * @param radius
	 */
	public DrawableCircle(final IVector2 center, final double radius)
	{
		circle = Circle.createCircle(center, radius);
	}


	/**
	 * @param center
	 * @param radius
	 * @param color
	 */
	public DrawableCircle(final IVector2 center, final double radius, final Color color)
	{
		circle = Circle.createCircle(center, radius);
		setColor(color);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 center = tool.transformToGuiCoordinates(circle.center(), invert);
		final double radius = tool.scaleGlobalToGui(circle.radius());

		if (fill)
		{
			g.fillOval((int) (center.x() - radius), (int) (center.y() - radius), (int) radius * 2, (int) radius * 2);
		} else
		{
			g.drawOval((int) (center.x() - radius), (int) (center.y() - radius), (int) radius * 2, (int) radius * 2);
		}
	}


	@Override
	public DrawableCircle setFill(final boolean fill)
	{
		this.fill = fill;
		return this;
	}
}
