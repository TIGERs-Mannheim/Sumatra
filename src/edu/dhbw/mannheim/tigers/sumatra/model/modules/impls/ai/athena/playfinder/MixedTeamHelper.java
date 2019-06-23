/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 26, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder;

import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;


/**
 * Helper class to determine if mixedteam.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class MixedTeamHelper
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private boolean	mixedTeam;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param isMixedTeam
	 */
	public MixedTeamHelper(boolean isMixedTeam)
	{
		mixedTeam = isMixedTeam;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * 
	 * Check if a keeper exists
	 * 
	 * @param aiFrame
	 * @return true or false
	 */
	public boolean needsKeeper(AIInfoFrame aiFrame)
	{
		if (mixedTeam)
		{
			if ((aiFrame.refereeMsgCached != null))
			{
				BotID botID = new BotID(aiFrame.refereeMsgCached.getTeamInfoTigers().getGoalie());
				if (aiFrame.worldFrame.tigerBotsAvailable.containsKey(botID))
				{
					return true;
				}
				return false;
			}
			BotID botID = TeamConfig.getInstance().getTeam().getKeeperId();
			if (aiFrame.worldFrame.tigerBotsAvailable.containsKey(botID))
			{
				return true;
			}
			return false;
		}
		return true;
	}
	
	
	/**
	 * method for Mixed team. checks if a team member from the other team is nearer then ours
	 * @param curFrame
	 * @return true if tigers are nearer, false if others are nearer
	 */
	protected boolean tigerIsNearThenOtherTeam(AIInfoFrame curFrame)
	{
		if (mixedTeam)
		{
			IBotIDMap<TrackedTigerBot> tigerBots = curFrame.worldFrame.getTigerBotsAvailable();
			IBotIDMap<TrackedTigerBot> allFriendlyBots = curFrame.worldFrame.getTigerBotsVisible();
			IBotIDMap<TrackedTigerBot> otherFriendlyBots = AiMath.getOtherBots(curFrame);
			BotIDMap<TrackedBot> bots = new BotIDMap<TrackedBot>();
			for (Entry<BotID, TrackedTigerBot> bot : allFriendlyBots)
			{
				bots.put(bot.getKey(), bot.getValue());
			}
			BotID nearestBot = AiMath.getNearestBot(bots, curFrame.worldFrame.getBall().getPos()).getId();
			if (tigerBots.containsKey(nearestBot))
			{
				return true;
			} else if (otherFriendlyBots.containsKey(nearestBot))
			{
				return false;
			}
		}
		return true;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the mixedTeam
	 */
	public boolean isMixedTeam()
	{
		return mixedTeam;
	}
	
	
	/**
	 * Activate mixed team
	 * @param b
	 */
	public void setMixedTeam(boolean b)
	{
		mixedTeam = b;
	}
}
