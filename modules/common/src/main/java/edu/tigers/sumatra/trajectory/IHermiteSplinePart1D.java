/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IHermiteSplinePart1D extends ITrajectory<Double>
{
	/**
	 * @return
	 */
	double[] getA();
	
	
	/**
	 * Get a spline value.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return value at t
	 */
	Double value(final double t);
	
	
	/**
	 * Get the first derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return first derivative (slope) at t
	 */
	Double firstDerivative(final double t);
	
	
	/**
	 * Get the second derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return second derivative at t
	 */
	Double secondDerivative(final double t);
	
	
	/**
	 * Get the third derivative.
	 * As this is a cubic spline, this value is constant.
	 * 
	 * @param t
	 * @return third derivative
	 */
	Double thirdDerivative(final double t);
	
	
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
	default Double getPositionMM(final double t)
	{
		return value(t) * 1000;
	}
	
	
	@Override
	default Double getPosition(final double t)
	{
		return value(t);
	}
	
	
	@Override
	default Double getVelocity(final double t)
	{
		return firstDerivative(t);
	}
	
	
	@Override
	default Double getAcceleration(final double t)
	{
		return secondDerivative(t);
	}
	
	
	@Override
	default double getTotalTime()
	{
		return getEndTime();
	}
}
