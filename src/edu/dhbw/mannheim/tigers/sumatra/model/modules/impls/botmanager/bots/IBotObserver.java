/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;


/**
 * ABot observer interface.
 * 
 * @author AndreR
 * 
 */
public interface IBotObserver
{
	/**
	 * 
	 * @param name
	 */
	void onNameChanged(String name);
	
	
	/**
	 * 
	 * @param oldId
	 * @param newId
	 */
	void onIdChanged(BotID oldId, BotID newId);
	
	
	/**
	 * 
	 * @param state
	 */
	void onNetworkStateChanged(ENetworkState state);
	
	
	/**
	 * 
	 * @param blocked
	 */
	void onBlocked(boolean blocked);
}
