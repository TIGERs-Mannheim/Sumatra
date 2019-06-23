/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.dynamics;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.BallTrajectoryState;


/**
 * Ball dynamics model for simulation.
 * Uses two phase dynamic vel for flat balls and fixed loss plus rolling for chipped balls.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author AndreR <andre@ryll.cc>
 */
public class BallDynamicsModelDynamicVelFixedLoss
{
	/**
	 * @param state
	 * @param dt [s]
	 * @param obtainedImpulse [mm/s]
	 * @return
	 */
	public BallTrajectoryState dynamics(final BallTrajectoryState state, final double dt, final IVector3 obtainedImpulse)
	{
		double vSwitch = state.getvSwitchToRoll();
		boolean chipped = state.isChipped();
		double v = state.getVel().getLength2();
		double velChange = obtainedImpulse.getLength();
		
		IVector3 pos = state.getPos();
		IVector3 vel = state.getVel();
		IVector3 acc = state.getAcc();
		
		if (velChange > 0)
		{
			// kicked
			if (obtainedImpulse.z() > 0)
			{
				// chipped
				chipped = true;
			} else
			{
				// straight
				vSwitch = v * Geometry.getBallParameters().getkSwitch();
				pos = Vector3.from2d(pos.getXYVector(), 0);
				vel = Vector3.from2d(vel.getXYVector(), 0);
				acc = Vector3.from2d(acc.getXYVector(), 0);
				chipped = false;
			}
		}
		
		if (velChange < 0)
		{
			// deflected
			vSwitch = v;
		}
		
		if (!Double.isFinite(state.getPos().x()) || !Double.isFinite(state.getPos().y())
				|| !Double.isFinite(state.getPos().z()))
		{
			pos = AVector3.ZERO_VECTOR;
			vel = AVector3.ZERO_VECTOR;
			acc = AVector3.ZERO_VECTOR;
		}
		
		BallTrajectoryState bs = BallTrajectoryState.aBallState()
				.withPos(pos)
				.withVel(vel)
				.withAcc(acc)
				.withChipped(chipped)
				.withVSwitchToRoll(vSwitch)
				.build();
		
		ABallTrajectory traj = BallFactory.createTrajectory(bs);
		
		return traj.getMilliStateAtTime(traj.gettKickToNow() + dt);
	}
}
