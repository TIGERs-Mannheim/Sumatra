/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import lombok.Setter;


/**
 * Wrapper skill that takes a bot skill and executes it.
 */
public class BotSkillWrapperSkill extends ASkill
{
	@Setter
	private ABotSkill skill;
	@Setter
	private boolean keepKickerDribbler = false;


	public BotSkillWrapperSkill(final ABotSkill botSkill)
	{
		skill = botSkill;
	}


	public BotSkillWrapperSkill()
	{
		this(new BotSkillMotorsOff());
	}


	@Override
	protected void doCalcActionsBeforeStateUpdate()
	{
		if (keepKickerDribbler)
		{
			skill.setKickerDribbler(getMatchCtrl().getSkill().getKickerDribbler());
		}
		getMatchCtrl().setSkill(skill);
	}
}
