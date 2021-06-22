/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Drawable path, consisting of several points.
 */
@Persistent
public class DrawablePath extends ADrawableWithStroke
{
	private final List<IVector2> path;


	@SuppressWarnings("unused")
	private DrawablePath()
	{
		this(new ArrayList<>());
	}


	/**
	 * @param path
	 */
	public DrawablePath(final List<IVector2> path)
	{
		this(path, Color.red);
	}


	/**
	 * @param path
	 * @param color
	 */
	public DrawablePath(final List<IVector2> path, final Color color)
	{
		this.path = new ArrayList<>(path);
		setColor(color);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		final GeneralPath drawPath = new GeneralPath();
		IVector2 startPos = path.get(0);
		final IVector2 transBotPos = tool.transformToGuiCoordinates(startPos, invert);
		final int robotX = (int) transBotPos.x();
		final int robotY = (int) transBotPos.y();
		drawPath.moveTo(robotX, robotY);

		for (IVector2 point : path)
		{
			final IVector2 transPathPoint = tool.transformToGuiCoordinates(point, invert);
			g.drawOval((int) transPathPoint.x() - 1, (int) transPathPoint.y() - 1, 3, 3);
			drawPath.lineTo((int) transPathPoint.x(), (int) transPathPoint.y());
		}
		g.draw(drawPath);
	}
}
