/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2011
 * Author(s): Oliver Steinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.controller;

/**
 * PID contoller configuration.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class PIDControllerConfig
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	public float	p;
	public float	i;
	public float	d;
	public float	maxOutput;
	public float	slewRate;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public PIDControllerConfig(float p, float i, float d, float maxOutputValue)
	{
		this.p = p;
		this.i = i;
		this.d = d;
		this.maxOutput = maxOutputValue;
		this.slewRate = Float.MAX_VALUE;
	}
	

	public PIDControllerConfig(float p, float i, float d, float maxOutputValue, float slewRate)
	{
		this.p = p;
		this.i = i;
		this.d = d;
		this.maxOutput = maxOutputValue;
		this.slewRate = slewRate;
	}
	

	public PIDControllerConfig()
	{
		this.p = 0;
		this.i = 0;
		this.d = 0;
		this.maxOutput = Float.MAX_VALUE;
		this.slewRate = Float.MAX_VALUE;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
