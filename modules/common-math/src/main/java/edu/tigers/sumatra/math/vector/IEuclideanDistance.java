/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

/**
 * @author nicolai.ommer
 */
@FunctionalInterface
public interface IEuclideanDistance
{
	/**
	 * @param point target point
	 * @return euclidean distance to the target point
	 */
	double distanceTo(IVector2 point);
}
