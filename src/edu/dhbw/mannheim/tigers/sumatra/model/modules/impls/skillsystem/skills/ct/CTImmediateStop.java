/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s):
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ct;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTSetSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Immediatly stops the CT-bot
 * 
 * @author AndreR
 */
public class CTImmediateStop extends ASkill
{
	public CTImmediateStop()
	{
		super(ESkillName.IMMEDIATE_STOP, ESkillGroup.MOVE);
	}
	

	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		cmds.add(new CTSetSpeed(new Vector2(0, 0), 0));
		
		complete();
		
		return cmds;
	}


	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		return true;
	}
}
