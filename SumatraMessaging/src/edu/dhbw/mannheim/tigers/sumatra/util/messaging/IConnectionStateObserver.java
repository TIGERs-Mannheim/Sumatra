/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging;

/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public interface IConnectionStateObserver
{
	/**
	 * Invoked when the connection state has changed
	 * @param connected true if it is connected, false if not
	 */
	void onConnectionEvent(boolean connected);
}
