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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;


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
	
	
	private boolean	active	= true;
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Calculation call from extern
	 * @param curFrame
	 * @param preFrame
	 */
	public void calculate(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		if (active)
		{
			doCalc(curFrame, preFrame);
		} else
		{
			fallbackCalc(curFrame, preFrame);
		}
	}
	
	
	/**
	 * This function should be used to analyze something. It will modifies the the current aiframe (parameter)
	 * @param curFrame The current {@link AIInfoFrame}
	 * @param preFrame The previous {@link AIInfoFrame}
	 * 
	 *           return Object (Pay attention, maybe the return object has to be typecasted)
	 */
	public abstract void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame);
	
	
	/**
	 * This method will be called if a calculator shouldn't be executed. It should set a default value to the tactical
	 * field, so that there won't be errors of uninitialized values.
	 * @param curFrame
	 * @param preFrame
	 */
	public abstract void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame);
	
	
	/**
	 * Activates or deactivates this calculator
	 * @param active
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}
}
