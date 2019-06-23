/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 8, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.calibrate;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.SkillTestRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipAutoSkill;


/**
 * Tests the chip kick role
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ChipKickCalibratePlay extends ACalibratePlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private SkillTestRole	chipKickRole;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public ChipKickCalibratePlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected boolean doAfterUpdate(AIInfoFrame currentFrame)
	{
		if (chipKickRole.isCompleted())
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	protected void doPrepareNext(IVector2 target, int run)
	{
		chipKickRole = new SkillTestRole(new ChipAutoSkill(getDurations().get(run)));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected ARole getDoerRole()
	{
		return chipKickRole;
	}
	
	
	@Override
	protected void fillDurations()
	{
		getDurations().add(1000f);
		getDurations().add(2000f);
		getDurations().add(3000f);
		getDurations().add(4000f);
		getDurations().add(5000f);
		getDurations().add(6000f);
		getDurations().add(7000f);
		getDurations().add(8000f);
		getDurations().add(9000f);
		getDurations().add(10000f);
	}
	
	
	@Override
	protected String getIdentifier()
	{
		return "Chip";
	}
}
