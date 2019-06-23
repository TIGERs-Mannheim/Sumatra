/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import java.util.Optional;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.wp.ball.dynamics.BallState;
import edu.tigers.sumatra.wp.ball.dynamics.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallCollisionState extends BallState implements ICollisionState
{
	private ICollision collision = null;
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param acc
	 */
	public BallCollisionState(final IVector3 pos, final IVector3 vel, final IVector3 acc)
	{
		super(pos, vel, acc);
	}
	
	
	/**
	 * @param state
	 */
	public BallCollisionState(final ICollisionState state)
	{
		super(state);
		this.collision = state.getCollision().orElse(null);
	}
	
	
	/**
	 * @param state
	 */
	public BallCollisionState(final IState state)
	{
		super(state);
	}
	
	
	/**
	 * @return the collision
	 */
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
