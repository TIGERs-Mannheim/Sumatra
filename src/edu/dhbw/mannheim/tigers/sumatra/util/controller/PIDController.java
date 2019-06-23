/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.09.2010
 * Author(s): Bernhards Betreuer, Christian König
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.controller;

/**
 * Basics are from Bernhard's tutor
 * 
 * it's a PID-Controller, i hope^^
 * 
 * Use this PID-Controller for normal things
 */
public class PIDController
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private float k_p;
	private float k_i;
	private float k_d;
	
	private float previousError;
	private float integral;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PIDController(float k_p, float k_i, float k_d) 
	{
		this.k_p = k_p;
		this.k_i = k_i;
		this.k_d = k_d;
		previousError = 0.0f;
		integral = 0.0f;
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public float process(float actualValue, float setpoint, float Ts) 
	{
		float error = setpoint - actualValue;
				
		this.integral += error * Ts;
		float derivative = (error - previousError)/Ts;
		float output = (k_p*error) + (k_i*integral) + (k_d*derivative);
		this.previousError = error;
		
		return output;
	}
	
	public void zeroIntegrator()  //i don't know, what's this for...
	{
		this.integral = 0.0f;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public float getK_p() 
	{
		return k_p;
	}

	public void setK_p(float k_p) 
	{
		this.k_p = k_p;
	}

	public float getK_i() 
	{
		return k_i;
	}

	public void setK_i(float k_i) 
	{
		this.k_i = k_i;
	}

	public float getK_d() 
	{
		return k_d;
	}

	public void setK_d(float k_d) 
	{
		this.k_d = k_d;
	}	
}
