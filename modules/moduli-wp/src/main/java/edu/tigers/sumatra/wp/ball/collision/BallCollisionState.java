/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import java.util.Optional;

import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.wp.ball.BallState;
import edu.tigers.sumatra.wp.ball.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallCollisionState extends BallState implements ICollisionState
{
	private Optional<ICollision> collision = Optional.empty();
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param accTorque
	 */
	public BallCollisionState(final IVector3 pos, final IVector3 vel, final IVector3 acc, final IVector3 accTorque)
	{
		super(pos, vel, acc, accTorque);
	}
	
	
	/**
	 * @param state
	 */
	public BallCollisionState(final ICollisionState state)
	{
		super(state);
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
		return collision;
	}
	
	
	/**
	 * @param collision the collision to set
	 */
	public void setCollision(final Optional<ICollision> collision)
	{
		this.collision = collision;
	}
}
