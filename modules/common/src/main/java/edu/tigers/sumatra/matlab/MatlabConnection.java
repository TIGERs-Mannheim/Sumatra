/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.matlab;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;


/**
 * Utility class for connecting to a matlab instance
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class MatlabConnection
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger			log	= Logger.getLogger(MatlabConnection.class.getName());
	private static volatile MatlabProxy	proxy	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private MatlabConnection()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Get the application global proxy handle.
	 * If no proxy was created yet, one will be created
	 * by starting matlab or connecting to an existing instance.
	 * 
	 * @return
	 * @throws MatlabConnectionException
	 */
	public static synchronized MatlabProxy getMatlabProxy() throws MatlabConnectionException
	{
		if ((proxy != null) && proxy.isConnected())
		{
			return proxy;
		}
		
		log.info("Create MatlabProxy connection");
		MatlabProxyFactoryOptions.Builder proxyBuilder = new MatlabProxyFactoryOptions.Builder();
		proxyBuilder.setMatlabStartingDirectory(new File("./matlab"));
		proxyBuilder.setUsePreviouslyControlledSession(true);
		MatlabProxyFactory factory = new MatlabProxyFactory(proxyBuilder.build());
		
		try
		{
			proxy = factory.getProxy();
		} catch (MatlabConnectionException err)
		{
			log.error("Could not connect to matlab.", err);
			throw err;
		}
		
		changeToDefaultDir();
		
		log.info("MatlabProxy connection created.");
		return proxy;
	}
	
	
	/**
	 * Change to the default Sumatra matlab path
	 * 
	 * @throws MatlabConnectionException
	 */
	public static synchronized void changeToDefaultDir() throws MatlabConnectionException
	{
		Path currentRelativePath = Paths.get("");
		String currentAbsolutePath = currentRelativePath.toAbsolutePath().toString();
		String path = currentAbsolutePath + "/matlab";
		try
		{
			getMatlabProxy().eval("cd " + path);
			getMatlabProxy().eval("setup");
		} catch (MatlabInvocationException err)
		{
			log.warn("Could not change to default dir: " + path, err);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
