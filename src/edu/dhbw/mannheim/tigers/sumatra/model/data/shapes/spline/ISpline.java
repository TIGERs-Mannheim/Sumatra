/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 9, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;


/**
 * every spline which can be calculated for a path should implement this interface
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public interface ISpline
{
	
	
	/**
	 * the position on the field after a given time
	 * 
	 * @param t [s] the time from the beginning
	 * @return [mm,mm,rad]
	 */
	IVector3 getPositionByTime(float t);
	
	
	/**
	 * The velocity, basically the derivative of the position
	 * 
	 * @param t [s]
	 * @return [m/s,m/s,rad/s]
	 */
	IVector3 getVelocityByTime(float t);
	
	
	/**
	 * @param t
	 * @return
	 */
	IVector3 getAccelerationByTime(float t);
	
	
	/**
	 * total time in s
	 * 
	 * @return [s]
	 */
	float getTotalTime();
	
	
	/**
	 * @return
	 */
	float getCurrentTime();
}
