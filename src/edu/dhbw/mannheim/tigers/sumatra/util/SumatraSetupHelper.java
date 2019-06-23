/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

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
 * 
 */
public final class SumatraSetupHelper
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log	= Logger.getLogger(SumatraSetupHelper.class.getName());
	
	
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
	 */
	public static void setupSumatra()
	{
		Sumatra.touch();
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_AI_CONFIG, AAgent.VALUE_AI_CONFIG);
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_GEOMETRY_CONFIG, AAgent.VALUE_GEOMETRY_CONFIG);
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_BOT_CONFIG, AAgent.VALUE_BOT_CONFIG);
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_TEAM_CONFIG, AAgent.VALUE_TEAM_CONFIG);
		ConfigManager.getInstance();
		
		try
		{
			SumatraModel.getInstance().loadModules("config/moduli/moduli_sim.xml");
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
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
