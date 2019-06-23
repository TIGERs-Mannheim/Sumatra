/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 17, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.SplinePathDriver;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryXyw;


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
	
	
	private enum EStateId
	{
		DEFAULT
	}
	
	
	private class DefState implements IState
	{
		ITrajectory<IVector2>	trajXY;
		ITrajectory<Double>		trajW;
		
		
		@Override
		public void doEntryActions()
		{
			trajXY = new TrajectoryGenerator().generatePositionTrajectory(getTBot(), dest);
			trajW = new TrajectoryGenerator().generateRotationTrajectory(getTBot(), orient, maxAcc, maxVel);
			ITrajectory<IVector3> traj = new TrajectoryXyw(trajXY, trajW);
			SplinePathDriver driver = new SplinePathDriver(traj);
			driver.clearSupportedCommands();
			driver.addSupportedCommand(botSkill);
			setPathDriver(driver);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.DEFAULT;
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
