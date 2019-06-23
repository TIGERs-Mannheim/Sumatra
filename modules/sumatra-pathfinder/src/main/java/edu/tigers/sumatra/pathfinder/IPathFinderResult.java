/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import java.util.Optional;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * Interface for a result of any path finder
 */
@FunctionalInterface
public interface IPathFinderResult
{
	/**
	 * @return the score for this result (0 is best)
	 */
	default double getPenaltyScore()
	{
		return 0;
	}
	
	
	/**
	 * @return the resulting trajectory
	 */
	ITrajectory<IVector2> getTrajectory();
	
	
	/**
	 * @return collisionDurationFront
	 */
	default double getCollisionDurationFront()
	{
		return 0;
	}
	
	
	/**
	 * @return collisionDurationBack
	 */
	default double getCollisionDurationBack()
	{
		return 0.0;
	}
	
	
	/**
	 * @return get time until first collision accures (+INF, if no collision)
	 */
	default double getFirstCollisionTime()
	{
		return Double.POSITIVE_INFINITY;
	}
	
	
	/**
	 * @return the last time on the trajectory without a collision - this is the total trajectory time if there is no
	 *         collision
	 */
	default double getLastNonCollisionTime()
	{
		return getTrajectory().getTotalTime() - getCollisionDurationBack();
	}
	
	
	/**
	 * @return true, if the full path is colliding with obstacles
	 */
	default boolean isAlwaysColliding()
	{
		return getCollisionDurationFront() >= getLastNonCollisionTime();
	}
	
	
	/**
	 * @return if there is a collision on the path that is not in front or back of path
	 */
	default boolean hasIntermediateCollision()
	{
		return Double.isFinite(getFirstCollisionTime());
	}
	
	
	/**
	 * @return if there is a collision at the beginning of the path
	 */
	default boolean hasFrontCollision()
	{
		return getCollisionDurationFront() > 0;
	}
	
	
	/**
	 * @return if there is a collision at the end of the path
	 */
	default boolean hasBackCollision()
	{
		return getCollisionDurationBack() > 0;
	}
	
	
	/**
	 * @return the max lookahead for checking collisions
	 */
	default double getCollisionLookahead()
	{
		return 0;
	}
	
	
	/**
	 * @return the obstacle that causes the collision, if present
	 */
	default Optional<IObstacle> getCollider()
	{
		return Optional.empty();
	}
	
	
	/**
	 * @return true, if there is any collision on the path
	 */
	default boolean hasCollision()
	{
		return hasFrontCollision() || hasIntermediateCollision() || hasBackCollision();
	}
}
