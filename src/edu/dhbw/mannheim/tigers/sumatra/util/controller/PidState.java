/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.07.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.controller;

/**
 * Simple data holder for the state of a PID controller
 * 
 * @author Gero
 */
public class PidState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public final float	error;
	public final float	error1;
	public final float	error2;
	
	public final float	lastOutput;
	public final float	previousOutput;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PidState(PidState original)
	{
		this(original.error, original.error1, original.error2, original.lastOutput, original.previousOutput);
	}
	

	public PidState(float error, float error1, float error2, float lastOutput, float previousOutput)
	{
		this.error = error;
		this.error1 = error1;
		this.error2 = error2;
		this.lastOutput = lastOutput;
		this.previousOutput = previousOutput;
	}
}
