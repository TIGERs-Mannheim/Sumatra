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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dna.mqtt.moquette.server.Server;


/**
 * Factory class for Messaging Connections.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public final class Messaging
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger							log				= Logger.getLogger(Messaging.class.getName());
	
	private static Map<Integer, MessageConnection>	connections;
	
	private static MessageConnection						defaultCon		= null;
	
	/** Default connection id. */
	private static final int								DEFAULT_ID		= 0;
	/** Default host */
	public static final String								DEFAULT_HOST	= "localhost";
	/** Default tcp port */
	public static final int									DEFAULT_PORT	= 1883;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	static
	{
		connections = new HashMap<Integer, MessageConnection>();
	}
	
	
	private Messaging()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Returns a special connection
	 * @param id
	 * @return
	 */
	public static MessageConnection getConnection(int id)
	{
		MessageConnection connection = connections.get(id);
		if (connection == null)
		{
			connection = new MessageConnection();
			connections.put(id, connection);
			if (id == DEFAULT_ID)
			{
				defaultCon = connection;
			}
		}
		return connection;
	}
	
	
	/**
	 * Returns the default connection. (cached from field)
	 * @return
	 */
	public static MessageConnection getDefaultCon()
	{
		if (defaultCon == null)
		{
			getConnection(DEFAULT_ID);
		}
		return defaultCon;
	}
	
	
	/**
	 * Starts the unstable Java broker Moquete. Use this ONLY as fallback
	 */
	public static void startJavaBroker()
	{
		String[] emptyParam = {};
		try
		{
			log.warn("Use unstable moquete broker. Please update connection details. ");
			Server.main(emptyParam);
		} catch (IOException err)
		{
			log.error("Moquete error", err);
		}
	}
	
	
	/**
	 * Clears all references form here, so it can be started again. This is used for tests.
	 */
	public static synchronized void clear()
	{
		connections = new HashMap<Integer, MessageConnection>();
	}
}
