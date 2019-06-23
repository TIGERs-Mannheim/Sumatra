/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IObstacle extends IDrawableShape
{
	/**
	 * @param point
	 * @param t the time in future
	 * @return
	 */
	boolean isPointCollidingWithObstacle(IVector2 point, double t);
	
	
	/**
	 * @param point
	 * @param t
	 * @param margin
	 * @return
	 */
	default boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return isPointCollidingWithObstacle(point, t);
	}
}
