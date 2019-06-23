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

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Stops the bot
 * 
 * @author ChristianK
 */
public class ImmediateStopSkill extends AMoveSkill
{
	private int	ctr	= 0;
	
	
	/**
	 */
	public ImmediateStopSkill()
	{
		super(ESkillName.IMMEDIATE_STOP);
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
		getDevices().disarm(cmds);
		stopMoveImmediately(cmds);
		
		return cmds;
	}
	
	
	@Override
	protected void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds)
	{
		stopMoveImmediately(cmds);
		ctr++;
		if (ctr > 5)
		{
			complete();
		}
	}
}
