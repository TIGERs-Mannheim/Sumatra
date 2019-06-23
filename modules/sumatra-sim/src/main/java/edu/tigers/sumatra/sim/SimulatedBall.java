/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.collision.BallCollisionModel;
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
	
	private BallTrajectoryState state;
	
	private final Object sync = new Object();
	
	
	/**
	 * Default
	 */
	public SimulatedBall()
	{
		dynamicsModel = new BallDynamicsModelDynamicVelFixedLoss();
		collisionModel = new BallCollisionModel();
		state = BallTrajectoryState.aBallState()
				.withPos(AVector3.ZERO_VECTOR)
				.withVel(AVector3.ZERO_VECTOR)
				.withAcc(AVector3.ZERO_VECTOR)
				.withChipped(false)
				.withVSwitchToRoll(1.0)
				.build();
	}
	
	
	@Override
	public void step(final double dt, final MotionContext context)
	{
		synchronized (sync)
		{
			BallState bState = new BallState(state.getPos(), state.getVel().multiplyNew(1e-3),
					state.getAcc().multiplyNew(1e-3));
			ICollisionState colState = collisionModel.processCollision(bState, dt, context);
			
			IVector3 obtainedImpulse = colState.getVel().subtractNew(bState.getVel()).multiply(1e3);
			
			state = BallTrajectoryState.aBallState()
					.withPos(colState.getPos())
					.withVel(colState.getVel().multiplyNew(1e3))
					.withAcc(colState.getAcc().multiplyNew(1e3))
					.withChipped(state.isChipped())
					.withVSwitchToRoll(state.getvSwitchToRoll())
					.build();
			
			BallTrajectoryState newState = dynamicsModel.dynamics(state, dt, obtainedImpulse);
			
			if (!Geometry.getFieldWBorders().isPointInShape(newState.getPos().getXYVector()))
			{
				newState = BallTrajectoryState.aBallState()
						.withPos(Vector3.from2d(
								Geometry.getField().withMargin(-100).nearestPointInside(newState.getPos().getXYVector()),
								0))
						.withVel(AVector3.ZERO_VECTOR)
						.withAcc(AVector3.ZERO_VECTOR)
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
}
