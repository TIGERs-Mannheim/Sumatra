/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball;

import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
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
