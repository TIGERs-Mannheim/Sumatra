/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance;

import java.io.File;

import org.apache.log4j.Logger;


/**
 * Abstract berkeley persistence for all data independent actions.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public abstract class ABerkeleyPersistence
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log	= Logger.getLogger(ABerkeleyPersistence.class.getName());
	
	private final BerkeleyEnv		env;
	private final String				dbPath;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param dbPath
	 * @param readOnly
	 */
	public ABerkeleyPersistence(String dbPath, boolean readOnly)
	{
		this.dbPath = dbPath;
		env = new BerkeleyEnv();
		File envHome = new File(dbPath);
		envHome.mkdirs();
		env.setup(envHome, readOnly);
		attachShutDownHook();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public final void close()
	{
		env.close();
	}
	
	
	private void attachShutDownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				if (env.isOpen())
				{
					log.warn("Database " + dbPath + " was open on shutdown. It would be better to close it explictly");
					close();
				}
			}
		}, "DB Shutdown"));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the env
	 */
	public final BerkeleyEnv getEnv()
	{
		return env;
	}
}
