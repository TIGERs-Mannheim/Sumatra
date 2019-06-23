/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.aicenter.view;

import edu.tigers.sumatra.ai.pandora.roles.ARole;


/**
 * This observer gets called if someone interacts with the {@link RoleControlPanel}.
 * 
 * @author Gero
 */
public interface IRoleControlPanelObserver
{
	/**
	 * @param role
	 * @param botId botId to be assigned
	 */
	void addRole(ARole role, int botId);
	
	
	/**
	 * @param role
	 */
	void removeRole(ARole role);
	
	
	/**
	 * Clear all roles
	 */
	void clearRoles();
}
