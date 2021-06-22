/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.sim.collision.ball.BallCollisionHandler;
import edu.tigers.sumatra.sim.collision.ball.ICollision;
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
	private BallState state;
	private final Collection<? extends ISimBot> bots;

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
		if (!Geometry.getField().isPointInShape(state.getPos().getXYVector()))
		{
			lastCollision = null;
		}
	}


	private synchronized Optional<ICollision> processBall(final double dt)
	{
		ICollisionState colState = ballCollisionHandler.process(state, dt, bots);

		IVector3 pos = colState.getPos();
		IVector3 vel = colState.getVel();
		IVector3 acc = colState.getAcc();

		IVector3 obtainedImpulse = colState.getVel().subtractNew(state.getVel());
		final boolean ballGotImpulse = obtainedImpulse.getLength2() > 0;
		if (ballGotImpulse && obtainedImpulse.z() <= 0)
		{
			// flat kick
			pos = Vector3.from2d(pos.getXYVector(), 0);
			vel = Vector3.from2d(vel.getXYVector(), 0);
			acc = Vector3.from2d(acc.getXYVector(), 0);
		}

		if (colState.getCollision().isPresent())
		{
			acc = acc.addNew(colState.getCollision().get().getObject().getAcc());
		}

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


	synchronized void resetState()
	{
		state = BallState.builder()
				.withPos(Vector3f.ZERO_VECTOR)
				.withVel(Vector3f.ZERO_VECTOR)
				.withAcc(Vector3f.ZERO_VECTOR)
				.withSpin(Vector2f.ZERO_VECTOR)
				.build();
	}
}
