/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICollision
{
	
	/**
	 * @return the pos
	 */
	IVector2 getPos();
	
	
	/**
	 * @return
	 */
	IVector3 getObjectVel();
	
	
	/**
	 * @return the normal
	 */
	IVector2 getNormal();
	
	
	/**
	 * @return if the ball should stick at obstacle (dribbler)
	 */
	boolean isSticky();
	
	
	/**
	 * @return the object the ball has collide with
	 */
	ICollisionObject getObject();
}
