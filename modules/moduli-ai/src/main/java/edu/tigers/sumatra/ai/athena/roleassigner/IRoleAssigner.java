/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena.roleassigner;

import edu.tigers.sumatra.ai.athena.IPlayStrategy;


/**
 * This interface declares the methods for a RoleAssigner
 * 
 * @author MalteJ
 */
public interface IRoleAssigner
{
	/**
	 * Assigns the roles of activePlays to the assignees and may change the role counts of the plays.
	 * 
	 * @param playStrategy
	 */
	void assignRoles(final IPlayStrategy playStrategy);
}
