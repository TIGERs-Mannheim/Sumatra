/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.dhbw.mannheim.tigers.moduli.AModule;


/**
 * This type is meant to provide the data generated in the innermost of the Sumatra-Software to other systems. 'How' is
 * the implementations business.
 * 
 * @author Gero
 * 
 */
public abstract class AVisDataConnector extends AModule
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final String	MODULE_TYPE	= "AVisDataConnector";
	/** */
	public static final String	MODULE_ID	= "vis";
}
