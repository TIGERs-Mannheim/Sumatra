/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import java.util.Optional;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.wp.ball.collision.BallCollisionModel;
import edu.tigers.sumatra.wp.ball.collision.ICollision;
import edu.tigers.sumatra.wp.ball.collision.ICollisionState;
import edu.tigers.sumatra.wp.ball.dynamics.BallDynamicsModelDynamicVelFixedLoss;
import edu.tigers.sumatra.wp.ball.dynamics.BallState;
import edu.tigers.sumatra.wp.data.BallTrajectoryState;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * Ball simulation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulatedBall implements ISimulatedBall
{
	private final BallDynamicsModelDynamicVelFixedLoss dynamicsModel;
	private final BallCollisionModel collisionModel;
	private ICollision lastCollision;
	
	private BallTrajectoryState state;
	
	private final SimulatedBallSync sync = new SimulatedBallSync()
	{
	};
	
	
	/**
	 * Default
	 */
	public SimulatedBall()
	{
		dynamicsModel = new BallDynamicsModelDynamicVelFixedLoss();
		collisionModel = new BallCollisionModel();
		state = BallTrajectoryState.aBallState()
				.withPos(Vector3f.ZERO_VECTOR)
				.withVel(Vector3f.ZERO_VECTOR)
				.withAcc(Vector3f.ZERO_VECTOR)
				.withChipped(false)
				.withVSwitchToRoll(1.0)
				.build();
	}
	
	
	@Override
	public void step(final double dt, final MotionContext context)
	{
		synchronized (sync)
		{
			BallState bState = new BallState(state.getPos().getXYZVector(),
					state.getVel().multiplyNew(1e-3).getXYZVector(),
					state.getAcc().multiplyNew(1e-3).getXYZVector());
			ICollisionState colState = collisionModel.processCollision(bState, dt, context);
			
			IVector3 obtainedImpulse = colState.getVel().subtractNew(bState.getVel()).multiply(1e3);
			IVector2 acc = Vector2f.ZERO_VECTOR;
			if (colState.getCollision().isPresent())
			{
				lastCollision = colState.getCollision().get();
				acc = lastCollision.getObject().getAcc();
			} else if (!Geometry.getField().isPointInShape(state.getPos().getXYVector()))
			{
				lastCollision = null;
			}
			
			state = BallTrajectoryState.aBallState()
					.withPos(colState.getPos())
					.withVel(colState.getVel().multiplyNew(1e3))
					.withAcc(colState.getAcc().addNew(acc.getXYZVector()).multiplyNew(1e3))
					.withChipped(state.isChipped())
					.withVSwitchToRoll(state.getvSwitchToRoll())
					.withSpin(state.getSpin())
					.build();
			
			BallTrajectoryState newState = dynamicsModel.dynamics(state, dt, obtainedImpulse);
			
			if (!Geometry.getFieldWBorders().isPointInShape(newState.getPos().getXYVector()))
			{
				newState = BallTrajectoryState.aBallState()
						.withPos(Vector3.from2d(
								Geometry.getField().withMargin(-100).nearestPointInside(newState.getPos().getXYVector()),
								0))
						.withVel(Vector3f.ZERO_VECTOR)
						.withAcc(Vector3f.ZERO_VECTOR)
						.withChipped(false)
						.withVSwitchToRoll(1.0)
						.build();
			}
			
			state = newState;
		}
	}
	
	
	@Override
	public void setState(final BallTrajectoryState newState)
	{
		synchronized (sync)
		{
			state = newState;
		}
	}
	
	
	@Override
	public BallTrajectoryState getState()
	{
		return state;
	}
	
	
	@Override
	public Optional<ICollision> getLastCollision()
	{
		return Optional.ofNullable(lastCollision);
	}
	
	private interface SimulatedBallSync
	{
	}
}
