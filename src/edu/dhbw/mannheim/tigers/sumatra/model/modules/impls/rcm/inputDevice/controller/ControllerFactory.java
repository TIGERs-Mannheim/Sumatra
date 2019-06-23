/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): Manuel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.controller;

import java.util.ArrayList;
import java.util.List;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.util.NativesLoader;
import edu.dhbw.mannheim.tigers.sumatra.util.OsDetector;


/**
 * 
 * @author Manuel
 * 
 */
public final class ControllerFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger					log					= Logger.getLogger(ControllerFactory.class.getName());
	private static volatile ControllerFactory	instance				= null;
	private final ArrayList<Controller>			controllers			= new ArrayList<Controller>();
	private final ArrayList<Controller>			usedControllers	= new ArrayList<Controller>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	static
	{
		// set library path for jinput
		final String curDir = System.getProperty("user.dir");
		System.setProperty("net.java.games.input.librarypath",
				curDir + "/lib/native/" + NativesLoader.DEFAULT_FOLDER_MAP.get(OsDetector.detectOs()));
	}
	
	
	private ControllerFactory()
	{
		updateControllers();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public static ControllerFactory getInstance()
	{
		if (instance == null)
		{
			instance = new ControllerFactory();
		}
		return instance;
	}
	
	
	/**
	 */
	public void updateControllers()
	{
		controllers.clear();
		usedControllers.clear();
		final ControllerEnvironment cEnv = ControllerEnvironment.getDefaultEnvironment();
		final Controller[] cs = cEnv.getControllers();
		for (final Controller controller : cs)
		{
			controllers.add(controller);
			log.info("Controller found: " + controller.getName());
		}
	}
	
	
	/**
	 * @param controller
	 * @return
	 */
	public boolean isUsed(Controller controller)
	{
		for (final Controller c : usedControllers)
		{
			if (c.equals(controller))
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * @param controller
	 */
	public void useController(Controller controller)
	{
		if (!isUsed(controller))
		{
			usedControllers.add(controller);
		}
	}
	
	
	/**
	 * @param controller
	 */
	public void unuseController(Controller controller)
	{
		if (isUsed(controller))
		{
			usedControllers.remove(controller);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public List<Controller> getAllControllers()
	{
		return controllers;
	}
	
	
	/**
	 * @return
	 */
	public List<Controller> getAllUsedControllers()
	{
		return usedControllers;
	}
}
