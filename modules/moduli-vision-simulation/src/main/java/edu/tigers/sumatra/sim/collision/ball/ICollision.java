/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 *
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
	IVector2 getObjectSurfaceVel();


	/**
	 * @return the normal
	 */
	IVector2 getNormal();


	/**
	 * @return the object the ball has collided with
	 */
	ICollisionObject getObject();
}
