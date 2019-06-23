/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public final class MqttIdGenerator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static Set<String>	ids;
	private static String		userName;
	private static Random		rand;
	
	private static final int	MAX_ID_LENGTH	= 23;
	
	static
	{
		ids = new HashSet<String>();
		userName = System.getProperty("user.name") + ".";
		rand = new Random(System.currentTimeMillis());
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private MqttIdGenerator()
	{
	}
	
	
	/**
	 * creates a new Mqtt client Id
	 * @return
	 */
	public static String newId()
	{
		String id = userName + rand.nextInt();
		if (id.length() >= MAX_ID_LENGTH)
		{
			id = id.substring(0, MAX_ID_LENGTH - 1);
		}
		boolean state = ids.add(id);
		
		// Set already contains id
		if (!state)
		{
			id = userName + System.nanoTime();
			id = id.substring(0, MAX_ID_LENGTH - 1);
			// assume now that user id is unique, as a low possibility that the nanos are equal to two calls
			ids.add(id);
		}
		return id;
		
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
