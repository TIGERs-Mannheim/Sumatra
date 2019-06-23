/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;

/**
 * This observer gets called if someone interacts with the {@link RoleControlPanel}.
 * 
 * @author Gero
 */
public interface IRoleControlPanelObserver
{
	/**
	 * The given {@link ERole} should be executed
	 * 
	 * @param role
	 */
	public void addRole(ERole role);
	
	
	/**
	 * The given {@link ERole} should be executed on the bot identified by the given id
	 * 
	 * @param role
	 * @param botId
	 */
	public void addRole(ERole role, int botId);
	
	
	/**
	 * Remove the specified role
	 * TODO Gero: Undefined behavior, as one should use the BOTID as identifier instead of ERole (Gero)
	 * 
	 * @param role
	 */
	public void removeRole(ERole role);
	
	
	/**
	 * Removes all roles
	 */
	public void clearRoles();
}
