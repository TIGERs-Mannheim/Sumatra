/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * abstract superclass for every subordinal calculator of the Analyzer
 */
public abstract class ACalculator
{
	private static final Logger log = LogManager.getLogger(ACalculator.class);
	private boolean active = true;
	private boolean started = false;
	private Exception lastException = null;
	private BaseAiFrame curAiFrame = null;
	private TacticalField newTacticalField = null;
	private boolean executionStatusLastFrame = false;
	
	
	/**
	 * Calculation call from extern
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	public final void calculate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		curAiFrame = baseAiFrame;
		this.newTacticalField = newTacticalField;
		executionStatusLastFrame = false;
		if (!active && started)
		{
			stop();
			started = false;
		}
		
		if (!started)
		{
			start();
			started = true;
		}
		
		if (!active || !isCalculationNecessary())
		{
			return;
		}
		
		try
		{
			doCalc();
			executionStatusLastFrame = true;
			lastException = null;
		} catch (Exception err)
		{
			if ((lastException == null) || ((err.getMessage() != null)
					&& !err.getMessage().equals(lastException.getMessage())))
			{
				log.error("Error in calculator " + getClass().getSimpleName(), err);
			}
			lastException = err;
		}
	}
	
	
	/**
	 * Do the calculations.
	 * Please use {@link #doCalc()} instead of this method.
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	protected void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
	}
	
	
	/**
	 * Do the calculations.
	 */
	protected void doCalc()
	{
		doCalc(getNewTacticalField(), getAiFrame());
	}
	
	
	/**
	 * Specifies if the doCalc method has to be executed.
	 * Please use {@link #isCalculationNecessary()} instead of this method.
	 *
	 * @param tacticalField
	 * @param aiFrame
	 * @return
	 */
	@SuppressWarnings("squid:S1172") // ignore unused parameters
	protected boolean isCalculationNecessary(TacticalField tacticalField, BaseAiFrame aiFrame)
	{
		return true;
	}
	
	
	/**
	 * Specifies if the doCalc method has to be executed
	 *
	 * @return
	 */
	protected boolean isCalculationNecessary()
	{
		return isCalculationNecessary(getNewTacticalField(), getAiFrame());
	}
	
	
	/**
	 * Called when calculator gets activated or AI gets started
	 */
	protected void start()
	{
		// can be overwritten
	}
	
	
	/**
	 * Called when calculator gets deactivated or AI gets stopped
	 */
	protected void stop()
	{
		// can be overwritten
	}
	
	
	/**
	 * Tear down this calculator by calling stop() if required
	 */
	public final void tearDown()
	{
		if (started)
		{
			stop();
			started = false;
		}
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
	
	
	protected BaseAiFrame getAiFrame()
	{
		return curAiFrame;
	}
	
	
	protected WorldFrame getWFrame()
	{
		return curAiFrame.getWorldFrame();
	}
	
	
	protected ITrackedBall getBall()
	{
		return curAiFrame.getWorldFrame().getBall();
	}
	
	
	public boolean getExecutionStatusLastFrame()
	{
		return executionStatusLastFrame;
	}
	
	
	public TacticalField getNewTacticalField()
	{
		return newTacticalField;
	}
}
