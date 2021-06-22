/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.sim.dynamics.ball.IState;

import java.util.Optional;


/**
 *
 */
public interface ICollisionState extends IState
{
	/**
	 * @return
	 */
	Optional<ICollision> getCollision();
}
