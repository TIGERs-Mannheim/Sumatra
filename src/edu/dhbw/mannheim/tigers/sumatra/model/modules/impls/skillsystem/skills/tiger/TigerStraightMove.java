/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s): AndreR
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
 * Simple straight move that times out after a specified time.
 * Mainly for testing purposes.
 * 
 * @author AndreR
 * 
 */
public class TigerStraightMove extends ASkill
{
	private long	startTime	= 0;
	private long	runTime		= 2000;	// ms
													

	public TigerStraightMove()
	{
		super(ESkillName.STRAIGHT_MOVE, ESkillGroup.MOVE);
	}
	

	public TigerStraightMove(int time)
	{
		super(ESkillName.STRAIGHT_MOVE, ESkillGroup.MOVE);
		
		runTime = time;
	}
	

	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		if (startTime == 0)
		{
			startTime = System.nanoTime();
		}
		
		Vector2 move = new Vector2(0f, 0.5f);
		// move.scaleTo(AIConfig.getSkills().getMaxVelocity());
		
		cmds.add(new TigerMotorMoveV2(move, 0f));
		
		if ((System.nanoTime() - startTime) / 1000000 > runTime)
		{
			cmds.add(new TigerMotorMoveV2(new Vector2(0, 0), 0f));
			
			complete();
		}
		
		return cmds;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		return false;
	}
}
