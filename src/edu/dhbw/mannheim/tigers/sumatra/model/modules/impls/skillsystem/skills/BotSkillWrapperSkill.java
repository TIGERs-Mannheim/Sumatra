/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotSkillWrapperSkill extends ASkill
{
	private final ABotSkill	skill;
	
	
	/**
	 * @param botSkill
	 */
	public BotSkillWrapperSkill(final ABotSkill botSkill)
	{
		super(ESkillName.BOT_SKILL_WRAPPER);
		skill = botSkill;
	}
	
	
	@Override
	protected void doCalcEntryActions(final List<ACommand> cmds)
	{
		super.doCalcEntryActions(cmds);
		cmds.add(new TigerSystemBotSkill(skill));
	}
}
