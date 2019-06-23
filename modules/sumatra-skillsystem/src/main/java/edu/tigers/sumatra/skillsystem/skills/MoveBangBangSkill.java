/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 17, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.control.motor.AMotorModel;
import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveBangBangSkill extends AMoveSkill
{
	private IVector2	dest;
	private double		orient;
	private EBotSkill	botSkill;
	
	private double		maxAcc	= 30;
	private double		maxVel	= 5;
	
	
	/**
	 * @param dest
	 * @param orient
	 */
	public MoveBangBangSkill(final IVector2 dest, final double orient)
	{
		this(dest, orient, EBotSkill.LOCAL_VELOCITY);
	}
	
	
	/**
	 * @param dest
	 * @param orient
	 * @param botSkill
	 */
	public MoveBangBangSkill(final IVector2 dest, final double orient, final EBotSkill botSkill)
	{
		super(ESkill.MOVE_BANG_BANG);
		this.dest = dest;
		this.orient = orient;
		this.botSkill = botSkill;
		setInitialState(new DefState());
	}
	
	

	private class DefState implements IState
	{
		ITrajectory<IVector2>	trajXY;
		ITrajectory<Double>		trajW;
		long							tStart;
		
		
		@Override
		public void doEntryActions()
		{
			trajXY = TrajectoryGenerator.generatePositionTrajectory(getTBot(), dest);
			trajW = TrajectoryGenerator.generateRotationTrajectory(getTBot(), orient);
			tStart = getWorldFrame().getTimestamp();
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			double t = (getWorldFrame().getTimestamp() - tStart) / 1e9;
			IVector2 vel = trajXY.getVelocity(t);
			double rot = trajW.getVelocity(t);
			switch (botSkill)
			{
				case GLOBAL_VELOCITY:
					setGlobalVelocity(vel, rot, getMoveCon().getMoveConstraints());
					break;
				case LOCAL_VELOCITY:
					setLocalVelFromGlobalVel(vel, rot, getMoveCon().getMoveConstraints());
					break;
				case WHEEL_VELOCITY:
					AMotorModel motorModel = new MatrixMotorModel();
					IVectorN motors = motorModel.getWheelSpeed(Vector3.from2d(vel, rot));
					BotSkillWheelVelocity skill = new BotSkillWheelVelocity(motors.toArray());
					getMatchCtrl().setSkill(skill);
					break;
				default:
					throw new IllegalArgumentException("Unsupported bot skill: " + botSkill);
			}
		}


    }
	
	
	/**
	 * @return the maxAcc
	 */
	public double getMaxAcc()
	{
		return maxAcc;
	}
	
	
	/**
	 * @param maxAcc the maxAcc to set
	 */
	public void setMaxAcc(final double maxAcc)
	{
		this.maxAcc = maxAcc;
	}
	
	
	/**
	 * @return the maxVel
	 */
	public double getMaxVel()
	{
		return maxVel;
	}
	
	
	/**
	 * @param maxVel the maxVel to set
	 */
	public void setMaxVel(final double maxVel)
	{
		this.maxVel = maxVel;
	}
	
	
}
