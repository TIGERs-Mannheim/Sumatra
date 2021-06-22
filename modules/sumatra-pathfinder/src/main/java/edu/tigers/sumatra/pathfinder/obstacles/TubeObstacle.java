/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.util.List;

import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * An obstacle in a tube shape
 */
public class TubeObstacle extends AObstacle
{
	private final Tube tube;


	public TubeObstacle(final Tube tube)
	{
		this.tube = tube;
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return tube.isPointInShape(point, margin);
	}


	@Override
	protected void initializeShapes(final List<IDrawableShape> shapes)
	{
		shapes.add(new DrawableTube(tube));
	}
}
