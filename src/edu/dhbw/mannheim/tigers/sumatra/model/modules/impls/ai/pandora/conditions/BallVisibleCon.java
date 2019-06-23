/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s):
 * Gunther Berthold
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;


/**
 * Implementation of {@link AVisibleCon} which checks if
 * there are obstacle between ball and the robot.
 * 
 * @author DanielW, Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class BallVisibleCon extends AVisibleCon
{
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This creates a ball visible condition.
	 */
	public BallVisibleCon()
	{
		super(ECondition.BALL_VISIBLE);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean doCheckCondition(WorldFrame worldFrame, int botID)
	{
		
		TrackedTigerBot bot = worldFrame.tigerBots.get(botID);
		updateStart(bot.pos);
		updateEnd(worldFrame.ball.pos);
		addToIgnore(botID);
		
		return checkVisibility(worldFrame);
	}
	

}
