/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.calibrate;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.SkillTestRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickAutoSkill;


/**
 * Calibrate the straight kicker
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class StraightKickCalibratePlay extends ACalibratePlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private SkillTestRole	role;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public StraightKickCalibratePlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected boolean doAfterUpdate(AIInfoFrame currentFrame)
	{
		if (role.isCompleted())
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	protected void doPrepareNext(IVector2 target, int run)
	{
		role = new SkillTestRole(new KickAutoSkill(getDurations().get(run), 0));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected ARole getDoerRole()
	{
		return role;
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
		return "Straight";
	}
}
