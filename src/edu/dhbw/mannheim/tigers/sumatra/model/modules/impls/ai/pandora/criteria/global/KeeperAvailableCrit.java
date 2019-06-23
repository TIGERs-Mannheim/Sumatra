/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * Checks if the current keeper id announced by the referee is one of our ids.
 * This criterion is needed for mixed team challenge where it is possible that we have no keeper
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class KeeperAvailableCrit extends ACriterion
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public KeeperAvailableCrit()
	{
		super(ECriterion.KEEPER_AVAILABLE);
	}
	
	
	@Override
	protected float doCheckCriterion(AIInfoFrame currentFrame)
	{
		if (currentFrame.refereeMsgCached != null)
		{
			BotID botID = new BotID(currentFrame.refereeMsgCached.getTeamInfoTigers().getGoalie());
			if (currentFrame.worldFrame.tigerBotsAvailable.getWithNull(botID) == null)
			{
				return MIN_SCORE;
			}
		}
		return MAX_SCORE;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
