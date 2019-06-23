/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.Vector2;


/**
 * A hermite spline in 2D.
 * This class only takes two points. For more complex splines with more points combine multiple HermiteSplines.
 * This implementation always references values on a t-axis from 0 to tEnd.
 * 
 * @author AndreR
 */
@Persistent
public class HermiteSplinePart2D implements IHermiteSplinePart2D
{
	private IHermiteSplinePart1D	x;
	private IHermiteSplinePart1D	y;
											
											
	@SuppressWarnings("unused")
	private HermiteSplinePart2D()
	{
	}
	
	
	/**
	 * Create a 2D hermite cubic spline.
	 * 
	 * @param initialPos Initial position.
	 * @param finalPos Final position.
	 * @param initialVelocity Initial velocity.
	 * @param finalVelocity Final velocity.
	 * @param tEnd End time.
	 */
	public HermiteSplinePart2D(final IVector2 initialPos, final IVector2 finalPos, final IVector2 initialVelocity,
			final IVector2 finalVelocity,
			final double tEnd)
	{
		x = new CubicHermiteSplinePart(initialPos.x(), finalPos.x(), initialVelocity.x(), finalVelocity.x(), tEnd);
		y = new CubicHermiteSplinePart(initialPos.y(), finalPos.y(), initialVelocity.y(), finalVelocity.y(), tEnd);
	}
	
	
	/**
	 * @param spline
	 */
	public HermiteSplinePart2D(final HermiteSplinePart2D spline)
	{
		x = new CubicHermiteSplinePart(spline.x);
		y = new CubicHermiteSplinePart(spline.y);
	}
	
	
	/**
	 * Get the 2D value.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return
	 */
	@Override
	public IVector2 value(final double t)
	{
		return new Vector2(x.value(t), y.value(t));
	}
	
	
	/**
	 * Get first derivative.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return
	 */
	@Override
	public IVector2 firstDerivative(final double t)
	{
		return new Vector2(x.firstDerivative(t), y.firstDerivative(t));
	}
	
	
	/**
	 * Get second derivative.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return
	 */
	@Override
	public IVector2 secondDerivative(final double t)
	{
		return new Vector2(x.secondDerivative(t), y.secondDerivative(t));
	}
	
	
	@Override
	public IVector2 thirdDerivative(final double t)
	{
		return new Vector2(x.thirdDerivative(t), y.thirdDerivative(t));
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
		double a = firstDerivative(0).getLength2();
		double b = 0.0;
		double c = firstDerivative(x.getEndTime()).getLength2();
		
		if ((x.getA()[3] + y.getA()[3]) != 0.0) // maximum existent?
		{
			double[] ax = x.getA();
			double[] ay = y.getA();
			// http://www.wolframalpha.com/input/?i=solve%28+derivative%28sqrt%28%286*a*x%2B2*b%29%5E2+%2B+%286*c*x%2B2*d%29%5E2%29%2C+x%29+%3D+0%2C+x%29
			double t = ((-ax[3] * ax[2]) - (ay[3] * ay[2])) / (3.0 * ((ax[3] * ax[3]) + (ay[3] * ay[3])));
			if ((t > 0) && (t < x.getEndTime()))
			{
				b = firstDerivative(t).getLength2();
			}
		}
		
		return SumatraMath.max(a, b, c);
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
		double a = secondDerivative(0).getLength2();
		double b = secondDerivative(x.getEndTime()).getLength2();
		
		return SumatraMath.max(a, b);
	}
	
	
	/**
	 * Get the curvature at a given time.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return Curvature, high values mean tight curves.
	 */
	public double getCurvature(final double t)
	{
		double x1 = x.firstDerivative(t);
		double x2 = x.secondDerivative(t);
		double y1 = y.firstDerivative(t);
		double y2 = y.secondDerivative(t);
		
		if ((x1 == 0) && (x2 == 0))
		{
			return Double.MAX_VALUE;
		}
		return ((x1 * y2) - (y1 * x2)) / Math.pow((x1 * x1) + (y1 * y1), 1.5);
	}
	
	
	/**
	 * Get the end time.
	 * 
	 * @return tEnd
	 */
	@Override
	public double getEndTime()
	{
		return x.getEndTime();
	}
	
	
	/**
	 * @return
	 */
	public IHermiteSplinePart1D getXSpline()
	{
		return x;
	}
	
	
	/**
	 * @return
	 */
	public IHermiteSplinePart1D getYSpline()
	{
		return y;
	}
	
	
	@Override
	public String toString()
	{
		return "[x=" + x + ", y=" + y + "]";
	}
}
