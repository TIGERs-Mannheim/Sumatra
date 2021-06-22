/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * A Rectangle with a color
 */
@Persistent
public class DrawableTriangle extends ADrawableWithStroke
{
	private ITriangle triangle;
	private boolean fill = false;


	/**
	 * For db only
	 */
	@SuppressWarnings("unused")
	private DrawableTriangle()
	{
		triangle = Triangle.fromCorners(Vector2f.ZERO_VECTOR, Vector2.fromXY(1, 1), Vector2.fromAngle(0));
	}


	/**
	 * @param a
	 * @param b
	 * @param c
	 */
	public DrawableTriangle(final IVector2 a, final IVector2 b, final IVector2 c)
	{
		this(Triangle.fromCorners(a, b, c));
	}


	/**
	 * @param a
	 * @param b
	 * @param c
	 * @param color
	 */
	public DrawableTriangle(final IVector2 a, final IVector2 b, final IVector2 c, final Color color)
	{
		this(Triangle.fromCorners(a, b, c), color);
	}


	/**
	 * @param triangle
	 */
	public DrawableTriangle(final ITriangle triangle)
	{
		this(triangle, Color.black);
	}


	/**
	 * @param triangle
	 * @param color
	 */
	public DrawableTriangle(final ITriangle triangle, final Color color)
	{
		this.triangle = triangle;
		setColor(color);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		final IVector2 a = tool.transformToGuiCoordinates(triangle.getCorners().get(0), invert);
		final IVector2 b = tool.transformToGuiCoordinates(triangle.getCorners().get(1), invert);
		final IVector2 c = tool.transformToGuiCoordinates(triangle.getCorners().get(2), invert);

		int[] x = { (int) a.x(), (int) b.x(), (int) c.x() };
		int[] y = { (int) a.y(), (int) b.y(), (int) c.y() };
		int number = 3;

		g.drawPolygon(x, y, number);
		if (fill)
		{
			g.fillPolygon(x, y, number);
		}
	}


	@Override
	public DrawableTriangle setFill(final boolean fill)
	{
		this.fill = fill;
		return this;
	}


	public ITriangle getTriangle()
	{
		return triangle;
	}
}
