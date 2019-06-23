/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.05.2011
 * Author(s): Oliver Steinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.moduli.AModule;


/**
 * This is the base class for every referee manager which observes the match
 * and generates referee cmds!
 * 
 * @author Oliver Steinbrecher
 * 
 */
public abstract class ARefereeManager extends AModule implements IWorldFrameConsumer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	public static final String	MODULE_TYPE					= "ARefereeManager";
	public static final String	MODULE_ID					= "refereemanager";
	
	// --- config ---
	public final static String	REFEREE_CONFIG_PATH		= "./config/referee/";
	public final static String	REFEREE_DEFAULT_CONFIG	= "referee_default.xml";
	public static String			currentConfig				= REFEREE_DEFAULT_CONFIG;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
