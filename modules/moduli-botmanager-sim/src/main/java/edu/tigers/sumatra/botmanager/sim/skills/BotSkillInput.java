/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;


/**
 * Additional bot skill input data.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillInput
{
	private final ABotSkill skill;
	private final SimBotState state;
	private final long tNow;
	private final boolean strictVelocityLimit;
	
	
	/**
	 * @param skill
	 * @param state
	 * @param tNow
	 * @param strictVelocityLimit
	 */
	public BotSkillInput(final ABotSkill skill, final SimBotState state,
			final long tNow, final boolean strictVelocityLimit)
	{
		this.skill = skill;
		this.state = state;
		this.tNow = tNow;
		this.strictVelocityLimit = strictVelocityLimit;
	}
	
	
	public SimBotState getState()
	{
		return state;
	}
	
	
	/**
	 * @return
	 */
	public long gettNow()
	{
		return tNow;
	}
	
	
	public ABotSkill getSkill()
	{
		return skill;
	}
	
	
	public boolean isStrictVelocityLimit()
	{
		return strictVelocityLimit;
	}
}
