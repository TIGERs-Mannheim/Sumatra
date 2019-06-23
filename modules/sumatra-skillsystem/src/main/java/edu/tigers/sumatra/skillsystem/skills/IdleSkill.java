/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IState;


/**
 * Stops the bot and maintains update and feedback frequency
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
	
	
	/**
	 * Default instance
	 */
	public IdleSkill()
	{
		super(ESkill.IDLE);
		setInitialState(new IdleState());
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
				setMinDt();
			} else
			{
				getMatchCtrl().setFeedbackFreq(feedbackFreqIdle);
				setMinDt(1f / updateRateIdle);
			}
		}
	}
}
