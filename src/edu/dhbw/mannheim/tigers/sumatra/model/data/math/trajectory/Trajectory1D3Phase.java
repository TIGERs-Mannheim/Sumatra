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
 * Trajectory with acceleration, decceleration and plateau phase
 * 
 * @author AndreR
 * 
 */
public class Trajectory1D3Phase implements ITrajectory1D
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private float		tp;
	private float		tc;
	private float		tm;
	private float		tot;
	private float		a;
	private float		vb;
	private float		vc;
	private float		ve;
	private boolean	invert;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Trajectory constuctor.
	 * 
	 * To be used by trajectory generator.
	 * 
	 * @param tp
	 * @param tm
	 * @param tc
	 * @param tot
	 * @param a
	 * @param vb
	 * @param vc
	 * @param ve
	 * @param invert
	 */
	public Trajectory1D3Phase(float tp, float tm, float tc, float tot, float a, float vb, float vc, float ve,
			boolean invert)
	{
		this.tp = tp;
		this.tc = tc;
		this.tm = tm;
		this.tot = tot;
		this.a = a;
		this.vb = vb;
		this.vc = vc;
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
			if (vb > vc)
			{
				result = vb - (a * t);
			} else
			{
				result = vb + (a * t);
			}
		} else if (t < (tp + tc))
		{
			result = vc;
		} else if (t < tot)
		{
			if (ve < vc)
			{
				result = vc - (a * (t - tp - tc));
			} else
			{
				result = vc + (a * (t - tp - tc));
			}
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
			if (vb > vc)
			{
				result = -a;
			} else
			{
				result = a;
			}
		} else if (t < (tp + tc))
		{
			result = 0;
		} else if (t < tot)
		{
			if (ve < vc)
			{
				result = -a;
			} else
			{
				result = a;
			}
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
		return tot;
	}
	
	
	/**
	 * Reduce the trajectory by a certain factor.
	 * 
	 * @param factor
	 * @return
	 */
	public ITrajectory1D reduce(float factor)
	{
		return new Trajectory1D3Phase(tp, tm, tc, tot, a * factor, vb, vc * factor, ve, invert);
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
