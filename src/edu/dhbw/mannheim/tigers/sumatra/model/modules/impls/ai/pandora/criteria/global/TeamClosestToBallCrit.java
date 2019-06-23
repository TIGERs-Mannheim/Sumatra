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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
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
	
	private final ETeam	team;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param team
	 */
	public TeamClosestToBallCrit(ETeam team)
	{
		super(ECriterion.TEAM_CLOSEST_TO_BALL);
		
		this.team = team;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		if (currentFrame.tacticalInfo.getTeamClosestToBall() == team)
		{
			return MAX_SCORE;
		}
		return MIN_SCORE;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
