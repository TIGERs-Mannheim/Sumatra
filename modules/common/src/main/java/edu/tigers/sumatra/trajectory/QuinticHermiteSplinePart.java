/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import java.util.Arrays;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.SumatraMath;


/**
 * Hermite Cubic or quintic Spline. Other name is Catmull-Rom-Spline.
 * This class only takes two points. For more complex splines with more points combine multiple HermiteSplines.
 * This implementation always references values on a t-axis from 0 to tEnd.
 * 
 * @author AndreR
 */
@Persistent
public class QuinticHermiteSplinePart implements IHermiteSplinePart1D
{
	private final double[]	a;
	private final double		tEnd;
									
									
	@SuppressWarnings("unused")
	private QuinticHermiteSplinePart()
	{
		a = new double[6];
		tEnd = 0;
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
	public QuinticHermiteSplinePart(final double p0, final double p1, final double m0, final double m1,
			final double tEnd)
	{
		final double t2 = tEnd * tEnd;
		final double t3 = t2 * tEnd;
		a = new double[6];
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
		assert Double.isFinite(this.tEnd);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param spline
	 */
	public QuinticHermiteSplinePart(final IHermiteSplinePart1D spline)
	{
		a = Arrays.copyOf(spline.getA(), 6);
		tEnd = spline.getEndTime();
		assert Double.isFinite(tEnd);
	}
	
	
	/**
	 * @return the a
	 */
	@Override
	public final double[] getA()
	{
		return Arrays.copyOf(a, a.length);
	}
	
	
	/**
	 * Get a spline value.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return value at t
	 */
	@Override
	public Double value(double t)
	{
		if (t < 0)
		{
			t = 0.0;
		}
		if (t > tEnd)
		{
			t = tEnd;
		}
		double result = (a[5] * t * t * t * t * t) +
				(a[4] * t * t * t * t) +
				(a[3] * t * t * t) +
				(a[2] * t * t) +
				(a[1] * t) +
				a[0];
		return result;
	}
	
	
	/**
	 * Get the first derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return first derivative (slope) at t
	 */
	@Override
	public Double firstDerivative(double t)
	{
		if (t < 0)
		{
			t = 0.0;
		}
		if (t > tEnd)
		{
			t = tEnd;
		}
		double result = (a[5] * 5 * t * t * t * t) +
				(a[4] * 4 * t * t * t) +
				(a[3] * 3 * t * t) +
				(a[2] * 2 * t) +
				a[1];
		return result;
	}
	
	
	/**
	 * Get the second derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return second derivative at t
	 */
	@Override
	public Double secondDerivative(double t)
	{
		if (t < 0)
		{
			t = 0.0;
		}
		if (t > tEnd)
		{
			t = tEnd;
		}
		double result = (a[5] * 20 * t * t * t) +
				(a[4] * 12 * t * t) +
				(a[3] * 6 * t) +
				(a[2] * 2);
		return result;
	}
	
	
	/**
	 * Get the third derivative.
	 * 
	 * @return third derivative
	 */
	@Override
	public Double thirdDerivative(final double t)
	{
		return (a[5] * 60 * t * t) +
				(a[4] * 24 * t) +
				(a[3] * 6);
	}
	
	
	/**
	 * Calculate maximum value of first derivative.
	 * Always positive.
	 * 
	 * @return max
	 */
	@Override
	public double getMaxFirstDerivative()
	{
		double p = Math.abs(firstDerivative(0));
		double q = 0.0;
		double r = Math.abs(firstDerivative(tEnd));
		
		if (a[3] != 0.0) // maximum existent?
		{
			double t = -(a[2] * 2) / (a[3] * 6); // time at maximum
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
	public double getMaxSecondDerivative()
	{
		// maximum of second derivative is at begin or end
		double q = Math.abs(secondDerivative(0));
		double p = Math.abs(secondDerivative(tEnd));
		
		return SumatraMath.max(q, p);
	}
	
	
	/**
	 * @return the tEnd
	 */
	@Override
	public double getEndTime()
	{
		return tEnd;
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
