/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.botskills;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;


/**
 * @author AndreR
 */
public abstract class ABotSkill
{
	private final EBotSkill	skill;
	
	
	protected ABotSkill(final EBotSkill skill)
	{
		this.skill = skill;
	}
	
	
	/**
	 * @return
	 */
	public EBotSkill getType()
	{
		return skill;
	}
}
