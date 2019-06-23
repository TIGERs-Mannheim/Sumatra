/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IHermiteSplinePart2D extends ITrajectory<IVector2>
{
	/**
	 * Get a spline value.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return value at t
	 */
	IVector2 value(final double t);
	
	
	/**
	 * Get the first derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return first derivative (slope) at t
	 */
	IVector2 firstDerivative(final double t);
	
	
	/**
	 * Get the second derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return second derivative at t
	 */
	IVector2 secondDerivative(final double t);
	
	
	/**
	 * Get the third derivative.
	 * As this is a cubic spline, this value is constant.
	 * 
	 * @param t
	 * @return third derivative
	 */
	IVector2 thirdDerivative(final double t);
	
	
	/**
	 * Calculate maximum value of first derivative.
	 * Always positive.
	 * 
	 * @return max
	 */
	double getMaxFirstDerivative();
	
	
	/**
	 * Calculate maximum of second derivative.
	 * Always positive.
	 * 
	 * @return max
	 */
	double getMaxSecondDerivative();
	
	
	/**
	 * @return the tEnd
	 */
	double getEndTime();
	
	
	@Override
	default IVector2 getPositionMM(final double t)
	{
		return value(t).multiplyNew(1000);
	}
	
	
	@Override
	default IVector2 getPosition(final double t)
	{
		return value(t);
	}
	
	
	@Override
	default IVector2 getVelocity(final double t)
	{
		return firstDerivative(t);
	}
	
	
	@Override
	default IVector2 getAcceleration(final double t)
	{
		return secondDerivative(t);
	}
	
	
	@Override
	default double getTotalTime()
	{
		return getEndTime();
	}
}
