/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
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
	 * Warn: This will construct a new map each time it is called!
	 *
	 * @return
	 */
	BotIDMap<ARole> getActiveRoles();
	
	
	/**
	 * Get roles by type
	 *
	 * @param roleTypes
	 * @return
	 */
	List<ARole> getActiveRoles(final ERole... roleTypes);
	
	
	/**
	 * Get roles by play
	 *
	 * @param playType
	 * @return
	 */
	List<ARole> getActiveRoles(final EPlay playType);
	
	
	/**
	 * @return the unmodifiable role mapping
	 */
	Map<EPlay, RoleMapping> getRoleMapping();
}