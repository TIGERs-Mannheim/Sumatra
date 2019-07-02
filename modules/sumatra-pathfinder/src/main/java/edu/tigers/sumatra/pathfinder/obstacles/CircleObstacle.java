/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A simple circle-based obstacle.
 * Note: Use {@link GenericCircleObstacle} instead. This class is only kept for Berkeley DB support for a while.
 */
@Persistent
public class CircleObstacle extends Circle implements IObstacle
{
	private Color color = Color.red;


	@SuppressWarnings("unused") // required by Berkeley DB
	private CircleObstacle()
	{
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return isPointInShape(point, margin);
	}


	@Override
	public List<IDrawableShape> getShapes()
	{
		return Collections.singletonList(new DrawableCircle(this, color));
	}
}
