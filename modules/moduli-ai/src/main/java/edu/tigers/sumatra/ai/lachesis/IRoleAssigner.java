/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.11.2013
 * Author(s): jaue_ma
 * *********************************************************
 */
package edu.tigers.sumatra.ai.lachesis;

import java.util.List;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This interface declares the methods for a RoleAssigner
 * 
 * @author MalteJ
 */
public interface IRoleAssigner
{
	
	/**
	 * Assignes the roles of activePlays to the assignees and may change the role counts of the plays.
	 * 
	 * @param assignees the current available TigerBots
	 * @param activePlays the current active plays
	 * @param frame the current frame
	 */
	void assignRoles(BotIDMap<ITrackedBot> assignees, List<APlay> activePlays, AthenaAiFrame frame);
	
}
