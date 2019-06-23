/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2010
 * Author(s): bernhard
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model;

import edu.moduli.Moduli;


/**
 * The model of the application.
 * It contains the low-level-methods and data.
 * You can use the methods in a Presenter for combining
 * to a business - logic.
 * In Sumatra the Model is entirely outsourced in moduli.
 * That means, that all low-level-methods are within separated modules.
 * The model make use of a Singleton - pattern,
 * so you can access the Model
 * with a simple SumatraModel.getInstance() .
 * 
 * @author bernhard
 * 
 */
public class SumatraModel extends Moduli
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --- version ---
	public static String				VERSION						= "0.6";
	
	// --- singleton ---
	private static SumatraModel	instance;
	
	// --- moduli config ---
	public final static String		MODULI_CONFIG_PATH		= "./config/moduli/";
	public final static String		MODULI_DEFAULT_CONFIG	= "moduli_default.xml";
	private String						currentModuliConfig		= MODULI_DEFAULT_CONFIG;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Constructor from model.
	 * Initializes data which is kept by the model.
	 */
	private SumatraModel()
	{
	}
	

	/**
	 * getInstance() - Singleton-pattern.
	 * @return the "one-and-only" instance of CSModel
	 */
	public static SumatraModel getInstance()
	{
		if (instance == null)
		{
			instance = new SumatraModel();
		}
		return instance;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the currentModuliConfig
	 */
	public String getCurrentModuliConfig()
	{
		return currentModuliConfig;
	}
	

	/**
	 * @param currentModuliConfig the currentModuliConfig to set
	 */
	public void setCurrentModuliConfig(String currentModuliConfig)
	{
		this.currentModuliConfig = currentModuliConfig;
	}
	
}
