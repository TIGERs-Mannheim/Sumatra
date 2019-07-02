/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.pathfinder.obstacles;

import java.util.List;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Interface for all obstacles for path planning
 */
public interface IObstacle
{
	/**
	 * Check if the given point is colliding with this obstacle at the given time.
	 *
	 * @param point the point to check
	 * @param t the time in future
	 * @return true, if a collision was detected
	 */
	default boolean isPointCollidingWithObstacle(IVector2 point, double t)
	{
		return isPointCollidingWithObstacle(point, t, 0.0);
	}


	/**
	 * Check if the given point is colliding with this obstacle at the given time.
	 *
	 * @param point the point to check
	 * @param t the time in future
	 * @param margin an extra margin to apply around this obstacle
	 * @return true, if a collision was detected
	 */
	boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin);


	/**
	 * @return true, if this is a critical obstacle that we really do not want to enter
	 */
	default boolean isCritical()
	{
		return false;
	}


	List<IDrawableShape> getShapes();
}
