/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistance;

import java.io.File;

import org.apache.log4j.Logger;
import org.zeroturnaround.zip.ZipUtil;


/**
 * Abstract berkeley persistence for all data independent actions.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ABerkeleyPersistence
{
	private static final Logger	log	= Logger.getLogger(ABerkeleyPersistence.class.getName());
	
	private final BerkeleyEnv		env;
	private final String				dbPath;
	
	
	/**
	 * @param dbPath
	 * @param readOnly
	 */
	public ABerkeleyPersistence(final String dbPath, final boolean readOnly)
	{
		if (dbPath.endsWith(".zip"))
		{
			this.dbPath = dbPath.substring(0, dbPath.length() - 4);
			if (!new File(this.dbPath).exists())
			{
				log.info("Unpacking database...");
				ZipUtil.unpack(new File(dbPath), new File(this.dbPath + "/.."));
				log.info("Unpacking finished.");
			}
		} else
		{
			this.dbPath = dbPath;
		}
		log.debug("Setting up database...");
		env = new BerkeleyEnv();
		File envHome = new File(this.dbPath);
		if (!envHome.exists())
		{
			boolean mkdirs = envHome.mkdirs();
			if (!mkdirs)
			{
				log.error("Could not create " + envHome);
			}
		}
		env.setup(envHome, readOnly);
		attachShutDownHook();
		log.debug("Database ready!");
	}
	
	
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
	
	
	/**
	 * @return the env
	 */
	public final BerkeleyEnv getEnv()
	{
		return env;
	}
}
