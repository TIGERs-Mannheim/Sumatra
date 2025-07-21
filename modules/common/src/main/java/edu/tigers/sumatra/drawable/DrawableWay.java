/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Drawable way.
 */
public class DrawableWay extends ADrawableWithStroke
{
	private final List<IVector2> path;


	public DrawableWay(final Collection<IVector2> path)
	{
		// make sure we have a persistent implementation of List
		this.path = new ArrayList<>(path);
	}


	/**
	 * @param path
	 * @param color
	 */
	public DrawableWay(final Collection<IVector2> path, final Color color)
	{
		this(path);
		setColor(color);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		if (path.isEmpty())
		{
			return;
		}

		final GeneralPath drawPath = new GeneralPath();

		IVector2 startPos = path.get(0);
		final IVector2 transBotPos = tool.transformToGuiCoordinates(startPos, invert);
		final int robotX = (int) transBotPos.x();
		final int robotY = (int) transBotPos.y();
		drawPath.moveTo(robotX, robotY);

		for (IVector2 point : path)
		{
			final IVector2 transPathPoint = tool.transformToGuiCoordinates(point, invert);
			drawPath.lineTo((int) transPathPoint.x(), (int) transPathPoint.y());
		}
		g.draw(drawPath);
	}
}
