/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 30, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.motorlearner;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.control.motor.EMotorModel;
import edu.tigers.sumatra.control.motor.IMotorModel;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.IVectorN;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1D;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryXyw;
import edu.tigers.sumatra.wp.VisionWatcher;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MotorTrajSamplerRole extends ARole
{
	@SuppressWarnings("unused")
	private static final Logger	log			= Logger.getLogger(MotorTrajSamplerRole.class.getName());
															
	private static double			timeOffset	= 0.5;
															
	private IVector3					initPos;
	private IVector3					finalPos;
	private IMotorModel				motorModel;
											
											
	/**
	 * @param initPos
	 * @param finalPos
	 * @param emm
	 */
	public MotorTrajSamplerRole(final IVector3 initPos, final IVector3 finalPos, final EMotorModel emm)
	{
		super(ERole.MOTOR_TRAJ_SAMPLER);
		this.initPos = initPos;
		this.finalPos = finalPos;
		try
		{
			motorModel = (IMotorModel) emm.getInstanceableClass().newDefaultInstance();
		} catch (NotCreateableException e)
		{
			log.error("Could not create motor model", e);
			setCompleted();
		}
		
		IRoleState initState = new InitState();
		IRoleState sampleState = new SampleState();
		setInitialState(initState);
		addTransition(initState, EEvent.READY, sampleState);
		addTransition(sampleState, EEvent.DONE, initState);
	}
	
	
	private enum EStateId
	{
		INIT,
		SAMPLE
	}
	
	private enum EEvent
	{
		READY,
		DONE
	}
	
	private class InitState implements IRoleState
	{
		private AMoveToSkill move;
		
		
		@Override
		public void doEntryActions()
		{
			move = AMoveToSkill.createMoveToSkill();
			move.getMoveCon().setPenaltyAreaAllowedOur(true);
			move.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(move);
			// setNewSkill(new PositionSkill(initPos.getXYVector(), initPos.z()));
		}
		
		
		@Override
		public void doUpdate()
		{
			double targetAngle = initPos.z();
			move.getMoveCon().updateDestination(initPos.getXYVector());
			move.getMoveCon().updateTargetAngle(targetAngle);
			
			double distDiff = GeoMath.distancePP(getPos(), initPos.getXYVector());
			double angleDiff = Math.abs(AngleMath.difference(getBot().getAngle(), targetAngle));
			if ((distDiff < 20) && (angleDiff < 0.05))
			{
				triggerEvent(EEvent.READY);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.INIT;
		}
	}
	
	private class SampleState implements IRoleState
	{
		long							tStart;
		BotSkillWrapperSkill		skill;
		CSVExporter					exp;
		ITrajectory<IVector3>	trajectory;
		VisionWatcher					watcher;
										
										
		@Override
		public void doEntryActions()
		{
			tStart = getWFrame().getTimestamp();
			skill = new BotSkillWrapperSkill();
			setNewSkill(skill);
			String fileName = "trajSample/" + System.currentTimeMillis();
			exp = new CSVExporter("data/" + fileName, false);
			watcher = new VisionWatcher(fileName);
			watcher.start();
			
			ITrajectory<IVector2> trajXy = new BangBangTrajectory2D(
					initPos.getXYVector().multiplyNew(1e-3),
					finalPos.getXYVector().multiplyNew(1e-3),
					AVector2.ZERO_VECTOR,
					2, 3, 2);
			ITrajectory<Double> trajW = new BangBangTrajectory1D(initPos.z(), finalPos.z(), 0, 10, 10, 10);
			
			trajectory = new TrajectoryXyw(trajXy, trajW);
		}
		
		
		@Override
		public void doUpdate()
		{
			double t = ((getWFrame().getTimestamp() - tStart) / 1e9) - timeOffset;
			IVector3 pos = trajectory.getPosition(t);
			IVector3 velGlob = trajectory.getVelocity(t);
			double orientation = pos.z();
			IVector2 vel = GeoMath.convertGlobalBotVector2Local(velGlob.getXYVector(), orientation);
			double aVel = velGlob.z();
			IVectorN motorVel = motorModel.getWheelSpeed(new Vector3(vel, aVel));
			
			skill.setSkill(new BotSkillWheelVelocity(motorVel.toArray()));
			
			if (t > (trajectory.getTotalTime() + timeOffset))
			{
				skill.setSkill(new BotSkillMotorsOff());
				triggerEvent(EEvent.DONE);
			}
			List<Number> nbrs = new ArrayList<>();
			nbrs.add(t);
			nbrs.add(trajectory.getTotalTime());
			
			nbrs.addAll(vel.getNumberList());
			nbrs.add(aVel);
			
			nbrs.addAll(velGlob.getNumberList());
			
			nbrs.addAll(getBot().getVel().getNumberList());
			nbrs.add(getBot().getaVel());
			
			nbrs.addAll(pos.getNumberList());
			
			nbrs.addAll(getBot().getPos().multiplyNew(1e-3).getNumberList());
			nbrs.add(getBot().getAngle());
			
			exp.addValues(nbrs);
		}
		
		
		@Override
		public void doExitActions()
		{
			exp.close();
			watcher.stopExport();
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.SAMPLE;
		}
	}
}
