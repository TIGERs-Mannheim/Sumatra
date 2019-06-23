/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillPositioningCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Testing performance of dynamically looking at ball
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class LookAtBallTestSkill extends ASkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2	destination;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	  * 
	  */
	public LookAtBallTestSkill()
	{
		super(ESkillName.LOOK_AT_BALL);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public List<ACommand> calcEntryActions(List<ACommand> cmds)
	{
		destination = getPos();
		return super.calcEntryActions(cmds);
	}
	
	
	@Override
	public List<ACommand> calcActions(List<ACommand> cmds)
	{
		float orientation = getWorldFrame().getBall().getPos().subtractNew(destination).getAngle();
		Vector2 dest = new Vector2(destination);
		if (getWorldFrame().isInverted())
		{
			dest.multiply(-1);
			orientation = AngleMath.normalizeAngle(orientation + AngleMath.PI);
		}
		cmds.add(new TigerSkillPositioningCommand(dest, orientation));
		return cmds;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
