/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.06.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * this skill moves the robot towards a position as fast as possible.
 * note: bot wont stop at that position, but will start decelerating there
 * 
 * @author DanielW
 * 
 */
public class MoveFast extends ASkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final float		MAX_VELOCITY	= AIConfig.getSkills().getMaxVelocity();
	private final IVector2	dest;
	private IVector2			originalMove	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * this skill moves the robot towards a position as fast as possible.
	 * note: bot wont stop at that position, but will start decelerating there
	 * @param dest
	 */
	public MoveFast(IVector2 dest)
	{
		super(ESkillName.MOVE_FAST, ESkillGroup.MOVE);
		this.dest = dest;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		MoveFast move = (MoveFast) newSkill;
		
		return this.dest.equals(move.dest);
	}
	

	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		// Safety check
		TrackedTigerBot bot = getBot();
		if (bot == null)
		{
			return cmds;
		}
		
		if (originalMove == null)
			originalMove = dest.subtractNew(getBot().pos);
		
		IVector2 extendedDest = dest.addNew(originalMove.scaleToNew(1000));
		
		IVector2 moveVec = extendedDest.subtractNew(getBot().pos);
		

		// Check: Destination passed?
		if (dest.subtractNew(getBot().pos).scalarProduct(originalMove) <= 0)
		{
			cmds.add(new TigerMotorMoveV2(AVector2.ZERO_VECTOR, 0, 0)); // stop
			complete();
			return cmds;
		}
		

		// apply velocity with accelerate function
		moveVec = AMoveSkillV2.accelerateComplex(moveVec, MAX_VELOCITY, getBot());
		

		// ##### Convert to local bot-system
		moveVec = moveVec.turnNew(AIMath.PI_HALF - getBot().angle);
		
		cmds.add(new TigerMotorMoveV2(moveVec, 0, 0));
		

		return cmds;
	}
	

}
