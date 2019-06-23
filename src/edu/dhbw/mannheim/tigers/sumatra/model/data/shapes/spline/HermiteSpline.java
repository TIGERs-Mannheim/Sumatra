/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.03.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;


/**
 * Hermite Cubic Spline.
 * This class only takes two points. For more complex splines with more points combine multiple HermiteSplines.
 * 
 * This implementation always references values on a t-axis from 0 to tEnd.
 * 
 * @author AndreR
 * 
 */
@Embeddable
public class HermiteSpline
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// package visibility of a,b,c,d,tEnd is intended! Used by HermiteSpline2D.
	float	a;
	float	b;
	float	c;
	float	d;
	float	tEnd;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Construct a hermite cubic spline.
	 * Uses tEnd = 1.0.
	 * 
	 * @param p0 Initial value.
	 * @param p1 Final value.
	 * @param m0 Initial slope.
	 * @param m1 Final slope.
	 */
	public HermiteSpline(float p0, float p1, float m0, float m1)
	{
		a = ((2 * p0) - (2 * p1)) + m0 + m1;
		b = ((-3 * p0) + (3 * p1)) - (2 * m0) - m1;
		c = m0;
		d = p0;
		tEnd = 1.0f;
	}
	
	
	/**
	 * Construct a hermite cubic spline.
	 * 
	 * @param p0 Initial value.
	 * @param p1 Final value.
	 * @param m0 Initial slope.
	 * @param m1 Final slope.
	 * @param tEnd End time.
	 */
	public HermiteSpline(float p0, float p1, float m0, float m1, float tEnd)
	{
		final float t2 = tEnd * tEnd;
		final float t3 = t2 * tEnd;
		a = (((2 * p0) / t3) - ((2 * p1) / t3)) + (m0 / t2) + (m1 / t2);
		b = ((3 * p1) / t2) - ((3 * p0) / t2) - ((2 * m0) / tEnd) - (m1 / tEnd);
		c = m0;
		d = p0;
		this.tEnd = tEnd;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Get a spline value.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return value at t
	 */
	public float value(float t)
	{
		if (t < 0)
		{
			t = 0.0f;
		}
		if (t > tEnd)
		{
			t = tEnd;
		}
		
		return (a * t * t * t) + (b * t * t) + (c * t) + d;
	}
	
	
	/**
	 * Get the first derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return first derivative (slope) at t
	 */
	public float firstDerivative(float t)
	{
		if (t < 0)
		{
			t = 0.0f;
		}
		if (t > tEnd)
		{
			t = tEnd;
		}
		
		return (a * 3 * t * t) + (b * 2 * t) + c;
	}
	
	
	/**
	 * Get the second derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return second derivative at t
	 */
	public float secondDerivative(float t)
	{
		if (t < 0)
		{
			t = 0.0f;
		}
		if (t > tEnd)
		{
			t = tEnd;
		}
		
		return (a * 6 * t) + (b * 2);
	}
	
	
	/**
	 * Calculate maximum value of first derivative.
	 * Always positive.
	 * 
	 * @return max
	 */
	public float getMaxFirstDerivative()
	{
		float p = Math.abs(firstDerivative(0));
		float q = 0.0f;
		float r = Math.abs(firstDerivative(tEnd));
		
		if (a != 0.0f) // maximum existent?
		{
			float t = -(b * 2) / (a * 6); // time at maximum
			if ((t > 0) && (t < tEnd))
			{
				q = Math.abs(firstDerivative(t));
			}
		}
		
		return SumatraMath.max(q, p, r);
	}
	
	
	/**
	 * Calculate maximum of second derivative.
	 * Always positive.
	 * 
	 * @return max
	 */
	public float getMaxSecondDerivative()
	{
		// maximum of second derivative is at begin or end
		float q = Math.abs(secondDerivative(0));
		float p = Math.abs(secondDerivative(tEnd));
		
		return SumatraMath.max(q, p);
	}
	
	
	/**
	 * Get the third derivative.
	 * As this is a cubic spline, this value is constant.
	 * 
	 * @return third derivative
	 */
	public float thirdDerivative()
	{
		return a * 6;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the a
	 */
	public float getA()
	{
		return a;
	}
	
	
	/**
	 * @param a the a to set
	 */
	public void setA(float a)
	{
		this.a = a;
	}
	
	
	/**
	 * @return the b
	 */
	public float getB()
	{
		return b;
	}
	
	
	/**
	 * @param b the b to set
	 */
	public void setB(float b)
	{
		this.b = b;
	}
	
	
	/**
	 * @return the c
	 */
	public float getC()
	{
		return c;
	}
	
	
	/**
	 * @param c the c to set
	 */
	public void setC(float c)
	{
		this.c = c;
	}
	
	
	/**
	 * @return the d
	 */
	public float getD()
	{
		return d;
	}
	
	
	/**
	 * @param d the d to set
	 */
	public void setD(float d)
	{
		this.d = d;
	}
	
	
	/**
	 * @return the tEnd
	 */
	public float getEndTime()
	{
		return tEnd;
	}
	
	
	/**
	 * @param tEnd the tEnd to set
	 */
	public void setEndTime(float tEnd)
	{
		this.tEnd = tEnd;
	}
}
