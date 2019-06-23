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
import edu.dhbw.mannheim.tigers.sumatra.util.StopWatch;


/**
 * a pid controller for the rotate skill
 * a bit hacking for rotate-skill, SO DON'T USE IT FOR OTHER THINGS^^
 * @author DanielW, ChristianK
 */
public class PIDControllerRotate
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private float				k_p;
	private float				k_i;
	private float				k_d;
	
	private final StopWatch	stopwatch		= new StopWatch();
	private float				previousError	= 0.0f;
	private float				errorSum			= 0.0f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public PIDControllerRotate(float k_p, float k_i, float k_d)
	{
		this.k_p = k_p;
		this.k_i = k_i;
		this.k_d = k_d;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public float process(int botID, float actualValue, float setpoint, float ts)
	{
		float t = stopwatch.stop(botID) / 1000f;
		if (t > 30000)
		{
			t = 0;
		}
		
		float error = setpoint - actualValue;
		t /= 1000000;
		

		// for rotate-skill only
		error = AIMath.normalizeAngle(error);
		//
		// System.out.println("error a: " + error);
		// System.out.println("ts: " + t);
		// System.out.println("error sum:" + errorSum);
		
		errorSum += error;
		float output = k_p * error + k_i * t * errorSum + k_d * (error - previousError) / t;
		
		// if (Math.abs(error - previousError) > Math.PI)
		// zeroIntegrator();
		
		previousError = error;
		

		// System.out.println("kp: " + k_p);
		// System.out.println("ki: " + k_i);
		// System.out.println("kd: " + k_d);
		
		return output;
	}
	

	// public void zeroIntegrator() // i don't know, what's this for...
	// {
	// this.errorSum = 0.0f;
	// }
	

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
		return previousError;
	}
}
