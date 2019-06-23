/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.11.2010
 * Author(s):
 * ChristianK
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IState;


/**
 * Stops the bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class IdleSkill extends ASkill
{
	
	@Configurable(comment = "Desired frequency [Hz] of feedback commands from bot if idle")
	private static int		feedbackFreqIdle		= 5;
	
	@Configurable(comment = "Desired frequency [Hz] of feedback commands from bot if active")
	private static int		feedbackFreqActive	= 120;
	
	@Configurable
	private static double	updateRateIdle			= 20;
	
	@Configurable
	private static double	breakAcc					= 6;
	
	private enum EStateId
	{
		IDLE,
		STOP;
	}
	
	private enum EEvent
	{
		STOPPED
	}
	
	
	/**
	 */
	public IdleSkill()
	{
		super(ESkill.IDLE);
		setInitialState(new StopState());
		addTransition(EEvent.STOPPED, new IdleState());
	}
	
	
	private class IdleState implements IState
	{
		@Override
		public void doEntryActions()
		{
			getMatchCtrl().setSkill(new BotSkillMotorsOff());
		}
		
		
		@Override
		public void doExitActions()
		{
			getMatchCtrl().setFeedbackFreq(feedbackFreqActive);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getMatchCtrl().getSkill().getType() != EBotSkill.MOTORS_OFF)
			{
				getMatchCtrl().setFeedbackFreq(feedbackFreqActive);
				setMinDt(1f / getBot().getUpdateRate());
			} else
			{
				getMatchCtrl().setFeedbackFreq(feedbackFreqIdle);
				setMinDt(1f / updateRateIdle);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.IDLE;
		}
	}
	
	private class StopState implements IState
	{
		@Override
		public void doEntryActions()
		{
			getMatchCtrl().setDribblerSpeed(0);
			getMatchCtrl().setKick(0, EKickerDevice.STRAIGHT, EKickerMode.DISARM);
			BotSkillLocalVelocity skill = new BotSkillLocalVelocity(getBot().getMoveConstraints());
			skill.setAccMax(breakAcc);
			// getMatchCtrl().setSkill(skill);
			
			// BotSkillWheelVelocity wSkill = new BotSkillWheelVelocity();
			// getMatchCtrl().setSkill(wSkill);
		}
		
		
		@Override
		public void doUpdate()
		{
			// if (getVel().getLength() < 0.3)
			{
				triggerEvent(EEvent.STOPPED);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.STOP;
		}
	}
}
