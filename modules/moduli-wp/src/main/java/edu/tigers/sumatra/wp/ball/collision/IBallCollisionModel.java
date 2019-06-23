/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import edu.tigers.sumatra.wp.ball.dynamics.IState;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@FunctionalInterface
public interface IBallCollisionModel
{
	/**
	 * @param state
	 * @param dt
	 * @param context
	 * @return
	 */
	ICollisionState processCollision(final IState state, final double dt,
			final MotionContext context);
}
