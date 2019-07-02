/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import java.util.Collection;
import java.util.Optional;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.sim.collision.ball.BallCollisionHandler;
import edu.tigers.sumatra.sim.collision.ball.ICollision;
import edu.tigers.sumatra.sim.collision.ball.ICollisionState;
import edu.tigers.sumatra.sim.dynamics.ball.BallDynamicsModelDynamicVelFixedLoss;
import edu.tigers.sumatra.sim.dynamics.ball.BallState;
import edu.tigers.sumatra.wp.data.BallTrajectoryState;


/**
 * Ball simulation
 */
public class SimulatedBall implements ISimulatedObject, ISimBall
{
	private final BallDynamicsModelDynamicVelFixedLoss dynamicsModel = new BallDynamicsModelDynamicVelFixedLoss();
	private final BallCollisionHandler ballCollisionHandler = new BallCollisionHandler();
	private BallTrajectoryState state;
	private final Collection<? extends ISimBot> bots;
	
	private ICollision lastCollision = null;
	
	
	SimulatedBall(final Collection<? extends ISimBot> bots)
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
		BallState bState = new BallState(
				state.getPos().getXYZVector(),
				state.getVel().getXYZVector(),
				state.getAcc().getXYZVector());
		ICollisionState colState = ballCollisionHandler.process(bState, dt, bots);
		
		IVector3 pos = colState.getPos();
		IVector3 vel = colState.getVel();
		IVector3 acc = colState.getAcc();
		boolean chipped = state.isChipped();
		double vSwitch = state.getvSwitchToRoll();
		
		IVector3 obtainedImpulse = colState.getVel().subtractNew(bState.getVel());
		final boolean ballGotImpulse = obtainedImpulse.getLength2() > 0;
		if (ballGotImpulse)
		{
			// kicked
			if (obtainedImpulse.z() > 0)
			{
				chipped = true;
			} else
			{
				chipped = false;
				vSwitch = vel.getXYVector().getLength2() * Geometry.getBallParameters().getkSwitch();
				pos = Vector3.from2d(pos.getXYVector(), 0);
				vel = Vector3.from2d(vel.getXYVector(), 0);
				acc = Vector3.from2d(acc.getXYVector(), 0);
			}
		}
		
		if (colState.getCollision().isPresent())
		{
			acc = acc.addNew(colState.getCollision().get().getObject().getAcc());
		}
		
		state = BallTrajectoryState.aBallState()
				.withPos(pos)
				.withVel(vel)
				.withAcc(acc)
				.withChipped(chipped)
				.withVSwitchToRoll(vSwitch)
				.withSpin(state.getSpin())
				.build();
		
		return colState.getCollision();
	}
	
	
	public Optional<ICollision> getLastCollision()
	{
		return Optional.ofNullable(lastCollision);
	}
	
	
	@Override
	public synchronized BallTrajectoryState getState()
	{
		return state;
	}
	
	
	synchronized void setState(final BallTrajectoryState state)
	{
		this.state = state;
	}
	
	
	synchronized void resetState()
	{
		state = BallTrajectoryState.aBallState()
				.withPos(Vector3f.ZERO_VECTOR)
				.withVel(Vector3f.ZERO_VECTOR)
				.withAcc(Vector3f.ZERO_VECTOR)
				.withChipped(false)
				.withVSwitchToRoll(1.0)
				.build();
	}
}
