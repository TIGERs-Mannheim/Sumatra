/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.11.2010
 * Author(s):
 * ChristianK
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;

/**
 * Stops the bot
 * 
 * @author ChristianK
 */
public class ImmediateStop extends ASkill
{
	public ImmediateStop()
	{
		super(ESkillName.IMMEDIATE_STOP, ESkillGroup.MOVE);
	}
	
	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		// Stop Moving
		cmds.add(new TigerMotorMoveV2(new Vector2(0, 0), 0f));
		
		complete();
		
		return cmds;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		return true;
	}
}
