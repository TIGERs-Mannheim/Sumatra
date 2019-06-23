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

import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;


/**
 * abstract superclass for every subordinal calculator of the Analyzer
 * 
 * @author Gunther, Oliver Steinbrecher <OST1988@aol.com>
 */
public abstract class ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private boolean		active					= true;
	
	private long			lastCalculationTime	= 0;
	
	private ECalculator	type;
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Calculation call from extern
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	public final void calculate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (active && (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastCalculationTime) > type.getTimeRateMs()))
		{
			doCalc(newTacticalField, baseAiFrame);
			lastCalculationTime = System.nanoTime();
		} else
		{
			fallbackCalc(newTacticalField, baseAiFrame);
		}
	}
	
	
	/**
	 * This function should be used to analyze something.
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	public abstract void doCalc(TacticalField newTacticalField, BaseAiFrame baseAiFrame);
	
	
	/**
	 * This method will be called if a calculator shouldn't be executed. It should set a default value to the tactical
	 * field, so that there won't be errors of uninitialized values.
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
	}
	
	
	/**
	 * Activates or deactivates this calculator
	 * 
	 * @param active
	 */
	public void setActive(final boolean active)
	{
		this.active = active;
	}
	
	
	/**
	 * @return the type
	 */
	public final ECalculator getType()
	{
		return type;
	}
	
	
	/**
	 * @param type the type to set
	 */
	public final void setType(final ECalculator type)
	{
		this.type = type;
	}
}
