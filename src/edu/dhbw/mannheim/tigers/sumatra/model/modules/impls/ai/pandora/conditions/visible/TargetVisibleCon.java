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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;


/**
 * Implementation of {@link AVisibleCon} which checks if
 * there are obstacle between the robot and a specified target.
 * <strong>Note: </strong>if the target is a bot-position, do not forget to add that bot to the ignore-list (via
 * {@linkplain #addToIgnore(BotID)})
 * @author DanielW, Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class TargetVisibleCon extends AVisibleCon
{
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TargetVisibleCon()
	{
		super(ECondition.TARGET_VISIBLE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public EConditionState doCheckCondition(WorldFrame worldFrame, BotID botID)
	{
		final TrackedTigerBot bot = worldFrame.tigerBotsVisible.get(botID);
		updateStart(new Vector2(bot.getPos()));
		addToIgnore(botID);
		
		return checkVisibility(worldFrame) ? EConditionState.FULFILLED : EConditionState.BLOCKED;
	}
	
	
	/**
	 * updates the target-point
	 * 
	 * @param target
	 */
	public void updateTarget(IVector2 target)
	{
		super.updateEnd(target);
	}
	
	
	/**
	 * @return
	 */
	public IVector2 getTarget()
	{
		return super.getEnd();
	}
}
