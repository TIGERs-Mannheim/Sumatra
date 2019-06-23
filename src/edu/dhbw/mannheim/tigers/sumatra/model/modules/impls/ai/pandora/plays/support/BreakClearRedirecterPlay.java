/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 19, 2012
 * Author(s): NicolaiO
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.support;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;


/**
 * Let a bot break clear (freispielen).
 * First approach is to choose a target randomly
 * 
 * @author NicolaiO
 * 
 */
public class BreakClearRedirecterPlay extends ASupportPlay
{
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public BreakClearRedirecterPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		IVector2 defaultPos = new Vector2((AIConfig.getGeometry().getFieldLength() / 4) * 3, 0);
		BotIDMap<TrackedTigerBot> bots = new BotIDMap<>(aiFrame.worldFrame.tigerBotsAvailable);
		for (int i = 0; i < getNumAssignedRoles(); i++)
		{
			BotID botId = AiMath.getReceiver(aiFrame, bots);
			bots.remove(botId);
			IVector2 pos = defaultPos;
			if ((botId != null) && botId.isBot())
			{
				TrackedTigerBot bot = aiFrame.worldFrame.getTiger(botId);
				if (bot != null)
				{
					pos = bot.getPos();
				}
			}
			RedirectRole role = new RedirectRole(pos, false);
			addDefensiveRole(role, pos);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame aiFrame)
	{
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
