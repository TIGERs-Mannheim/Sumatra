/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * A simple circle-based obstacle
 */
@Persistent
public class GenericCircleObstacle extends AObstacle
{
	private final ICircle circle;
	private Color color = Color.black;


	@SuppressWarnings("unused") // required by Berkeley DB
	protected GenericCircleObstacle()
	{
		circle = Circle.from2Points(Vector2.zero(), Vector2.zero());
	}


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
	protected void initializeShapes()
	{
		shapes.add(new DrawableCircle(circle, color));
	}


	public void setColor(final Color color)
	{
		this.color = color;
	}
}
