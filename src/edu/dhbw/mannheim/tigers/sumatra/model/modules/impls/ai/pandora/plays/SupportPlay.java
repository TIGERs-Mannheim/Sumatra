/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 20, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.SupportRole;


/**
 * Support play manages the support roles. This are all roles that are not offensive or defensive.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 *         Simon Sander <Simon.Sander@dlr.de>
 */
public class SupportPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public SupportPlay()
	{
		super(EPlay.SUPPORT);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected ARole onRemoveRole()
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		return new SupportRole();
	}
	
	
	@Override
	protected void onGameStateChanged(EGameState gameState)
	{
		// following msgs must be covered, but they may be covered by the OffensiveRole
		switch (gameState)
		{
			case PREPARE_KICKOFF_THEY:
				// TODO wait until ball moving (or better until something in Metis detected it)
				break;
			case PREPARE_KICKOFF_WE:
				// TODO wait for normal start remember here, that there is kickoff
				break;
			case PREPARE_PENALTY_THEY:
				// TODO wait anywhere to catch the ball after shot
				break;
			case PREPARE_PENALTY_WE:
				// TODO do the penalty shot myself or do nothing and let a specialized role do the work
				break;
			case STOPPED:
				// TODO keep distance to ball
				break;
			case RUNNING:
			case CORNER_KICK_THEY:
			case GOAL_KICK_THEY:
			case THROW_IN_THEY:
			case DIRECT_KICK_THEY:
				// TODO keep distance to ball
				break;
			case CORNER_KICK_WE:
			case GOAL_KICK_WE:
			case THROW_IN_WE:
			case DIRECT_KICK_WE:
				break;
			default:
				break;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
