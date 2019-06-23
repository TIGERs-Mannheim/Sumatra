/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.ball.collision;

import java.util.Optional;

import edu.tigers.sumatra.wp.ball.dynamics.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICollisionState extends IState
{
	/**
	 * @return
	 */
	Optional<ICollision> getCollision();
}
