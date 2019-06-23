/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.09.2010
 * Author(s): DanielW, Bernhards Betreuer, Christian König
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.controller;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;


/**
 * a pid controller for the rotate skill (Z transformed)
 * a bit hacking for rotate-skill, SO DON'T USE IT FOR OTHER THINGS^^
 * @author DanielW, ChristianK, Gero
 */
public class PIDControllerRotateZ implements IPIDController
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private float	k_p;
	private float	k_i;
	private float	k_d;
	
	private float	errorT0		= 0.0f;
	private float	errorT1		= 0.0f;
	private float	errorT2		= 0.0f;
	
	private float	lastOutput	= 0.0f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PIDControllerRotateZ(float k_p, float k_i, float k_d)
	{
		this.k_p = k_p;
		this.k_i = k_i;
		this.k_d = k_d;
	}
	

	public PIDControllerRotateZ(PIDControllerConfig config)
	{
		this.k_p = config.p;
		this.k_i = config.i;
		this.k_d = config.d;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float process(float actualValue, float setPoint, double deltaT)
	{
		float error = setPoint - actualValue;
		// log.fatal("Error        :" + error);
		
		// for rotate-skill only
		error = AIMath.normalizeAngle(error);
		// log.fatal("Error (norm) :" + error);
		
		errorT2 = errorT1;
		errorT1 = errorT0;
		errorT0 = error;
		
		float pError = errorT0 - errorT1;
		float iError = errorT1;
		float dError = errorT0 - 2 * errorT1 + errorT2;
		
		// log.debug("pError: " + pError + " iError: " + iError + " dError: " + dError);
		
		lastOutput = lastOutput + (k_p * pError) + (k_i * iError) + (k_d * dError);
		
		// TODO handle max outputValue
		
		return lastOutput;
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
	

	/**
	 * @return the previousError
	 */
	public float getPreviousError()
	{
		return errorT0;
	}
	
	
	@Override
	public PidState getState()
	{
		return new PidState(errorT0, errorT1, errorT2, lastOutput, lastOutput);
	}
	
	
	@Override
	public void setState(PidState newState)
	{
		errorT0 = newState.error;
		errorT1 = newState.error1;
		errorT2 = newState.error2;
		
		lastOutput = newState.lastOutput;
	}
}
