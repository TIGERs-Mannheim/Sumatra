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
public abstract class ACalculator implements ICalculator
{
	private static final Logger log = LogManager.getLogger(ACalculator.class);
	private boolean active = true;
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
		if (!active || !isCalculationNecessary(newTacticalField, baseAiFrame))
		{
			return;
		}
		
		try
		{
			doCalc(newTacticalField, baseAiFrame);
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
