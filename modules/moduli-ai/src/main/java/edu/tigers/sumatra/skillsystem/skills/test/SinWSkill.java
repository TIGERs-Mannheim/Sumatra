/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 26, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.statemachine.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SinWSkill extends ASkill
{
	
	
	/**
	 */
	public SinWSkill()
	{
		super(ESkill.SIN_W);
		setInitialState(new DoState());
	}
	
	private enum EStateId
	{
		INIT
	}
	
	
	private class DoState implements IState
	{
		long tStart;
		
		
		@Override
		public void doEntryActions()
		{
			tStart = getWorldFrame().getTimestamp();
		}
		
		
		@Override
		public void doUpdate()
		{
			double t = (getWorldFrame().getTimestamp() - tStart) / 1e9;
			double velw = 3 * Math.sin(t);
			BotSkillLocalVelocity skill = new BotSkillLocalVelocity(AVector2.ZERO_VECTOR, velw,
					getBot().getMoveConstraints());
			getMatchCtrl().setSkill(skill);
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
}
