/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.quadrilateral.IQuadrilateral;
import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * Drawable quadrilateral.
 */
public class DrawableQuadrilateral extends ADrawableWithStroke
{

	private final IQuadrilateral quadrilateral;

	private boolean fill = false;


	/**
	 * Create a drawable for a Quadrilateral.
	 *
	 * @param quadrilateral backed by this drawable
	 * @param color
	 */
	public DrawableQuadrilateral(IQuadrilateral quadrilateral, Color color)
	{
		this.quadrilateral = quadrilateral;
		setColor(color);
	}


	/**
	 * @param quadrilateral backed by this drawable
	 */
	public DrawableQuadrilateral(final IQuadrilateral quadrilateral)
	{
		this(quadrilateral, Color.BLACK);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		assert quadrilateral != null;
		for (ILineSegment line : quadrilateral.getEdges())
		{
			final IVector2 lineStart = tool.transformToGuiCoordinates(line.getPathStart(), invert);
			final IVector2 lineEnd = tool.transformToGuiCoordinates(line.getPathEnd(), invert);
			g.drawLine((int) lineStart.x(), (int) lineStart.y(), (int) lineEnd.x(), (int) lineEnd.y());
		}
		if (fill)
		{
			int[] x = new int[quadrilateral.getCorners().size()];
			int[] y = new int[quadrilateral.getCorners().size()];
			for (int i = 0; i < quadrilateral.getCorners().size(); i++)
			{
				IVector2 tmp = tool.transformToGuiCoordinates(quadrilateral.getCorners().get(i), invert);
				x[i] = (int) tmp.x();
				y[i] = (int) tmp.y();
			}
			g.fillPolygon(x, y, x.length);
		}
	}


	@Override
	public DrawableQuadrilateral setFill(final boolean fill)
	{
		this.fill = fill;
		return this;
	}
}
