/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.10.2010
 * Author(s): ChristianK
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.PathPlanningRole;

/**
 * Play for Bernhards PP
 * 
 * @author ChristianK
 * 
 */
public class PathPlanningPlay extends APlay
{
/**  */
	private static final long	serialVersionUID	= -1773415820553181234L;
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private PathPlanningRole testBot;

	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PathPlanningPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.PP_PLAY, aiFrame);
				
		testBot = new PathPlanningRole();
		addAggressiveRole(testBot);
		// SkillTester testBot2 = new SkillTester();
		// addAggressiveRole(testBot2);
	}

	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
 
	@Override
	public int calcPlayableScore(AIInfoFrame currentFrame)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
