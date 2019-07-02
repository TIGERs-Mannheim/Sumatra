/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;


/**
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
	 * @return the object the ball has collide with
	 */
	ICollisionObject getObject();
}
