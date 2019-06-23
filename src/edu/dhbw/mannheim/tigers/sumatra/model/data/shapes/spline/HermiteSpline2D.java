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
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * A hermite spline in 2D.
 * This class only takes two points. For more complex splines with more points combine multiple HermiteSplines.
 * 
 * This implementation always references values on a t-axis from 0 to tEnd.
 * 
 * @author AndreR
 * 
 */
@Embeddable
public class HermiteSpline2D
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private HermiteSpline	x;
	private HermiteSpline	y;
	
	private float				length	= -1;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Create a 2D hermite cubic spline.
	 * 
	 * @param initialPos Initial position.
	 * @param finalPos Final position.
	 * @param initialVelocity Initial velocity.
	 * @param finalVelocity Final velocity.
	 */
	public HermiteSpline2D(IVector2 initialPos, IVector2 finalPos, IVector2 initialVelocity, IVector2 finalVelocity)
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
	public HermiteSpline2D(IVector2 initialPos, IVector2 finalPos, IVector2 initialVelocity, IVector2 finalVelocity,
			float tEnd)
	{
		x = new HermiteSpline(initialPos.x(), finalPos.x(), initialVelocity.x(), finalVelocity.x(), tEnd);
		y = new HermiteSpline(initialPos.y(), finalPos.y(), initialVelocity.y(), finalVelocity.y(), tEnd);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Get the 2D value.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return
	 */
	public Vector2 value(float t)
	{
		return new Vector2(x.value(t), y.value(t));
	}
	
	
	/**
	 * Get first derivative.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return
	 */
	public Vector2 firstDerivative(float t)
	{
		return new Vector2(x.firstDerivative(t), y.firstDerivative(t));
	}
	
	
	/**
	 * Get second derivative.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return
	 */
	public Vector2 secondDerivative(float t)
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
		float c = firstDerivative(x.tEnd).getLength2();
		
		if ((x.a + y.a) != 0.0f) // maximum existent?
		{
			float t = -(y.b + x.b) / ((3 * y.a) + (3 * x.a)); // time at maximum
			if ((t > 0) && (t < x.tEnd))
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
		float b = secondDerivative(x.tEnd).getLength2();
		
		return SumatraMath.max(a, b);
	}
	
	
	/**
	 * Get the curvature at a given time.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return Curvature, high values mean tight curves.
	 */
	public float getCurvature(float t)
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
	 * get the total length of the spline in mm
	 * 
	 * @return
	 */
	public float getLength()
	{
		if (length == -1)
		{
			length = getLength(100);
		}
		return length;
	}
	
	
	/**
	 * The total length along the spline.
	 * 
	 * @param points Interpolation points, 100 should be a good value. Use higher values for a more accurate result.
	 * @return Spline length.
	 */
	public float getLength(int points)
	{
		// unfortunately the most efficient way to calculate the length of a 2D spline is numeric integration
		float length = 0;
		float dT = 1.0f / points;
		
		for (float t = 0.0f; t < getEndTime(); t += dT)
		{
			length += firstDerivative(t).getLength2() * dT;
		}
		
		return length;
	}
	
	
	/**
	 * Get the 2D value but by driven way
	 * 
	 * @param drivenWay
	 * @return
	 */
	public IVector2 getValue(float drivenWay)
	{
		return value(lengthToTime(drivenWay));
	}
	
	
	/**
	 * for the defense points, LOW PRECISION, BAD PERFORMANCE
	 * 
	 * @param drivenWay
	 * @return
	 */
	public float lengthToTime(float drivenWay)
	{
		float length = 0;
		for (float t = 0.0f; t < getEndTime(); t += 0.01f)
		{
			length += firstDerivative(t).getLength2() * 0.01f;
			if (length >= drivenWay)
			{
				return t;
			}
		}
		return getEndTime();
	}
	
	
	/**
	 * Get the end time.
	 * 
	 * @return tEnd
	 */
	public float getEndTime()
	{
		return x.tEnd;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public HermiteSpline getXSpline()
	{
		return x;
	}
	
	
	/**
	 * @return
	 */
	public HermiteSpline getYSpline()
	{
		return y;
	}
}
