/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.sim.dynamics.bot;

import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * Simulated bot dynamics based on second order trajectories.
 */
public class SimBotDynamics
{
	private static final float MOTORS_OFF_ACC = 8;
	private static final float MOTORS_OFF_ACC_W = 50;

	private final BangBangTrajectoryFactory trajectoryFactory = new BangBangTrajectoryFactory();

	/**
	 * [mm]
	 */
	private IVector2 pos = Vector2f.zero();
	private double orientation = 0;
	/**
	 * [mm/s, rad/s]
	 */
	private final Vector3 velLocal = Vector3.zero();
	/**
	 * [mm/s^2, rad/s^2]
	 */
	private final Vector3 accLocal = Vector3.zero();


	public Pose getPose()
	{
		return Pose.from(pos, orientation);
	}


	public IVector3 getVelGlobal()
	{
		return Vector3.from2d(BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orientation), velLocal.z());
	}


	public IVector3 getVelLocal()
	{
		return velLocal.copy();
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
	 * @param dt     time step in [s]
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
				offW(dt);
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
				offXY(dt);
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
		orientation = AngleMath.normalizeAngle(orientation + (velLocal.z() * dt) + (accLocal.z() * 0.5 * dt * dt));
		MatrixMotorModel mm = new MatrixMotorModel();
		double targetVel = mm.getXywSpeed(output.getTargetWheelVel()).z();
		velLocal.set(2, velLocal.z() + accLocal.z() * dt);
		accLocal.set(2, Math.signum(targetVel - velLocal.z()) * output.getDriveLimits().getAccMaxW());
	}


	private void offW(final double dt)
	{
		orientation = AngleMath.normalizeAngle(orientation + (velLocal.z() * dt) + (accLocal.z() * 0.5 * dt * dt));
		velLocal.set(2, velLocal.z() + accLocal.z() * dt);
		if (Math.abs(velLocal.z()) < 0.1)
		{
			velLocal.set(2, 0);
			accLocal.set(2, 0);
		} else
		{
			accLocal.set(2, -Math.signum(velLocal.z()) * MOTORS_OFF_ACC_W);
		}
	}


	private void localVelW(final double dt, final SimBotAction output)
	{
		orientation = AngleMath.normalizeAngle(orientation + (velLocal.z() * dt) + (accLocal.z() * 0.5 * dt * dt));
		var targetVel = output.getTargetVelLocal().z();
		velLocal.set(2, velLocal.z() + accLocal.z() * dt);
		accLocal.set(2, Math.signum(targetVel - velLocal.z()) * output.getDriveLimits().getAccMaxW());
	}


	private void globalPosW(final double dt, final SimBotAction output)
	{
		ITrajectory<Double> trajW = trajectoryFactory.orientation(
				orientation,
				output.getTargetPos().z(),
				velLocal.z(),
				output.getDriveLimits().getVelMaxW(),
				output.getDriveLimits().getAccMaxW());

		orientation = AngleMath.normalizeAngle(trajW.getPosition(dt));
		velLocal.set(2, trajW.getVelocity(dt));
		accLocal.set(2, trajW.getAcceleration(dt));
	}


	private void wheelVelXY(final double dt, final SimBotAction output)
	{
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orientation);
		IVector2 accGlobal = BotMath.convertLocalBotVector2Global(accLocal.getXYVector(), orientation);

		pos = pos.addNew(velGlobal.multiplyNew(dt).add(accGlobal.multiplyNew(0.5 * dt * dt)));

		MatrixMotorModel mm = new MatrixMotorModel();
		IVector2 targetVel = mm.getXywSpeed(output.getTargetWheelVel()).getXYVector();
		velLocal.setXY(velLocal.getXYVector().add(accLocal.getXYVector().multiply(dt)));
		var accMax = output.getDriveLimits().getAccMax() * 1000;
		accLocal.setXY(targetVel.subtractNew(velLocal.getXYVector()).scaleTo(accMax));
	}


	private void offXY(final double dt)
	{
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orientation);
		IVector2 accGlobal = BotMath.convertLocalBotVector2Global(accLocal.getXYVector(), orientation);

		pos = pos.addNew(velGlobal.multiplyNew(dt).add(accGlobal.multiplyNew(0.5 * dt * dt)));

		if (velLocal.getLength2() < 50)
		{
			velLocal.setXY(Vector2.zero());
			accLocal.setXY(Vector2.zero());
		} else
		{
			velLocal.setXY(velLocal.getXYVector().add(accLocal.getXYVector().multiply(dt)));
			accLocal.setXY(velLocal.getXYVector().scaleTo(-MOTORS_OFF_ACC * 1000));
		}
	}


	private void localVelXY(final double dt, final SimBotAction output)
	{
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orientation);
		IVector2 accGlobal = BotMath.convertLocalBotVector2Global(accLocal.getXYVector(), orientation);

		pos = pos.addNew(velGlobal.multiplyNew(dt).add(accGlobal.multiplyNew(0.5 * dt * dt)));

		var targetVel = output.getTargetVelLocal().getXYVector();
		velLocal.setXY(velLocal.getXYVector().add(accLocal.getXYVector().multiply(dt)));
		var accMax = output.getDriveLimits().getAccMax() * 1000;
		accLocal.setXY(targetVel.subtractNew(velLocal.getXYVector()).scaleTo(accMax));
	}


	private void globalPosXY(final double dt, final SimBotAction output)
	{
		double orient = orientation;
		IVector2 velGlobal = BotMath.convertLocalBotVector2Global(velLocal.getXYVector(), orient);

		ITrajectory<IVector2> trajXy;
		if ((output.getPrimaryDirection() != null) && !output.getPrimaryDirection().isZeroVector())
		{
			trajXy = trajectoryFactory.async(
					pos.getXYVector().multiplyNew(1e-3),
					output.getTargetPos().getXYVector().multiplyNew(1e-3),
					velGlobal.multiplyNew(1e-3),
					output.getDriveLimits().getVelMax(),
					output.getDriveLimits().getAccMax(),
					output.getPrimaryDirection());

		} else
		{
			trajXy = trajectoryFactory.sync(
					pos.getXYVector().multiplyNew(1e-3),
					output.getTargetPos().getXYVector().multiplyNew(1e-3),
					velGlobal.multiplyNew(1e-3),
					output.getDriveLimits().getVelMax(),
					output.getDriveLimits().getAccMax());
		}

		pos = trajXy.getPositionMM(dt);
		velLocal.setXY(BotMath.convertGlobalBotVector2Local(trajXy.getVelocity(dt).multiplyNew(1e3), orient));
		accLocal.setXY(BotMath.convertGlobalBotVector2Local(trajXy.getAcceleration(dt).multiplyNew(1e3), orient));

		if (output.isStrictVelocityLimit() && velLocal.getLength2() / 1000 > output.getDriveLimits().getVelMax())
		{
			velLocal.setXY(velLocal.getXYVector().scaleTo(output.getDriveLimits().getVelMax() * 1000));
		}
	}
}
