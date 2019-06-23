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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * This observer gets called if someone interacts with the {@link RoleControlPanel}.
 * 
 * @author Gero
 */
public interface IRoleControlPanelObserver
{
	/**
	 * @param role
	 * @param botId botId to be assigned or uninitialized botId, if auto assign
	 */
	void addRole(ARole role, BotID botId);
	
	
	/**
	 * @param role
	 */
	void removeRole(ARole role);
}
