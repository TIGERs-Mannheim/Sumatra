/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.sim.dynamics.bot;

import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1D;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1DOrient;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2DAsync;


/**
 * Simulated bot dynamics based on second order trajectories.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class SimBotDynamics
{
	/** [mm] */
	private IVector2 pos = Vector2f.zero();
	private double orientation = 0;
	/** [mm/s, rad/s] */
	private final Vector3 velLocal = Vector3.zero();
	/** [mm/s^2, rad/s^2] */
	private final Vector3 accLocal = Vector3.zero();
	
	
	public Pose getPose()
	{
		return Pose.from(pos, orientation);
	}
	
	
	public IVector3 getVelGlobal()
	{
		return Vector3.from2d(BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orientation), velLocal.z());
	}
	
	
	public void setPose(final Pose pose)
	{
		this.pos = pose.getPos();
		this.orientation = pose.getOrientation();
	}
	
	
	public void setVelGlobal(final IVector3 vel)
	{
		velLocal.setXY(BotMath.convertGlobalBotVector2Local(vel.getXYVector(), orientation));
		velLocal.set(2, vel.z());
	}
	
	
	/**
	 * Simulate a step
	 * 
	 * @param dt time step in [s]
	 * @param action the robot action
	 * @return the new state
	 */
	public SimBotDynamicsState step(final double dt, final SimBotAction action)
	{
		stepXY(dt, action);
		
		double orientBefore = orientation;
		
		stepW(dt, action);
		
		double rotate = AngleMath.difference(orientation, orientBefore);
		velLocal.setXY(velLocal.getXYVector().turn(-rotate));
		accLocal.setXY(accLocal.getXYVector().turn(-rotate));
		return new SimBotDynamicsState(getPose(), getVelGlobal());
	}
	
	
	private void stepW(final double dt, final SimBotAction output)
	{
		switch (output.getModeW())
		{
			case GLOBAL_POS:
				globalPosW(dt, output);
				break;
			case LOCAL_VEL:
				localVelW(dt, output);
				break;
			case OFF:
				offW(dt, output);
				break;
			case WHEEL_VEL:
				wheelVelW(dt, output);
				break;
			default:
				break;
		}
	}
	
	
	private void stepXY(final double dt, final SimBotAction output)
	{
		switch (output.getModeXY())
		{
			case GLOBAL_POS:
				globalPosXY(dt, output);
				break;
			case LOCAL_VEL:
				localVelXY(dt, output);
				break;
			case OFF:
				offXY(dt, output);
				break;
			case WHEEL_VEL:
				wheelVelXY(dt, output);
				break;
			default:
				break;
		}
	}
	
	
	private void wheelVelW(final double dt, final SimBotAction output)
	{
		MatrixMotorModel mm = new MatrixMotorModel();
		double targetVel = mm.getXywSpeed(output.getTargetWheelVel()).z();
		
		BangBangTrajectory1D trajW = new BangBangTrajectory1D(
				(float) velLocal.z(),
				(float) targetVel,
				(float) accLocal.z(),
				(float) output.getDriveLimits().getAccMaxW(),
				(float) output.getDriveLimits().getJerkMaxW());
		
		orientation = AngleMath.normalizeAngle(orientation + (velLocal.z() * dt) + (accLocal.z() * 0.5 * dt * dt));
		velLocal.set(2, trajW.getPosition(dt));
		accLocal.set(2, trajW.getVelocity(dt));
	}
	
	
	private void offW(final double dt, final SimBotAction output)
	{
		BangBangTrajectory1D trajW = new BangBangTrajectory1D(
				(float) velLocal.z(),
				0f,
				(float) accLocal.z(),
				(float) output.getDriveLimits().getAccMaxW(),
				(float) output.getDriveLimits().getJerkMaxW());
		
		orientation = AngleMath.normalizeAngle(orientation + (velLocal.z() * dt) + (accLocal.z() * 0.5 * dt * dt));
		velLocal.set(2, trajW.getPosition(dt));
		accLocal.set(2, trajW.getVelocity(dt));
	}
	
	
	private void localVelW(final double dt, final SimBotAction output)
	{
		BangBangTrajectory1D trajW = new BangBangTrajectory1D(
				(float) velLocal.z(),
				(float) output.getTargetVelLocal().z(),
				(float) accLocal.z(),
				(float) output.getDriveLimits().getAccMaxW(),
				(float) output.getDriveLimits().getJerkMaxW());
		
		orientation = AngleMath.normalizeAngle(orientation + (velLocal.z() * dt) + (accLocal.z() * 0.5 * dt * dt));
		velLocal.set(2, trajW.getPosition(dt));
		accLocal.set(2, trajW.getVelocity(dt));
	}
	
	
	private void globalPosW(final double dt, final SimBotAction output)
	{
		BangBangTrajectory1D trajW = new BangBangTrajectory1DOrient(
				(float) orientation,
				(float) output.getTargetPos().z(),
				(float) velLocal.z(),
				(float) output.getDriveLimits().getVelMaxW(),
				(float) output.getDriveLimits().getAccMaxW());
		
		orientation = AngleMath.normalizeAngle(trajW.getPosition(dt));
		velLocal.set(2, trajW.getVelocity(dt));
		accLocal.set(2, trajW.getAcceleration(dt));
	}
	
	
	private void wheelVelXY(final double dt, final SimBotAction output)
	{
		MatrixMotorModel mm = new MatrixMotorModel();
		IVector2 targetVel = mm.getXywSpeed(output.getTargetWheelVel()).getXYVector();
		
		BangBangTrajectory2D trajXy = new BangBangTrajectory2D(
				velLocal.getXYVector().multiply(1e-3),
				targetVel,
				accLocal.getXYVector().multiply(1e-3),
				(float) output.getDriveLimits().getAccMax(),
				(float) output.getDriveLimits().getJerkMax());
		
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orientation);
		IVector2 accGlobal = BotMath.convertLocalBotVector2Global(accLocal.getXYVector(), orientation);
		
		pos = pos.addNew(velGlobal.multiplyNew(dt).add(accGlobal.multiplyNew(0.5 * dt * dt)));
		
		velLocal.setXY(trajXy.getPositionMM(dt));
		accLocal.setXY(trajXy.getVelocity(dt).multiply(1e3));
	}
	
	
	private void offXY(final double dt, final SimBotAction output)
	{
		BangBangTrajectory2D trajXy = new BangBangTrajectory2D(
				velLocal.getXYVector().multiply(1e-3),
				Vector2f.ZERO_VECTOR,
				accLocal.getXYVector().multiply(1e-3),
				(float) output.getDriveLimits().getAccMax(),
				(float) output.getDriveLimits().getJerkMax());
		
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orientation);
		IVector2 accGlobal = BotMath.convertLocalBotVector2Global(accLocal.getXYVector(), orientation);
		
		pos = pos.addNew(velGlobal.multiplyNew(dt).add(accGlobal.multiplyNew(0.5 * dt * dt)));
		
		velLocal.setXY(trajXy.getPositionMM(dt));
		accLocal.setXY(trajXy.getVelocity(dt).multiply(1e3));
	}
	
	
	private void localVelXY(final double dt, final SimBotAction output)
	{
		BangBangTrajectory2D trajXy = new BangBangTrajectory2D(
				velLocal.getXYVector().multiply(1e-3),
				output.getTargetVelLocal().getXYVector().multiplyNew(1e-3),
				accLocal.getXYVector().multiply(1e-3),
				output.getDriveLimits().getAccMax(),
				output.getDriveLimits().getJerkMax());
		
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orientation);
		IVector2 accGlobal = BotMath.convertLocalBotVector2Global(accLocal.getXYVector(), orientation);
		
		pos = pos.addNew(velGlobal.multiplyNew(dt).add(accGlobal.multiplyNew(0.5 * dt * dt)));
		
		assert trajXy.getPositionMM(dt).isFinite();
		velLocal.setXY(trajXy.getPositionMM(dt));
		accLocal.setXY(trajXy.getVelocity(dt).multiply(1e3));
	}
	
	
	private void globalPosXY(final double dt, final SimBotAction output)
	{
		double orient = orientation;
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orient);
		
		BangBangTrajectory2D trajXy;
		if ((output.getPrimaryDirection() != null) && !output.getPrimaryDirection().isZeroVector())
		{
			trajXy = new BangBangTrajectory2DAsync(
					pos.getXYVector().multiplyNew(1e-3),
					output.getTargetPos().getXYVector().multiplyNew(1e-3),
					velGlobal.multiplyNew(1e-3),
					output.getDriveLimits().getVelMax(),
					output.getDriveLimits().getAccMax(),
					output.getPrimaryDirection());
			
		} else
		{
			trajXy = new BangBangTrajectory2D(
					pos.getXYVector().multiplyNew(1e-3),
					output.getTargetPos().getXYVector().multiplyNew(1e-3),
					velGlobal.multiplyNew(1e-3),
					output.getDriveLimits().getVelMax(),
					output.getDriveLimits().getAccMax());
		}
		
		pos = trajXy.getPositionMM(dt);
		velLocal.setXY(BotMath.convertGlobalBotVector2Local(trajXy.getVelocity(dt).multiply(1e3), orient));
		accLocal.setXY(BotMath.convertGlobalBotVector2Local(trajXy.getAcceleration(dt).multiply(1e3), orient));
		
		if (output.isStrictVelocityLimit() && velLocal.getLength2() / 1000 > output.getDriveLimits().getVelMax())
		{
			velLocal.setXY(velLocal.getXYVector().scaleTo(output.getDriveLimits().getVelMax() * 1000));
		}
	}
}
