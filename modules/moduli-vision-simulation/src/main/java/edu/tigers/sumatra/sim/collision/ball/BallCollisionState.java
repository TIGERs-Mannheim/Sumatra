/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;

import java.util.Optional;


/**
 *
 */
public class BallCollisionState implements ICollisionState
{
	private ICollision collision = null;
	private BallState ballState;


	/**
	 * @param state
	 */
	public BallCollisionState(final BallState state)
	{
		ballState = state;
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


	@Override
	public IVector3 getPos()
	{
		return ballState.getPos();
	}


	public void setPos(IVector3 pos)
	{
		ballState = ballState.toBuilder()
				.withPos(pos)
				.build();
	}


	@Override
	public IVector3 getVel()
	{
		return ballState.getVel();
	}


	public void setVel(IVector3 vel)
	{
		ballState = ballState.toBuilder()
				.withVel(vel)
				.build();
	}


	@Override
	public IVector3 getAcc()
	{
		return ballState.getAcc();
	}


	public void setSpin(IVector2 spin)
	{
		ballState = ballState.toBuilder()
				.withSpin(spin)
				.build();
	}


	@Override
	public IVector2 getSpin()
	{
		return ballState.getSpin();
	}
}
