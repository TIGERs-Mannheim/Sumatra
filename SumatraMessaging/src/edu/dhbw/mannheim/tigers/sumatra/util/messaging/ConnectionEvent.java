/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 1, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging;

/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class ConnectionEvent
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @author Daniel Andres <andreslopez.daniel@gmail.com>
	 */
	public enum ConnectionEventType
	{
		/** Called when connected */
		CONNECTED,
		/** Called when disconnected */
		DISCONNECTIED,
		/** Called when connection is lost */
		CONNECTION_LOST,
		/** Called when connection was reconnected */
		RECONNECTED;
	}
	
}
