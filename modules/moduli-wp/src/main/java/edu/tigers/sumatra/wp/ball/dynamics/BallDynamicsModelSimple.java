/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.dynamics;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * Simple ball dynamics model for simulation. Uses the two phase fixed velocity approach.
 * There are two constant accelerations: rolling and sliding. The transition is done at 2m/s.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallDynamicsModelSimple implements IBallDynamicsModel
{
	private static final double G = 9810;
	private static final double A_FALL = -G;
	private static final double V_SWITCH = 2000;
	
	
	/**
	 * @param state
	 * @param dt
	 * @param context
	 * @return
	 */
	@Override
	public IState dynamics(final IState state, final IAction action, final double dt,
			final MotionContext context)
	{
		double x = state.getPos().x();
		double y = state.getPos().y();
		double z = state.getPos().z();
		double vx = state.getVel().x() * 1000;
		double vy = state.getVel().y() * 1000;
		double vz = state.getVel().z() * 1000;
		
		final double ax;
		final double ay;
		double az;
		
		// velocity
		final double v = SumatraMath.sqrt((vx * vx) + (vy * vy));
		IVector2 dir = Vector2.fromXY(vx, vy);
		
		if ((z > 0) || (vz > 0))
		{
			// flying ball
			ax = 0.0;
			ay = 0.0;
			az = A_FALL;
		} else
		{
			if (vz < 0)
			{
				vx *= Geometry.getBallParameters().getChipDampingXYFirstHop();
				vy *= Geometry.getBallParameters().getChipDampingXYFirstHop();
			}
			vz *= -Geometry.getBallParameters().getChipDampingZ();
			double maxHeight = (vz * vz) / (2 * G);
			if (maxHeight < Geometry.getBallParameters().getMinHopHeight())
			{
				// rolling
				z = 0;
				vz = 0;
				az = 0;
			} else
			{
				// titching
				z = -z;
				az = A_FALL;
			}
			
			final IVector2 targetAcc;
			if (v > V_SWITCH)
			{
				targetAcc = dir.scaleToNew(Geometry.getBallParameters().getAccSlide());
			} else if (!SumatraMath.isZero(v) && ((v + (Geometry.getBallParameters().getAccRoll() * dt)) >= 0))
			{
				targetAcc = dir.scaleToNew(Geometry.getBallParameters().getAccRoll());
			} else
			{
				vx = 0.0;
				vy = 0.0;
				targetAcc = Vector2f.ZERO_VECTOR;
			}
			
			ax = targetAcc.x();
			ay = targetAcc.y();
		}
		
		// update position
		x = (x + (vx * dt)) + (0.5 * dt * dt * ax);
		y = (y + (vy * dt)) + (0.5 * dt * dt * ay);
		z = (z + (vz * dt)) + (0.5 * dt * dt * az);
		
		// calculate ball's velocity from current acceleration
		vx = vx + (dt * ax);
		vy = vy + (dt * ay);
		vz = vz + (dt * az);
		
		if (z < 0)
		{
			z = 0;
		}
		
		IVector3 pos = Vector3.fromXYZ(x, y, z);
		IVector3 vel = Vector3.fromXYZ(vx, vy, vz).multiply(1e-3);
		IVector3 acc = Vector3.fromXYZ(ax, ay, az).multiply(1e-3);
		
		return new BallState(pos, vel, acc);
	}
	
}
