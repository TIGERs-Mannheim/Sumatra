/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * A Rectangle with a color
 */
public class DrawableRectangle extends ADrawableWithStroke
{
	private IRectangle rectangle;
	private boolean fill = false;


	/**
	 * @param rec
	 */
	public DrawableRectangle(final IRectangle rec)
	{
		rectangle = rec;
		setColor(Color.black);
	}


	/**
	 * A Rectangle that can be drawn, duh!
	 *
	 * @param rec
	 * @param color
	 */
	public DrawableRectangle(final IRectangle rec, final Color color)
	{
		rectangle = rec;
		setColor(color);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		IVector2 topLeftGlobal = rectangle.center()
				.addNew(Vector2.fromXY(-rectangle.xExtent() / 2, -rectangle.yExtent() / 2));
		IVector2 bottomRightGlobal = rectangle.center()
				.addNew(Vector2.fromXY(rectangle.xExtent() / 2, rectangle.yExtent() / 2));

		final IVector2 topLeft = tool.transformToGuiCoordinates(topLeftGlobal, invert);
		final IVector2 bottomRight = tool.transformToGuiCoordinates(bottomRightGlobal, invert);

		int x = (int) (Math.min(topLeft.x(), bottomRight.x()));
		int y = (int) (Math.min(topLeft.y(), bottomRight.y()));

		int width = Math.abs((int) (bottomRight.x() - topLeft.x()));
		int height = Math.abs((int) (bottomRight.y() - topLeft.y()));

		g.drawRect(x, y, width, height);
		if (fill)
		{
			g.fillRect(x, y, width, height);
		}
	}


	@Override
	public DrawableRectangle setFill(final boolean fill)
	{
		this.fill = fill;
		return this;
	}
}
