/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.09.2010
 * Author(s):
 * Gunther
 * Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;


/**
 * abstract superclass for every subordinal calculator of the Analyzer
 * 
 * @author Gunther, Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public abstract class ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This function should be used to analyze something.
	 * @param curFrame The current {@link AIInfoFrame}
	 * 
	 * @return Object (Pay attention, maybe the return object has to be typecasted)
	 */
	public abstract Object calculate(AIInfoFrame curFrame);
}
