/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import java.util.Optional;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICollisionObject
{
	IVector3 getVel();
	
	
	default IVector2 getAcc()
	{
		return Vector2f.ZERO_VECTOR;
	}
	
	
	/**
	 * @return if the ball should stick on the obstacle
	 */
	default boolean isSticky()
	{
		return false;
	}
	
	
	default double getDampFactor()
	{
		return 0.5;
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
		return Vector3f.ZERO_VECTOR;
	}
	
	
	/**
	 * @return the bot id of the colliding robot or no_bot
	 */
	default BotID getBotID()
	{
		return BotID.noBot();
	}
}
