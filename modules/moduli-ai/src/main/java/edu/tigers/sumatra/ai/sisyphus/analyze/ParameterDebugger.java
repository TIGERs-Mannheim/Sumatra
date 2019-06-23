/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 15, 2012
 * Author(s): dirk
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.analyze;

import java.util.LinkedList;
import java.util.List;

import edu.tigers.sumatra.ai.sisyphus.errt.TuneableParameter;


/**
 * a debugger integrated in the path planner, several methods have to be called from the path planner:
 * 1. start / stop calculation
 * 2. goal reached
 * 3. rambo mode used / crash occured
 * 
 * @author dirk
 */
public class ParameterDebugger
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<PathInformation>	configurations			= new LinkedList<PathInformation>();
	private PathInformation					currentlyTested;
	private boolean							testing					= false;
	private long								calculationStarted	= 0;
																				
																				
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ParameterDebugger()
	{
		currentlyTested = new PathInformation(new TuneableParameter());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * call this at the beginning of each path calculation
	 */
	public void calculationStarted()
	{
		if (testing)
		{
			calculationStarted = System.nanoTime();
		}
	}
	
	
	/**
	 * call this after each path calculation
	 */
	public void calculationFinished()
	{
		if (testing)
		{
			currentlyTested.addCalculation(System.nanoTime() - calculationStarted);
		}
	}
	
	
	/**
	 * to be called if a goal of a path is reached
	 */
	public void goalReached()
	{
		if (testing)
		{
			currentlyTested.goalReached();
		}
	}
	
	
	/**
	 * call this if the bot crashed into another one (it used the rambo mode, effective but forbidden)
	 */
	public void ramboChosen()
	{
		if (testing)
		{
			currentlyTested.ramboChosen();
		}
	}
	
	
	/**
	 */
	public void secondTry()
	{
		if (testing)
		{
			currentlyTested.secondTry();
		}
	}
	
	
	/**
	 * @return a String representation of all path information gathered
	 */
	@Override
	public String toString()
	{
		StringBuilder output = new StringBuilder();
		for (final PathInformation pi : configurations)
		{
			output.append(pi.toString());
			output.append("\n");
		}
		return output.toString();
	}
	
	
	/**
	 * changes the parameters
	 * 
	 * @param adjustableParams
	 */
	public void changeParameterConfigToTest(final TuneableParameter adjustableParams)
	{
		configurations.add(currentlyTested);
		final PathInformation newCurrentlyTested = new PathInformation(adjustableParams);
		currentlyTested = newCurrentlyTested;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the testing
	 */
	public boolean isTesting()
	{
		return testing;
	}
	
	
	/**
	 * @param testing the testing to set
	 */
	public void setTesting(final boolean testing)
	{
		this.testing = testing;
	}
}
