/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A simple circle-based obstacle
 */
public class GenericCircleObstacle extends AObstacle
{
	private final ICircle circle;
	private Color color = Color.black;


	public GenericCircleObstacle(final ICircle circle)
	{
		this.circle = circle;
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return circle.isPointInShape(point, margin);
	}


	@Override
	protected void initializeShapes(final List<IDrawableShape> shapes)
	{
		shapes.add(new DrawableCircle(circle, color));
	}


	public void setColor(final Color color)
	{
		this.color = color;
	}
}
