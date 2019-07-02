/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import java.util.Optional;

import edu.tigers.sumatra.sim.dynamics.ball.BallState;
import edu.tigers.sumatra.sim.dynamics.ball.IState;


/**
 */
public class BallCollisionState extends BallState implements ICollisionState
{
	private ICollision collision = null;
	
	
	/**
	 * @param state
	 */
	public BallCollisionState(final IState state)
	{
		super(state);
	}
	
	
	@Override
	public Optional<ICollision> getCollision()
	{
		return Optional.ofNullable(collision);
	}
	
	
	/**
	 * @param collision the collision to set
	 */
	public void setCollision(final ICollision collision)
	{
		this.collision = collision;
	}
}
