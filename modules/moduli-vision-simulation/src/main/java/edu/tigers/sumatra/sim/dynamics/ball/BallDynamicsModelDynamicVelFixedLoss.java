/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.dynamics.ball;

import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.BallTrajectoryState;


/**
 * Ball dynamics model for simulation.
 * Uses two phase dynamic vel for flat balls and fixed loss plus rolling for chipped balls.
 */
public class BallDynamicsModelDynamicVelFixedLoss
{
	/**
	 * @param state the current state
	 * @param dt [s]
	 * @return the new state
	 */
	public BallTrajectoryState dynamics(final BallTrajectoryState state, final double dt)
	{
		double vSwitch = state.getvSwitchToRoll();
		boolean chipped = state.isChipped();
		
		IVector pos = state.getPos();
		IVector vel = state.getVel();
		IVector acc = state.getAcc();
		
		if (!Double.isFinite(state.getPos().x()) || !Double.isFinite(state.getPos().y())
				|| !Double.isFinite(state.getPos().getXYZVector().z()))
		{
			pos = Vector3f.ZERO_VECTOR;
			vel = Vector3f.ZERO_VECTOR;
			acc = Vector3f.ZERO_VECTOR;
		}
		
		BallTrajectoryState bs = BallTrajectoryState.aBallState()
				.withPos(pos)
				.withVel(vel)
				.withAcc(acc)
				.withChipped(chipped)
				.withVSwitchToRoll(vSwitch)
				.withSpin(state.getSpin())
				.build();
		
		ABallTrajectory traj = BallFactory.createTrajectory(bs);
		
		return traj.getMilliStateAtTime(traj.gettKickToNow() + dt);
	}
}
