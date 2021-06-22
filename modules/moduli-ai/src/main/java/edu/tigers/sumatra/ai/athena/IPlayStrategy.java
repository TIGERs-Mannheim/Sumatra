/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Interface for accessing {@link PlayStrategy} without modifiers
 */
public interface IPlayStrategy
{

	/**
	 * @return
	 */
	Set<APlay> getActivePlays();


	/**
	 * Warn: This will construct a new map each time it is called!
	 *
	 * @return
	 */
	Map<BotID, ARole> getActiveRoles();


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
}
