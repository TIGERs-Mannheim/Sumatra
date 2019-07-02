/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.AState;


/**
 * Stops the bot and maintains update and feedback frequency
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class IdleSkill extends ASkill
{
	/**
	 * Default instance
	 */
	public IdleSkill()
	{
		super(ESkill.IDLE);
		setInitialState(new IdleState());
	}
	
	private class IdleState extends AState
	{
		@Override
		public void doEntryActions()
		{
			getMatchCtrl().setSkill(new BotSkillMotorsOff());
		}
	}
}
