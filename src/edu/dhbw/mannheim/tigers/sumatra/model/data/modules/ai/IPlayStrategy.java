/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;


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