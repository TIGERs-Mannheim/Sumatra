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

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * abstract superclass for every subordinal calculator of the Analyzer
 * 
 * @author Gunther, Oliver Steinbrecher <OST1988@aol.com>
 */
public abstract class ACalculator
{
	private static final Logger	log						= Logger.getLogger(ACalculator.class.getName());
	private boolean					active					= true;
	
	private long						lastCalculationTime	= 0;
	
	private ECalculator				type						= null;
	
	private Exception					lastException			= null;
	
	
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
		if (active && ((type == null)
				|| (TimeUnit.NANOSECONDS.toMillis(SumatraClock.nanoTime() - lastCalculationTime) >= type.getTimeRateMs())))
		{
			try
			{
				doCalc(newTacticalField, baseAiFrame);
				lastException = null;
			} catch (Exception err)
			{
				if ((lastException == null) || ((err.getMessage() != null)
						&& !err.getMessage().equals(lastException.getMessage())))
				{
					log.error("Error in calculator " + getType().name(), err);
				}
				lastException = err;
				fallbackCalc(newTacticalField, baseAiFrame);
			}
			lastCalculationTime = SumatraClock.nanoTime();
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
	 * Called before destruction
	 */
	public void deinit()
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
