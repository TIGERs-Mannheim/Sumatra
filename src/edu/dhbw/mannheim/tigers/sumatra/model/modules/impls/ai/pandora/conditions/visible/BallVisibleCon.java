/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s):
 * Gunther Berthold
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;


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
	public EConditionState doCheckCondition(WorldFrame worldFrame, BotID botID)
	{
		
		TrackedTigerBot bot = worldFrame.tigerBotsVisible.get(botID);
		updateStart(bot.getPos());
		updateEnd(worldFrame.ball.getPos());
		addToIgnore(botID);
		
		return checkVisibility(worldFrame) ? EConditionState.FULFILLED : EConditionState.BLOCKED;
	}
	
}
