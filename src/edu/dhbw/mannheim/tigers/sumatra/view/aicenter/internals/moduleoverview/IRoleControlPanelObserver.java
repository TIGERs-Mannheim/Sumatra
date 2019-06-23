/*
 * *********************************************************
 * 
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
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
	void addRole(ERole role);
	
	
	/**
	 * The given {@link ERole} should be executed on the bot identified by the given id
	 * 
	 * @param role
	 * @param botId
	 */
	void addRole(ERole role, BotID botId);
	
	
	/**
	 * Remove the specified role
	 * TODO Gero: Undefined behavior, as one should use the BOTID as identifier instead of ERole (Gero)
	 * 
	 * @param role
	 */
	void removeRole(ERole role);
	
	
	/**
	 * Removes all roles
	 */
	void clearRoles();
}
