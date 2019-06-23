/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline;


/**
 * Interface for hermite splines
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public interface IHermiteSpline
{
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Get a spline value.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return value at t
	 */
	float value(float t);
	
	
	/**
	 * Mirror this spline (only for position splines)
	 */
	void mirrorPosition();
	
	
	/**
	 * Mirror this spline (only for rotation splines)
	 */
	void mirrorRotation();
	
	
	/**
	 * Get the first derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return first derivative (slope) at t
	 */
	float firstDerivative(float t);
	
	
	/**
	 * Get the second derivative at t.
	 * 
	 * @param t "Time" in range 0-tEnd
	 * @return second derivative at t
	 */
	float secondDerivative(float t);
	
	
	/**
	 * Calculate maximum value of first derivative.
	 * Always positive.
	 * 
	 * @return max
	 */
	float getMaxFirstDerivative();
	
	
	/**
	 * Calculate maximum of second derivative.
	 * Always positive.
	 * 
	 * @return max
	 */
	float getMaxSecondDerivative();
	
	
	/**
	 * Get the third derivative.
	 * As this is a cubic spline, this value is constant.
	 * 
	 * @return third derivative
	 */
	float thirdDerivative();
	
	
	/**
	 * @return the tEnd
	 */
	float getEndTime();
	
	
	/**
	 * @param tEnd the tEnd to set
	 */
	void setEndTime(float tEnd);
	
	
	/**
	 * @return
	 */
	float[] getA();
}