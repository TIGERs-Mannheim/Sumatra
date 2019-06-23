/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter.view;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ids.BotID;


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
