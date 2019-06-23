/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.XMLConfiguration;


/**
 * Configuration object for the aiCenter gui.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class Gui
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final String	nodePath	= "gui.";
	
	private final float	farDistance;
	private final float	reachedDistance;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public Gui(XMLConfiguration configFile)
	{
		farDistance = configFile.getFloat(nodePath + "farDistance");
		reachedDistance = configFile.getFloat(nodePath + "reachedDistance");
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public float getFarDistance()
	{
		return farDistance;
	}
	

	public float getReachedDistance()
	{
		return reachedDistance;
	}
}
