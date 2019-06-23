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
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ImmediateStopSkill extends AMoveSkill
{
	// private EControllerType lastCtrlType = EControllerType.NONE;
	
	
	/**
	 */
	public ImmediateStopSkill()
	{
		super(ESkillName.IMMEDIATE_STOP);
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(final List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
		getDevices().disarm(cmds);
		stopMoveImmediately(cmds);
		// if (getBotType() == EBotType.TIGER_V2)
		// {
		// TigerBotV2 botV2 = (TigerBotV2) getBot();
		// lastCtrlType = botV2.getControllerType();
		// cmds.add(new TigerCtrlSetControllerType(EControllerType.NONE));
		// }
		
		return cmds;
	}
	
	
	@Override
	protected void periodicProcess(final List<ACommand> cmds)
	{
	}
	
	
	@Override
	public boolean needsVision()
	{
		return false;
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(final List<ACommand> cmds)
	{
		// if (getBotType() == EBotType.TIGER_V2)
		// {
		// cmds.add(new TigerCtrlSetControllerType(lastCtrlType));
		// }
		return cmds;
	}
}
