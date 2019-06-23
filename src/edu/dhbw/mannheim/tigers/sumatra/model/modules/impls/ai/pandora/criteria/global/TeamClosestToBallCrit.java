/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.03.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * This class determines whether the bot closest to ball is on of ours or one
 * of our opponents. If the current situation relates to the desired situation
 * the value 1 will be returned. Otherwise the parameter 'penaltyFactor' will
 * be returned.
 * 
 * @author FlorianS
 * 
 */
public class TeamClosestToBallCrit extends ACriterion
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private ETeam	wish	= ETeam.UNKNOWN;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public TeamClosestToBallCrit(ETeam wish, float penaltyFactor)
	{
		super(ECriterion.TEAM_CLOSEST_TO_BALL, penaltyFactor);
		
		this.wish = wish;
	}
	
	
	public TeamClosestToBallCrit(ETeam wish)
	{
		super(ECriterion.TEAM_CLOSEST_TO_BALL);
		
		this.wish = wish;
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		if (currentFrame.tacticalInfo.getTeamClosestToBall() == wish)
		{
			return 1.0f;
		} else
		{
			return penaltyFactor;
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
