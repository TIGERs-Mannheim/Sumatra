/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline;

import java.util.Arrays;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;


/**
 * Hermite Cubic or quintic Spline. Other name is Catmull-Rom-Spline.
 * This class only takes two points. For more complex splines with more points combine multiple HermiteSplines.
 * This implementation always references values on a t-axis from 0 to tEnd.
 * 
 * @author AndreR
 */
@Persistent(version = 1)
public class HermiteSpline implements IHermiteSpline
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** cubic (4) or quintic (6) - change with care! check toString() */
	public static final int	SPLINE_SIZE	= 4;
	
	private float[]			a				= new float[SPLINE_SIZE];
	private float				tEnd;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@SuppressWarnings("unused")
	private HermiteSpline()
	{
	}
	
	
	/**
	 * Construct a hermite cubic spline.
	 * Uses tEnd = 1.0.
	 * 
	 * @param p0 Initial value.
	 * @param p1 Final value.
	 * @param m0 Initial slope.
	 * @param m1 Final slope.
	 */
	public HermiteSpline(final float p0, final float p1, final float m0, final float m1)
	{
		this(p0, p1, m0, m1, 1.0f);
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
	public HermiteSpline(final float p0, final float p1, final float m0, final float m1, final float tEnd)
	{
		final float t2 = tEnd * tEnd;
		final float t3 = t2 * tEnd;
		a[0] = p0;
		a[1] = m0;
		a[2] = ((-3 * p0) / t2) +
				((3 * p1) / t2) +
				((-2 * m0) / tEnd) +
				((-1 * m1) / tEnd);
		a[3] = (((2 * p0) / t3) +
				((-2 * p1) / t3)) +
				(m0 / t2) +
				(m1 / t2);
		this.tEnd = tEnd;
		assert Float.isFinite(this.tEnd);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param spline
	 */
	public HermiteSpline(final IHermiteSpline spline)
	{
		a = Arrays.copyOf(spline.getA(), spline.getA().length);
		tEnd = spline.getEndTime();
		assert Float.isFinite(tEnd);
	}
	
	
	/**
	 */
	@Override
	public void mirrorPosition()
	{
		for (int i = 0; i < a.length; i++)
		{
			a[i] *= -1;
		}
	}
	
	
	/**
	 */
	@Override
	public void mirrorRotation()
	{
		a[0] = AngleMath.normalizeAngle(a[0] + AngleMath.PI);
	}
	
	
	/**
	 * Get a spline value.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return value at t
	 */
	@Override
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
		float result = (a[3] * t * t * t) + (a[2] * t * t) + (a[1] * t)
				+ a[0];
		if (a.length == 6)
		{
			result = (result + (a[5] * t * t * t * t * t) + (a[4] * t * t * t * t));
		}
		return result;
	}
	
	
	/**
	 * Get the first derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return first derivative (slope) at t
	 */
	@Override
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
		float result = (a[3] * 3 * t * t) + (a[2] * 2 * t) + a[1];
		if (a.length == 6)
		{
			result = (result + (a[5] * 5 * t * t * t * t)) + (a[4] * 4 * t * t * t);
		}
		return result;
	}
	
	
	/**
	 * Get the second derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return second derivative at t
	 */
	@Override
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
		float result = (a[3] * 6 * t) + (a[2] * 2);
		if (a.length == 6)
		{
			result = (result + (a[5] * 20 * t * t * t) + (a[4] * 12 * t * t));
		}
		return result;
	}
	
	
	/**
	 * Calculate maximum value of first derivative.
	 * Always positive.
	 * 
	 * @return max
	 */
	@Override
	public float getMaxFirstDerivative()
	{
		float p = Math.abs(firstDerivative(0));
		float q = 0.0f;
		float r = Math.abs(firstDerivative(tEnd));
		
		if (a[3] != 0.0f) // maximum existent?
		{
			float t = -(a[2] * 2) / (a[3] * 6); // time at maximum
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
	@Override
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
	@Override
	public float thirdDerivative()
	{
		return a[3] * 6;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the tEnd
	 */
	@Override
	public float getEndTime()
	{
		return tEnd;
	}
	
	
	/**
	 * @param tEnd the tEnd to set
	 */
	@Override
	public void setEndTime(final float tEnd)
	{
		this.tEnd = tEnd;
		assert Float.isFinite(this.tEnd);
	}
	
	
	/**
	 * @return the a
	 */
	@Override
	public final float[] getA()
	{
		return Arrays.copyOf(a, a.length);
	}
	
	
	@Override
	public String toString()
	{
		if (a.length == 6)
		{
			return String.format("[%.3fx^5+%.3fx^4+%.3fx^3+%.3fx^2+%.3fx+%.3f, tEnd=%.3f]", a[0], a[1], a[2], a[3], a[4],
					a[5], tEnd);
		}
		return String.format("[%.3fx^3+%.3fx^2+%.3fx+%.3f, tEnd=%.3f]", a[0], a[1], a[2], a[3], tEnd);
	}
}
