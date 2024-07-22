/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.sim.collision.ball.BallCollisionHandler;
import edu.tigers.sumatra.sim.collision.ball.ICollision;
import edu.tigers.sumatra.sim.collision.ball.ICollisionObject;
import edu.tigers.sumatra.sim.collision.ball.ICollisionState;
import edu.tigers.sumatra.sim.dynamics.ball.BallDynamicsModelDynamicVelFixedLoss;

import java.util.Collection;
import java.util.Optional;


/**
 * Ball simulation
 */
public class SimulatedBall implements ISimulatedObject, ISimBall
{
	private final BallDynamicsModelDynamicVelFixedLoss dynamicsModel = new BallDynamicsModelDynamicVelFixedLoss();
	private final BallCollisionHandler ballCollisionHandler = new BallCollisionHandler();
	private final Collection<? extends ISimBot> bots;
	private BallState state;
	private ICollision lastCollision = null;


	public SimulatedBall(final Collection<? extends ISimBot> bots)
	{
		this.bots = bots;
		resetState();
	}


	@Override
	public synchronized void dynamics(final double dt)
	{
		state = dynamicsModel.dynamics(state, dt);
	}


	/**
	 * Process collisions
	 *
	 * @param dt the time horizon
	 */
	public void collision(final double dt)
	{
		final Optional<ICollision> collision = processBall(dt);

		lastCollision = collision.orElse(lastCollision);
	}


	private synchronized Optional<ICollision> processBall(final double dt)
	{
		ICollisionState colState = ballCollisionHandler.process(state, dt, bots);

		IVector3 pos = colState.getPos();
		IVector3 vel = colState.getVel();
		IVector3 acc = colState.getAcc();
		
		acc = acc.addNew(
				colState.getCollision()
						.map(ICollision::getObject)
						.map(ICollisionObject::getAcc)
						.orElse(Vector3f.ZERO_VECTOR)
		);

		state = BallState.builder()
				.withPos(pos)
				.withVel(vel)
				.withAcc(acc)
				.withSpin(colState.getSpin())
				.build();

		return colState.getCollision();
	}


	public Optional<ICollision> getLastCollision()
	{
		return Optional.ofNullable(lastCollision);
	}


	@Override
	public synchronized BallState getState()
	{
		return state;
	}


	synchronized void setState(final BallState state)
	{
		this.state = state;
	}


	private synchronized void resetState()
	{
		state = BallState.builder()
				.withPos(Vector3f.ZERO_VECTOR)
				.withVel(Vector3f.ZERO_VECTOR)
				.withAcc(Vector3f.ZERO_VECTOR)
				.withSpin(Vector2f.ZERO_VECTOR)
				.build();
	}
}
