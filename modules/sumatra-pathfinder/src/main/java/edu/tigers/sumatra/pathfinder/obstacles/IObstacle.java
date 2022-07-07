/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


/**
 * Interface for all obstacles for path planning
 */
public interface IObstacle
{
	/**
	 * Check if the given point is colliding with this obstacle at the given time.
	 *
	 * @param point  the point to check
	 * @param t      the time in future
	 * @param margin an extra margin to apply around this obstacle
	 * @return true, if a collision was detected
	 */
	boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin);


	/**
	 * @return true, if this is a critical obstacle that we really do not want to enter
	 */
	default boolean isEmergencyBrakeFor()
	{
		return false;
	}

	/**
	 * @return true, if this is an obstacle that we want to brake for if already inside
	 */
	default boolean isBrakeInside()
	{
		return false;
	}


	/**
	 * @return true, if the bot should evade this obstacle actively, i.e. even if standing on the destination already
	 */
	default boolean isActivelyEvade()
	{
		return false;
	}


	/**
	 * Calculate a collision penalty for this specific obstacle for the line between from and to.
	 *
	 * @param from starting point
	 * @param to   end point
	 * @return a penalty score (higher is worse)
	 */
	default double collisionPenalty(final IVector2 from, final IVector2 to)
	{
		return 0;
	}


	List<IDrawableShape> getShapes();

	/**
	 * @return the priority of the obstacle (higher will be considered before smaller)
	 */
	default int getPriority()
	{
		return 0;
	}
}
