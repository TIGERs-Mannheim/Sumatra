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
 * Moves forward a certain amount of time
 * 
 * @author Someone
 */
public class StraightMove extends ASkill
{
	/** [ms] */
	private static final int	DURATION		= 2000;
	private long					startTime	= 0;
	
	
	public StraightMove()
	{
		super(ESkillName.STRAIGHT_MOVE, ESkillGroup.MOVE);
	}
	

	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		if (startTime == 0)
		{
			startTime = System.nanoTime();
		}
		
		cmds.add(new CTSetSpeed(new Vector2(0, 1), 0f));
		
		if ((System.nanoTime() - startTime) / 1000000 > DURATION)
		{
			complete();
		}
		
		return cmds;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		return true;
	}
	
}
