/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.dynamics;

import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@FunctionalInterface
public interface IBallDynamicsModel
{
	/**
	 * @param state
	 * @param action
	 * @param dt
	 * @param context
	 * @return
	 */
	IState dynamics(final IState state, final IAction action, final double dt,
			final MotionContext context);
}
