/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.skillsystem.ESkill;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotSkillWrapperSkill extends ASkill
{
	private ABotSkill skill;
	
	
	/**
	 * @param botSkill
	 */
	public BotSkillWrapperSkill(final ABotSkill botSkill)
	{
		super(ESkill.BOT_SKILL_WRAPPER);
		skill = botSkill;
	}
	
	
	/**
	 */
	public BotSkillWrapperSkill()
	{
		super(ESkill.BOT_SKILL_WRAPPER);
		skill = new BotSkillMotorsOff();
	}
	
	
	@Override
	protected void doCalcActionsBeforeStateUpdate()
	{
		getMatchCtrl().setSkill(skill);
	}
	
	
	/**
	 * @return the skill
	 */
	public final ABotSkill getSkill()
	{
		return skill;
	}
	
	
	/**
	 * @param skill the skill to set
	 */
	public final void setSkill(final ABotSkill skill)
	{
		this.skill = skill;
	}
}
