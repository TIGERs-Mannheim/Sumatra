/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.control.motor.AMotorModel;
import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveBangBangSkill extends AMoveSkill
{
	private IVector2 dest;
	private double orient;
	private EBotSkill botSkill;
	private boolean rollOut;
	
	
	/**
	 * @param dest
	 * @param orient
	 */
	public MoveBangBangSkill(final IVector2 dest, final double orient)
	{
		this(dest, orient, EBotSkill.LOCAL_VELOCITY, false);
	}
	
	
	/**
	 * @param dest
	 * @param orient
	 * @param botSkill
	 */
	public MoveBangBangSkill(final IVector2 dest, final double orient, final EBotSkill botSkill)
	{
		this(dest, orient, botSkill, false);
	}
	
	
	/**
	 * @param dest
	 * @param orient
	 * @param botSkill
	 * @param rollOut
	 */
	public MoveBangBangSkill(final IVector2 dest, final double orient, final EBotSkill botSkill, final boolean rollOut)
	{
		super(ESkill.MOVE_BANG_BANG);
		this.dest = dest;
		this.orient = orient;
		this.botSkill = botSkill;
		this.rollOut = rollOut;
		setInitialState(new DefState());
	}
	
	
	private class DefState extends AState
	{
		ITrajectory<IVector2> trajXY;
		ITrajectory<Double> trajW;
		long tStart;
		boolean motorsOff;
		
		
		@Override
		public void doEntryActions()
		{
			trajXY = TrajectoryGenerator.generatePositionTrajectory(getTBot(), dest, getMoveCon().getMoveConstraints());
			trajW = TrajectoryGenerator.generateRotationTrajectory(getTBot(), orient, getMoveCon().getMoveConstraints());
			tStart = getWorldFrame().getTimestamp();
			motorsOff = false;
		}
		
		
		@Override
		public void doUpdate()
		{
			double t = (getWorldFrame().getTimestamp() - tStart) / 1e9;
			IVector2 vel = trajXY.getVelocity(t);
			
			if (rollOut)
			{
				IVector2 acc = trajXY.getAcceleration(t);
				if (vel.angleToAbs(acc).orElse(0.0) > AngleMath.PI_HALF)
				{
					motorsOff = true;
				}
			}
			
			if (motorsOff)
			{
				setLocalForce(Vector2f.ZERO_VECTOR, 0.0, getMoveCon().getMoveConstraints());
				return;
			}
			
			double rot = trajW.getVelocity(t);
			switch (botSkill)
			{
				case GLOBAL_POSITION:
					setTargetPose(dest, orient, getMoveCon().getMoveConstraints());
					break;
				case GLOBAL_VELOCITY:
					setGlobalVelocity(vel, rot, getMoveCon().getMoveConstraints());
					break;
				case LOCAL_VELOCITY:
					setLocalVelocity(BotMath.convertGlobalBotVector2Local(vel, trajW.getPosition(t)),
							rot, getMoveCon().getMoveConstraints());
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
	 * @return the rollOut
	 */
	public boolean isRollOut()
	{
		return rollOut;
	}
	
	
	/**
	 * @param rollOut the rollOut to set
	 */
	public void setRollOut(final boolean rollOut)
	{
		this.rollOut = rollOut;
	}
}
