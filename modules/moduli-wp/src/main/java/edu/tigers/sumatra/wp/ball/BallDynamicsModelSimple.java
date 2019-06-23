/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallDynamicsModelSimple implements IBallDynamicsModel
{
	/** [mm/s²] */
	private final double				acc;
	private double						accSlide;
	
	private static final double	A_FALL						= -9810;
	private static final double	BALL_DAMP_GROUND_FACTOR	= 0.37;
	
	
	/**
	 * @param acc
	 */
	public BallDynamicsModelSimple(final double acc)
	{
		this.acc = acc * 1000;
		accSlide = 10 * this.acc;
	}
	
	
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
		
		
		IVector3 accTorque = state.getAccFromTorque();
		if (accTorque.getLength2() <= action.getAccTorque().getLength2())
		{
			accTorque = accTorque.addNew(action.getAccTorque().subtractNew(state.getAccFromTorque()).multiply(0.9));
		} else
		{
			double accTorqueXy = Math.max(0, accTorque.getLength2() - (30 * dt));
			double accTorqueZ = Math.max(0, accTorque.z() - (30 * dt));
			accTorque = new Vector3(accTorque.getXYVector().scaleToNew(accTorqueXy), accTorqueZ);
		}
		
		final double ax;
		final double ay;
		double az = state.getAcc().z() * 1000;
		
		// velocity
		final double v = Math.sqrt((vx * vx) + (vy * vy));
		IVector2 dir = new Vector2(vx, vy);
		
		if ((z > 0) || (vz > 0))
		{
			// flying ball
			ax = 0.0;
			ay = 0.0;
			az = A_FALL;
		} else
		{
			// not flying or z < 0
			if ((Math.abs(vz) + (A_FALL * dt)) >= 0)
			{
				// damp on ground
				z = 0;
				vz = -BALL_DAMP_GROUND_FACTOR * vz;
				az = A_FALL;
			} else
			{
				z = 0;
				vz = 0;
				az = 0;
			}
			
			IVector2 targetAcc = accTorque.getXYVector().multiplyNew(1000);
			if (v > 2000)
			{
				targetAcc = targetAcc.addNew(dir.scaleToNew(accSlide));
			} else if ((v != 0) && ((v + (acc * dt)) >= 0))
			{
				targetAcc = targetAcc.addNew(dir.scaleToNew(acc));
			} else
			{
				vx = 0.0;
				vy = 0.0;
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
		
		IVector3 pos = new Vector3(x, y, z);
		IVector3 vel = new Vector3(vx, vy, vz).multiply(1e-3);
		IVector3 acc = new Vector3(ax, ay, az).multiply(1e-3);
		
		return new BallState(pos, vel, acc, accTorque);
	}
	
}
