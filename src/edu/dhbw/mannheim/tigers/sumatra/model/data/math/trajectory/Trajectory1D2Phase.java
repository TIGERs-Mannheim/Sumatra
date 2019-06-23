/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.07.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

/**
 * Trajectory with a acceleration AND decceleration phase.
 * 
 * @author AndreR
 * 
 */
public class Trajectory1D2Phase implements ITrajectory1D
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private float		tp;		// accel time
	private float		tm;		// deccel time
	private float		a;		// acceleration
	private float		vb;		// begin velocity
	private float		vp;		// peak velocity
	private float		ve;		// end velocity
	private boolean	invert;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constuctor.
	 * 
	 * To be used by TrajectoryGenerator.
	 * 
	 * @param tp
	 * @param tm
	 * @param a
	 * @param vb
	 * @param vp
	 * @param ve
	 * @param invert
	 */
	public Trajectory1D2Phase(float tp, float tm, float a, float vb, float vp, float ve, boolean invert)
	{
		this.tp = tp;
		this.tm = tm;
		this.a = a;
		this.vb = vb;
		this.vp = vp;
		this.ve = ve;
		this.invert = invert;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float getVelocity(float t)
	{
		float result;
		
		if (t < 0)
		{
			result = vb;
		} else if (t < tp)
		{
			result = vb + (t * a);
		} else if (t < (tp + tm))
		{
			result = vp - ((t - tp) * a);
		} else
		{
			result = ve;
		}
		
		if (invert)
		{
			result *= -1.0f;
		}
		
		return result;
	}
	
	
	@Override
	public float getAcceleration(float t)
	{
		float result;
		
		if (t < 0)
		{
			result = 0;
		} else if (t < tp)
		{
			result = a;
		} else if (t < (tp + tm))
		{
			result = -a;
		} else
		{
			result = 0;
		}
		
		if (invert)
		{
			result *= -1.0f;
		}
		
		return result;
	}
	
	
	@Override
	public float getTotalTime()
	{
		return Math.abs(tp) + Math.abs(tm);
	}
	
	
	/**
	 * Reduce the trajectory by a certain factor.
	 * 
	 * @param factor
	 * @return
	 */
	public ITrajectory1D reduce(float factor)
	{
		return new Trajectory1D2Phase(tp, tm, a * factor, vb, vp * factor, ve, invert);
	}
	
	
	@Override
	@Deprecated
	/**
	 * THIS METHOD IS NOT IMPLEMENTED.
	 */
	public float getPosition(float t)
	{
		return 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
