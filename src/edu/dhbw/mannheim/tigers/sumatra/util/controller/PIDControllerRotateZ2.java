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
 * TODO Oliver, Add DOKU
 */
public class PIDControllerRotateZ2 implements IPIDController
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private float			k_p;
	private float			k_i;
	private float			k_d;
	
	private float			error			= 0.0f;
	private float			error1		= 0.0f;
	private float			error2		= 0.0f;
	
	private float			lastOutput	= 0.0f;

	private final float	MAX_OUTPUT;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * Ctor for an new PID Rotate controller.
	 * 
	 * @param k_p
	 * @param k_i
	 * @param k_d
	 * @param maxOutput
	 */
	public PIDControllerRotateZ2(float k_p, float k_i, float k_d, float maxOutput)
	{
		this.MAX_OUTPUT = maxOutput;
		this.k_p = k_p;
		this.k_i = k_i;
		this.k_d = k_d;
	}

	public PIDControllerRotateZ2(PIDControllerConfig config)
	{
		this.MAX_OUTPUT = config.maxOutput;
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
		error2 = error1;
		error1 = error;
		
		error = setPoint - actualValue;
		
		error = AIMath.normalizeAngle(error);
		
		float k1 = k_p + k_i + k_d;
		float k2 = -k_p - 2 * k_d;
		float k3 = k_d;
		
		float delta = k1 * error + k2 * error1 + k3 * error2;
		lastOutput += delta;
		
		if (lastOutput > MAX_OUTPUT)
		{
			lastOutput = MAX_OUTPUT;
		} else if (lastOutput < -MAX_OUTPUT)
		{
			lastOutput = -MAX_OUTPUT;
		}
		
/*		if (lastOutput > previousOutput + rate)
		{
			lastOutput = previousOutput + rate;
		} else if (lastOutput < previousOutput - rate)
		{
			lastOutput = previousOutput - rate;
		}
	*/	
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
		return error;
	}
	

	@Override
	public PidState getState()
	{
		return new PidState(error, error1, error2, lastOutput, lastOutput);
	}
	
	
	@Override
	public void setState(PidState newState)
	{
		error = newState.error;
		error1 = newState.error1;
		error2 = newState.error2;
		
		lastOutput = newState.lastOutput;
	}
}
