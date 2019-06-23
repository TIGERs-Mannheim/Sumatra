/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.DependencyException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.LoadModulesException;
import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;


/**
 * This class will help you setting up your JUnit tests for running more
 * complicated stuff from Sumatra
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class SumatraSetupHelper
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log;
	
	
	static
	{
		Sumatra.touch();
		log = Logger.getLogger(SumatraSetupHelper.class.getName());
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private SumatraSetupHelper()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Call all the static stuff of Sumatra, including logger, configs and modules
	 * Call this from within a static{} block in your Test class
	 * Make sure any Logger instance will not be instantiated before this method.
	 * You can assign values to a static final field in the static block as well ;)
	 */
	public static void setupSumatra()
	{
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_GEOMETRY_CONFIG, AAgent.VALUE_GEOMETRY_CONFIG);
		ConfigManager.getInstance();
		
		try
		{
			SumatraModel.getInstance().loadModules("config/moduli/moduli_sumatra.xml");
		} catch (LoadModulesException err)
		{
			log.error("", err);
		} catch (DependencyException err)
		{
			log.error("", err);
		}
	}
	
	
	/**
	 * Switch the logger of
	 */
	public static void noLogging()
	{
		Logger.getRootLogger().setLevel(Level.OFF);
	}
	
	
	/**
	 * @param lvl
	 */
	public static void changeLogLevel(final Level lvl)
	{
		Appender appender = Logger.getRootLogger().getAppender("console");
		if ((appender != null) && (appender instanceof ConsoleAppender))
		{
			((ConsoleAppender) appender).setThreshold(lvl);
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
