/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 9, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * every spline which can be calculated for a path should implement this interface
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public interface ISpline
{
	
	
	/**
	 * the position on the field after a given time
	 * 
	 * @param t the time from the beginning
	 * @return
	 */
	IVector2 getValueByTime(float t);
	
	
	/**
	 * get the acceleration on the spline after a given time, this is the first derivation
	 * @param t
	 * @return
	 */
	IVector2 getAccelerationByTime(float t);
	
	
	/**
	 * the length of the spline in mm
	 * 
	 * @return
	 */
	float getLength();
	
	
	/**
	 * total time in s
	 * 
	 * @return
	 */
	float getTotalTime();
	
	
}
