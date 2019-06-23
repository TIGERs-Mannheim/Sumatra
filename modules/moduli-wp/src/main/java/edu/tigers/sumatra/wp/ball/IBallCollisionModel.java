/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball;

import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.wp.ball.collision.ICollisionState;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IBallCollisionModel
{
	/**
	 * @param state
	 * @param newState
	 * @param dt
	 * @param context
	 * @return
	 */
	ICollisionState processCollision(final IState state, final IState newState, final double dt,
			final MotionContext context);
	
	
	/**
	 * @param state
	 * @param context
	 * @return vel in [m/s]
	 */
	IVector3 getImpulse(ICollisionState state, final MotionContext context);
	
	
	/**
	 * @param state
	 * @param context
	 * @return
	 */
	IVector3 getTorqueAcc(IState state, MotionContext context);
}
