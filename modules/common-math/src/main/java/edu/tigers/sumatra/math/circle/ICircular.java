/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Interface to a circular object
 * 
 * @author nicolai.ommer
 */
public interface ICircular extends I2DShape
{
	
	/**
	 * @return the radius of this circle
	 */
	double radius();
	
	
	/**
	 * @return the center of this circle
	 */
	IVector2 center();
	
	
	/**
	 * @return a mirrored copy of this shape
	 */
	ICircular mirror();
}
