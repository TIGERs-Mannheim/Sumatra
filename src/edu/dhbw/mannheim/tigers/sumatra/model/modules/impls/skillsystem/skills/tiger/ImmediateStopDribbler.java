/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.03.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Sets the dribblers speed to 0
 * 
 * @author Gero
 */
public class ImmediateStopDribbler extends ASkill
{
	public ImmediateStopDribbler()
	{
		super(ESkillName.IMMEDIATE_STOP_DRIBBLER, ESkillGroup.DRIBBLE);
	}
	

	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		cmds.add(new TigerDribble(0));
		
		complete();
		
		return cmds;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		return false;
	}
}
