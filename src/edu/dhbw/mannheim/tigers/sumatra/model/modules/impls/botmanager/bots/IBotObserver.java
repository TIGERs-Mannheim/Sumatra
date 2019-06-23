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

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;

/**
 * ABot observer interface.
 * 
 * @author AndreR
 * 
 */
public interface IBotObserver
{
	void onNameChanged(String name);
	void onIdChanged(int oldId, int newId);
	void onIpChanged(String ip);
	void onPortChanged(int port);
	void onNetworkStateChanged(ENetworkState state);
}
