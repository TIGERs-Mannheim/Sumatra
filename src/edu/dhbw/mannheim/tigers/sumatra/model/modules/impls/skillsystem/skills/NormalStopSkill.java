/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.11.2010
 * Author(s):
 * ChristianK
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Stops the bot
 * 
 * @author ChristianK
 */
public class NormalStopSkill extends AMoveSkill
{
	
	/**
	 */
	public NormalStopSkill()
	{
		super(ESkillName.NORMAL_STOP);
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
		getDevices().disarm(cmds);
		stopMove(cmds);
		complete();
		
		return cmds;
	}
	
	
	@Override
	protected void periodicProcess(List<ACommand> cmds)
	{
	}
	
	
	@Override
	public boolean needsVision()
	{
		return false;
	}
}
