/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.tube;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.vector.IEuclideanDistance;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A tube is constructed out of two half circles and a rectangle, forming a tube.
 */
public interface ITube extends I2DShape, IEuclideanDistance
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


	/**
	 * @return the center of the tube
	 */
	IVector2 center();


	@Override
	ITube withMargin(double margin);
}
