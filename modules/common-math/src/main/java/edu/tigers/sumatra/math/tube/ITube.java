/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.tube;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public interface ITube extends I2DShape
{
	
	/**
	 * @return radius of tube
	 */
	double radius();
	
	
	/**
	 * @return center of first outer circle
	 */
	IVector2 startCenter();
	
	
	/**
	 * @return center of second outer circle
	 */
	IVector2 endCenter();
}
