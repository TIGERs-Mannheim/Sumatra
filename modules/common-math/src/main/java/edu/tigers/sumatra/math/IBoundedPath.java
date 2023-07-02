/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import edu.tigers.sumatra.math.vector.IVector2;


public interface IBoundedPath extends IPath
{

	/**
	 * Returns the starting point A of the line.
	 *
	 * @return The vector from which the segment extends
	 */
	IVector2 getPathStart();


	/**
	 * Returns the ending point B of the line.
	 *
	 * @return The vector to which the line extends
	 */
	IVector2 getPathEnd();


	/**
	 * Returns the point located in the middle between start and end
	 *
	 * @return the center position of this line
	 */
	IVector2 getPathCenter();


	/**
	 * Returns the absolute length of this line segment between A and B. The length of the line segment can be zero if
	 * {@code start} and {@code end} are identical.
	 *
	 * @return The absolute length of the line segment
	 */
	double getLength();


	/**
	 * Step the requested absolute distance along this line segment.
	 * If this segment has a length {@code l} and the parameter was set to an absolute value which equals {@code l / 2}
	 * then this method will return the point which is located exactly in between the two support points of this line
	 * segment.
	 *
	 * @param stepSize The absolute length of the step to make along this line
	 * @return The resulting vector if this segment is valid or one of the two support points if it is not valid.
	 */
	IVector2 stepAlongPath(double stepSize);


	/**
	 * Get the distance between the start of the path till {@code pointOnPath}
	 * Note no checks will be made to ensure {@code pointOnPath} is actually on the path
	 *
	 * @param pointOnPath
	 * @return
	 */
	double distanceFromStart(IVector2 pointOnPath);
}
