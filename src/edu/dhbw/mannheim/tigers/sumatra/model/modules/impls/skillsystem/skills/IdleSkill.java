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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlResetCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Stops the bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class IdleSkill extends AMoveSkill
{
	
	/**
	 */
	public IdleSkill()
	{
		super(ESkillName.IDLE);
	}
	
	
	@Override
	public boolean needsVision()
	{
		return false;
	}
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
		if (getBotType() == EBotType.TIGER_V3)
		{
			cmds.add(new TigerCtrlResetCommand());
		}
		else
		{
			getDevices().dribble(cmds, false);
			getDevices().disarm(cmds);
			cmds.add(new TigerMotorMoveV2(new Vector2(0, 0), 0f));
		}
		getBot().getPathFinder().reset();
	}
	
	
	@Override
	public void doCalcActions(final List<ACommand> cmds)
	{
		// do not call super
	}
}
