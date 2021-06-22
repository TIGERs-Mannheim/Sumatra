/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;


/**
 * @author Chris
 */
@Persistent
public class DrawableValuePoints implements IDrawableShape
{
	private final List<ValuePoint> points = new ArrayList<>();
	private static final int RADIUS = 15;


	@SuppressWarnings("unused")
	private DrawableValuePoints()
	{
	}


	/**
	 * @param distancePoints
	 */
	public DrawableValuePoints(final List<ValuePoint> distancePoints)
	{
		points.addAll(distancePoints);

	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		IVector2 tempPos;
		for (ValuePoint point : points)
		{
			tempPos = tool.transformToGuiCoordinates(point.getXYVector(), invert);
			int colorVal = (int) (255. * point.getValue());
			g.setColor(
					new Color(colorVal % 256, colorVal % 256, colorVal % 256, 150));
			g.fillOval((int) tempPos.x() - (RADIUS / 2), (int) tempPos.y() - (RADIUS / 2), RADIUS, RADIUS);
			g.setColor(Color.RED);
			g.drawString(Double.toString(point.getValue()), (int) tempPos.x(), (int) tempPos.y());
		}
	}
}
