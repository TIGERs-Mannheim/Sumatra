/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import java.util.List;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotIDMap;


/**
 * Interface for accessing {@link PlayStrategy} without modifiers
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPlayStrategy
{
	
	/**
	 * @return
	 */
	List<APlay> getActivePlays();
	
	
	/**
	 * @return
	 */
	BotIDMap<ARole> getActiveRoles();
	
	
	/**
	 * @param roleType
	 * @return
	 */
	List<ARole> getActiveRoles(final ERole roleType);
	
	
	/**
	 * @param playType
	 * @return
	 */
	List<ARole> getActiveRoles(final EPlay playType);
	
	
	/**
	 * @return
	 */
	List<APlay> getFinishedPlays();
	
	
	/**
	 * @return
	 */
	int getNumRoles();
	
	
	/**
	 * @return Athena's controlState
	 */
	EAIControlState getAIControlState();
}