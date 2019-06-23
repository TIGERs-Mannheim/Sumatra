/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.sim.util;

import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillOutput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.DriveLimits;
import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1D;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1DOrient;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;


/**
 * Simulated bot dynamics based on second order trajectories.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class SimBotDynamics
{
	/** [m] */
	private Vector3	pos		= Vector3.zero();
	/** [m/s] */
	private Vector3	velLocal	= Vector3.zero();
	/** [m/s^2] */
	private Vector3	accLocal	= Vector3.zero();
	
	
	/**
	 * @return [mm, rad]
	 */
	public IVector3 getPos()
	{
		return Vector3.from2d(pos.getXYVector().multiply(1e3), pos.z());
	}
	
	
	public IVector3 getVelLocal()
	{
		return Vector3.copy(velLocal);
	}
	
	
	public IVector3 getVelGlobal()
	{
		return Vector3.from2d(BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), pos.z()), velLocal.z());
	}
	
	
	public IVector3 getAccLocal()
	{
		return Vector3.copy(accLocal);
	}
	
	
	public void setPos(final IVector3 pos)
	{
		this.pos.setXY(pos.getXYVector().multiplyNew(1e-3));
		this.pos.set(2, pos.z());
	}
	
	
	public void setVelLocal(final IVector3 vel)
	{
		velLocal = Vector3.copy(vel);
	}
	
	
	public void setVelGlobal(final IVector3 vel)
	{
		velLocal.setXY(BotMath.convertGlobalBotVector2Local(vel.getXYVector(), pos.z()));
		velLocal.set(2, vel.z());
	}
	
	
	public void step(final double dt, final BotSkillOutput output)
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
				offXY(dt);
				break;
			case WHEEL_VEL:
				wheelVelXY(dt, output);
				break;
			default:
				break;
		}
		
		double orientBefore = pos.z();
		
		switch (output.getModeW())
		{
			case GLOBAL_POS:
				globalPosW(dt, output);
				break;
			case LOCAL_VEL:
				localVelW(dt, output);
				break;
			case OFF:
				offW(dt);
				break;
			case WHEEL_VEL:
				wheelVelW(dt, output);
				break;
			default:
				break;
		}
		
		double rotate = pos.z() - orientBefore;
		velLocal.setXY(velLocal.getXYVector().turn(-rotate));
		accLocal.setXY(accLocal.getXYVector().turn(-rotate));
	}
	
	
	private void wheelVelW(final double dt, final BotSkillOutput output)
	{
		MatrixMotorModel mm = new MatrixMotorModel();
		double targetVel = mm.getXywSpeed(output.getTargetWheelVel()).z();
		
		BangBangTrajectory1D trajW = new BangBangTrajectory1D(
				velLocal.z(),
				targetVel,
				accLocal.z(),
				DriveLimits.MAX_ACC_W,
				DriveLimits.MAX_JERK_W);
		
		pos.set(2, AngleMath.normalizeAngle(pos.z() + (velLocal.z() * dt) + (accLocal.z() * 0.5 * dt * dt)));
		velLocal.set(2, trajW.getPosition(dt));
		accLocal.set(2, trajW.getVelocity(dt));
	}
	
	
	private void offW(final double dt)
	{
		BangBangTrajectory1D trajW = new BangBangTrajectory1D(
				velLocal.z(),
				0,
				accLocal.z(),
				DriveLimits.MAX_ACC_W,
				DriveLimits.MAX_JERK_W);
		
		pos.set(2, AngleMath.normalizeAngle(pos.z() + (velLocal.z() * dt) + (accLocal.z() * 0.5 * dt * dt)));
		velLocal.set(2, trajW.getPosition(dt));
		accLocal.set(2, trajW.getVelocity(dt));
	}
	
	
	private void localVelW(final double dt, final BotSkillOutput output)
	{
		BangBangTrajectory1D trajW = new BangBangTrajectory1D(
				velLocal.z(),
				output.getTargetVelLocal().z(),
				accLocal.z(),
				output.getDriveLimits().getAccMaxW(),
				output.getDriveLimits().getJerkMaxW());
		
		pos.set(2, AngleMath.normalizeAngle(pos.z() + (velLocal.z() * dt) + (accLocal.z() * 0.5 * dt * dt)));
		velLocal.set(2, trajW.getPosition(dt));
		accLocal.set(2, trajW.getVelocity(dt));
	}
	
	
	private void globalPosW(final double dt, final BotSkillOutput output)
	{
		BangBangTrajectory1D trajW = new BangBangTrajectory1DOrient(
				pos.z(),
				output.getTargetPos().z(),
				velLocal.z(),
				output.getDriveLimits().getVelMaxW(),
				output.getDriveLimits().getAccMaxW());
		
		pos.set(2, AngleMath.normalizeAngle(trajW.getPosition(dt)));
		velLocal.set(2, trajW.getVelocity(dt));
		accLocal.set(2, trajW.getAcceleration(dt));
	}
	
	
	private void wheelVelXY(final double dt, final BotSkillOutput output)
	{
		MatrixMotorModel mm = new MatrixMotorModel();
		IVector2 targetVel = mm.getXywSpeed(output.getTargetWheelVel()).getXYVector();
		
		BangBangTrajectory2D trajXy = new BangBangTrajectory2D(
				velLocal.getXYVector(),
				targetVel,
				accLocal.getXYVector(),
				DriveLimits.MAX_ACC,
				DriveLimits.MAX_JERK);
		
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), pos.z());
		IVector2 accGlobal = BotMath.convertLocalBotVector2Global(accLocal.getXYVector(), pos.z());
		
		pos.setXY(pos.getXYVector().add(velGlobal.multiplyNew(dt)).add(accGlobal.multiplyNew(0.5 * dt * dt)));
		
		velLocal.setXY(trajXy.getPosition(dt));
		accLocal.setXY(trajXy.getVelocity(dt));
	}
	
	
	private void offXY(final double dt)
	{
		BangBangTrajectory2D trajXy = new BangBangTrajectory2D(
				velLocal.getXYVector(),
				Vector2.zero(),
				accLocal.getXYVector(),
				DriveLimits.MAX_ACC,
				DriveLimits.MAX_JERK);
		
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), pos.z());
		IVector2 accGlobal = BotMath.convertLocalBotVector2Global(accLocal.getXYVector(), pos.z());
		
		pos.setXY(pos.getXYVector().add(velGlobal.multiplyNew(dt)).add(accGlobal.multiplyNew(0.5 * dt * dt)));
		
		velLocal.setXY(trajXy.getPosition(dt));
		accLocal.setXY(trajXy.getVelocity(dt));
	}
	
	
	private void localVelXY(final double dt, final BotSkillOutput output)
	{
		BangBangTrajectory2D trajXy = new BangBangTrajectory2D(
				velLocal.getXYVector(),
				output.getTargetVelLocal().getXYVector(),
				accLocal.getXYVector(),
				output.getDriveLimits().getAccMax(),
				output.getDriveLimits().getJerkMax());
		
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), pos.z());
		IVector2 accGlobal = BotMath.convertLocalBotVector2Global(accLocal.getXYVector(), pos.z());
		
		pos.setXY(pos.getXYVector().add(velGlobal.multiplyNew(dt)).add(accGlobal.multiplyNew(0.5 * dt * dt)));
		
		velLocal.setXY(trajXy.getPosition(dt));
		accLocal.setXY(trajXy.getVelocity(dt));
	}
	
	
	private void globalPosXY(final double dt, final BotSkillOutput output)
	{
		double orient = pos.z();
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orient);
		BangBangTrajectory2D trajXy = new BangBangTrajectory2D(
				pos.getXYVector(),
				output.getTargetPos().getXYVector().multiplyNew(1e-3),
				velGlobal,
				output.getDriveLimits().getVelMax(),
				output.getDriveLimits().getAccMax());
		
		pos.setXY(trajXy.getPosition(dt));
		velLocal.setXY(BotMath.convertGlobalBotVector2Local(trajXy.getVelocity(dt), orient));
		accLocal.setXY(BotMath.convertGlobalBotVector2Local(trajXy.getAcceleration(dt), orient));
	}
}
