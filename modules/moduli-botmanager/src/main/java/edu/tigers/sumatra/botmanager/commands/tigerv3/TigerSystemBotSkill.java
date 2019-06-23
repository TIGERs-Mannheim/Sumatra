/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 26, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerSystemBotSkill extends ACommand
{
	private final ABotSkill	skill;
	
	
	/**
	 * 
	 */
	public TigerSystemBotSkill()
	{
		this(null);
	}
	
	
	/**
	 * @param skill
	 */
	public TigerSystemBotSkill(final ABotSkill skill)
	{
		super(ECommand.CMD_SYSTEM_BOT_SKILL);
		this.skill = skill;
	}
	
	
	/**
	 * @return the skill
	 */
	public final ABotSkill getSkill()
	{
		return skill;
	}
}
