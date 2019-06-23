/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 15, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.analyze;

/**
 * data holder for information concerning pathes
 * 
 * This classes are created by a test play to analyse the performance of the path planning and how long the bot needs to
 * reach the goal
 * 
 * @author dirk
 * 
 */
public class PathInformation
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int								calculationsAmount	= 0;
	private int								pathAmount				= 0;
	private int								ramboAmount				= 0;
	private int								secondTry				= 0;
	private long							midCalculationTime	= 0;
	private long							minCalculationTime	= Long.MAX_VALUE;
	private long							maxCalculationTime	= 0;
	private long							midCompleteTime		= 0;
	private long							minCompleteTime		= Long.MAX_VALUE;
	private long							maxCompleteTime		= 0;
	private long							startTime;
	private final TuneableParameter	accordingParams;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param accordingParams
	 */
	public PathInformation(TuneableParameter accordingParams)
	{
		this.accordingParams = accordingParams;
		startTime = System.nanoTime();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * adds a calculation to the information base
	 * @param neededTime the duration which was needed for the calculation of one path
	 */
	public void addCalculation(long neededTime)
	{
		calculationsAmount++;
		if (neededTime < minCalculationTime)
		{
			minCalculationTime = neededTime;
		}
		if (neededTime > maxCalculationTime)
		{
			maxCalculationTime = neededTime;
		}
		midCalculationTime = ((midCalculationTime * (calculationsAmount - 1)) + neededTime) / calculationsAmount;
	}
	
	
	/**
	 * a goal was reached, the driving time is measured
	 */
	public void goalReached()
	{
		pathAmount++;
		final long endTime = System.nanoTime();
		final long completeTime = endTime - startTime;
		if (completeTime < minCompleteTime)
		{
			minCompleteTime = completeTime;
		}
		if (completeTime > maxCompleteTime)
		{
			maxCompleteTime = completeTime;
		}
		midCompleteTime = ((midCompleteTime * (pathAmount - 1)) + completeTime) / pathAmount;
		startTime = System.nanoTime();
	}
	
	
	/**
	 * is called when the rambo mode (direct path despite bots in the way) was uesed
	 * 
	 */
	public void ramboChosen()
	{
		ramboAmount++;
	}
	
	
	/**
	 * is called when the path was found in the second try
	 * 
	 */
	public void secondTry()
	{
		secondTry++;
	}
	
	
	/**
	 * String representation of all gathered information
	 */
	@Override
	public String toString()
	{
		String output = "";
		output += accordingParams.toString();
		output += " " + minCalculationTime;
		output += " " + midCalculationTime;
		output += " " + maxCalculationTime;
		output += " " + minCompleteTime;
		output += " " + midCompleteTime;
		output += " " + maxCompleteTime;
		output += " " + calculationsAmount;
		output += " " + secondTry;
		output += " " + ramboAmount;
		return output;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public TuneableParameter getAccordingParams()
	{
		return accordingParams;
	}
	
	
}
