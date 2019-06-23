/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import java.util.Optional;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICollisionObject
{
	/**
	 * @return
	 */
	IVector3 getVel();
	
	
	/**
	 * @return if the ball should stick on the obstacle
	 */
	default boolean isSticky()
	{
		return false;
	}
	
	
	/**
	 * Get the collision information, if a collision is present.<br>
	 * A collision must be between pre and post pos. If both points are inside the obstacle, there is no collision.
	 * 
	 * @param prePos old pos
	 * @param postPos new pos
	 * @return collision information, if present
	 */
	Optional<ICollision> getCollision(IVector3 prePos, IVector3 postPos);
	
	
	/**
	 * Get the collision information, if pos is inside obstacle
	 *
	 * @param pos the current pos
	 * @return collision information, if present
	 */
	Optional<ICollision> getInsideCollision(IVector3 pos);
	
	
	/**
	 * @return the impulse to add to the ball on a collision
	 * @param prePos
	 */
	default IVector3 getImpulse(final IVector3 prePos)
	{
		return Vector3.ZERO_VECTOR;
	}
}
