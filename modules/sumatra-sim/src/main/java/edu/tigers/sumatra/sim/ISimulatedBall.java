/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.wp.data.BallTrajectoryState;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ISimulatedBall
{
	/**
	 * Reset state.
	 * 
	 * @param newState
	 */
	void setState(final BallTrajectoryState newState);
	
	
	/**
	 * Make a simulation step with given dt
	 * 
	 * @param dt
	 * @param context
	 */
	void step(final double dt, final MotionContext context);
	
	
	/**
	 * Get current ball state.
	 * 
	 * @return
	 */
	BallTrajectoryState getState();
}
