/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.10.2010
 * Author(s):
 * Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;


/**
 * Implementation of {@link AVisibleCon} which checks if
 * there are obstacle between the robot and a specified target.
 * <strong>Note: </strong>if the target is a bot-position, do not forget to add that bot to the ignore-list (via
 * {@linkplain #addToIgnore(int)})
 * @author DanielW, Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class TargetVisibleCon extends AVisibleCon
{
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public TargetVisibleCon()
	{
		super(ECondition.TARGET_VISIBLE);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean doCheckCondition(WorldFrame worldFrame, int botID)
	{
		TrackedTigerBot bot = worldFrame.tigerBots.get(botID);
		updateStart(new Vector2(bot.pos));
		addToIgnore(botID);
		
		return checkVisibility(worldFrame);
	}
	

	/**
	 * updates the target-point
	 * 
	 * @param target
	 */
	public void updateTarget(Vector2 target)
	{
		super.updateEnd(target);
	}
	

	@Override
	public void addToIgnore(int botId)
	{
		super.addToIgnore(botId);
	}
	

	public IVector2 getTarget()
	{
		return super.getEnd();
	}
}
