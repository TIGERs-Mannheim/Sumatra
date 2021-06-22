/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.dynamics.ball;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * Ball dynamics model for simulation.
 * Uses two phase dynamic vel for flat balls and fixed loss plus rolling for chipped balls.
 */
public class BallDynamicsModelDynamicVelFixedLoss
{
	/**
	 * @param state the current state
	 * @param dt    [s]
	 * @return the new state
	 */
	public BallState dynamics(final BallState state, final double dt)
	{
		var pos = state.getPos();
		var vel = state.getVel();
		var acc = state.getAcc();
		var spin = state.getSpin();

		if (!Double.isFinite(state.getPos().x()) || !Double.isFinite(state.getPos().y())
				|| !Double.isFinite(state.getPos().getXYZVector().z()))
		{
			pos = Vector3f.ZERO_VECTOR;
			vel = Vector3f.ZERO_VECTOR;
			acc = Vector3f.ZERO_VECTOR;
			spin = Vector2f.ZERO_VECTOR;
		}

		var safeState = BallState.builder()
				.withPos(pos)
				.withVel(vel)
				.withAcc(acc)
				.withSpin(spin)
				.build();

		var traj = Geometry.getBallFactory().createTrajectoryFromState(safeState);

		return traj.getMilliStateAtTime(dt);
	}
}
