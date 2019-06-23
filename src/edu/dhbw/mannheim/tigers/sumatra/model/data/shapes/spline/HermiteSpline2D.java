/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.ITrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * A hermite spline in 2D.
 * This class only takes two points. For more complex splines with more points combine multiple HermiteSplines.
 * This implementation always references values on a t-axis from 0 to tEnd.
 * 
 * @author AndreR
 */
@Persistent(version = 2)
public class HermiteSpline2D implements ITrajectory2D
{
	private IHermiteSpline	x;
	private IHermiteSpline	y;
	
	
	@SuppressWarnings("unused")
	private HermiteSpline2D()
	{
	}
	
	
	/**
	 * Create a 2D hermite cubic spline.
	 * 
	 * @param initialPos Initial position.
	 * @param finalPos Final position.
	 * @param initialVelocity Initial velocity.
	 * @param finalVelocity Final velocity.
	 */
	public HermiteSpline2D(final IVector2 initialPos, final IVector2 finalPos, final IVector2 initialVelocity,
			final IVector2 finalVelocity)
	{
		x = new HermiteSpline(initialPos.x(), finalPos.x(), initialVelocity.x(), finalVelocity.x());
		y = new HermiteSpline(initialPos.y(), finalPos.y(), initialVelocity.y(), finalVelocity.y());
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
	public HermiteSpline2D(final IVector2 initialPos, final IVector2 finalPos, final IVector2 initialVelocity,
			final IVector2 finalVelocity,
			final float tEnd)
	{
		x = new HermiteSpline(initialPos.x(), finalPos.x(), initialVelocity.x(), finalVelocity.x(), tEnd);
		y = new HermiteSpline(initialPos.y(), finalPos.y(), initialVelocity.y(), finalVelocity.y(), tEnd);
	}
	
	
	/**
	 * @param spline
	 */
	public HermiteSpline2D(final HermiteSpline2D spline)
	{
		x = new HermiteSpline(spline.x);
		y = new HermiteSpline(spline.y);
	}
	
	
	/**
	 * Get the 2D value.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return
	 */
	public Vector2 value(final float t)
	{
		return new Vector2(x.value(t), y.value(t));
	}
	
	
	/**
	 */
	public void mirror()
	{
		x.mirrorPosition();
		y.mirrorPosition();
	}
	
	
	/**
	 * Get first derivative.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return
	 */
	public Vector2 firstDerivative(final float t)
	{
		return new Vector2(x.firstDerivative(t), y.firstDerivative(t));
	}
	
	
	/**
	 * Get second derivative.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return
	 */
	public Vector2 secondDerivative(final float t)
	{
		return new Vector2(x.secondDerivative(t), y.secondDerivative(t));
	}
	
	
	/**
	 * Calculate maximum value of first derivative.
	 * Always positive.
	 * 
	 * @return max
	 */
	public float getMaxFirstDerivative()
	{
		float a = firstDerivative(0).getLength2();
		float b = 0.0f;
		float c = firstDerivative(x.getEndTime()).getLength2();
		
		if ((x.getA()[3] + y.getA()[3]) != 0.0f) // maximum existent?
		{
			float[] ax = x.getA();
			float[] ay = y.getA();
			// http://www.wolframalpha.com/input/?i=solve%28+derivative%28sqrt%28%286*a*x%2B2*b%29%5E2+%2B+%286*c*x%2B2*d%29%5E2%29%2C+x%29+%3D+0%2C+x%29
			float t = ((-ax[3] * ax[2]) - (ay[3] * ay[2])) / (3 * ((ax[3] * ax[3]) + (ay[3] * ay[3])));
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
	public float getMaxSecondDerivative()
	{
		// maximum of second derivative is at begin or end
		float a = secondDerivative(0).getLength2();
		float b = secondDerivative(x.getEndTime()).getLength2();
		
		return SumatraMath.max(a, b);
	}
	
	
	/**
	 * Get the curvature at a given time.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return Curvature, high values mean tight curves.
	 */
	public float getCurvature(final float t)
	{
		float x1 = x.firstDerivative(t);
		float x2 = x.secondDerivative(t);
		float y1 = y.firstDerivative(t);
		float y2 = y.secondDerivative(t);
		
		if ((x1 == 0) && (x2 == 0))
		{
			return Float.MAX_VALUE;
		}
		return (float) (((x1 * y2) - (y1 * x2)) / Math.pow((x1 * x1) + (y1 * y1), 1.5));
	}
	
	
	/**
	 * Get the end time.
	 * 
	 * @return tEnd
	 */
	public float getEndTime()
	{
		return x.getEndTime();
	}
	
	
	/**
	 * @return
	 */
	public IHermiteSpline getXSpline()
	{
		return x;
	}
	
	
	/**
	 * @return
	 */
	public IHermiteSpline getYSpline()
	{
		return y;
	}
	
	
	@Override
	public String toString()
	{
		return "[x=" + x + ", y=" + y + "]";
	}
	
	
	@Override
	public Vector2 getPosition(final float t)
	{
		return value(t).multiply(1000);
	}
	
	
	@Override
	public Vector2 getVelocity(final float t)
	{
		return firstDerivative(t);
	}
	
	
	@Override
	public Vector2 getAcceleration(final float t)
	{
		return secondDerivative(t);
	}
	
	
	@Override
	public float getTotalTime()
	{
		return getEndTime();
	}
}
