/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.rcm;

/**
 * Control the messaging connection
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface IMessagingGUIObserver
{
	
	/**
	 * Messaging should connect
	 */
	void onConnect();
	
	
	/**
	 * Messaging should disconnect
	 */
	void onDisconnect();
	
}
