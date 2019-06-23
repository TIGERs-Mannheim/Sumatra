/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.sim;

import java.util.Optional;

import edu.tigers.sumatra.wp.ball.collision.ICollision;
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
	
	
	/**
	 * @return the last collision with the ball, if present
	 */
	Optional<ICollision> getLastCollision();
}
