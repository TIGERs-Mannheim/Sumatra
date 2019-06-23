/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.11.2013
 * Author(s): jaue_ma
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;


/**
 * This interface declares the methods for a RoleAssigner
 * 
 * @author MalteJ
 */
public interface INewRoleAssigner
{
	
	/**
	 * Assignes the roles of activePlays to the assignees and may change the role counts of the plays.
	 * 
	 * @param assignees the current available TigerBots
	 * @param activePlays the current active plays
	 * @param frame the current frame
	 */
	void assignRoles(BotIDMap<TrackedTigerBot> assignees, List<APlay> activePlays, AthenaAiFrame frame);
	
}
