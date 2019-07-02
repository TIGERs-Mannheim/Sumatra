/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.sim.collision.ball;

import java.util.Optional;

import edu.tigers.sumatra.sim.dynamics.ball.IState;


/**
 */
public interface ICollisionState extends IState
{
	/**
	 * @return
	 */
	Optional<ICollision> getCollision();
}
