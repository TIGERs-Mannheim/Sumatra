/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 30, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm;

/**
 * Control the messaging connection
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
