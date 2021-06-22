/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.math.line.v2;

import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


/**
 * This interface represents a bounded line segment. It extends from a starting point A to an ending point B. A line
 * segment has a well-defined length.
 *
 * @author Lukas Magel
 */
public interface ILineSegment extends ILineBase
{
	@Override
	ILineSegment copy();


	/**
	 * Returns the starting point A of the line.
	 *
	 * @return The vector from which the segment extends
	 */
	IVector2 getStart();


	/**
	 * Returns the ending point B of the line.
	 *
	 * @return The vector to which the line extends
	 */
	IVector2 getEnd();


	/**
	 * Returns the point located in the middle between start and end
	 *
	 * @return the center position of this line
	 */
	IVector2 getCenter();


	/**
	 * Returns the absolute length of this line segment between A and B. The length of the line segment can be zero if
	 * {@code start} and {@code end} are identical.
	 *
	 * @return The absolute length of the line segment
	 */
	double getLength();


	/**
	 * Converts this instance into a {@link IHalfLine}. The resulting half-line extends from the start (i.e.
	 * {@link #getStart()} in the direction of the {@link #directionVector()}. Please note that the resulting
	 * {@link ILine} instance is only valid if this instance is also valid.
	 *
	 * @return A half-line instance which is centered at {@link #getStart()} and points in the same direction as this
	 * line segment
	 */
	IHalfLine toHalfLine();


	/**
	 * Step the requested absolute distance along this line segment.
	 * If this segment has a length {@code l} and the parameter was set to an absolute value which equals {@code l / 2}
	 * then this method will return the point which is located exactly in between the two support points of this line
	 * segment.
	 *
	 * @param stepSize The absolute length of the step to make along this line
	 * @return The resulting vector if this segment is valid or one of the two support points if it is not valid.
	 */
	IVector2 stepAlongLine(double stepSize);


	@Override
	default IVector2 supportVector()
	{
		return getStart();
	}


	/**
	 * Create a new line segment with a positive or negative margin.
	 * If the margin is negative and the line segment length is less than |margin*2|, the behavior is undefined.
	 *
	 * @param margin the margin to apply to both ends
	 * @return a new line segment
	 */
	ILineSegment withMargin(double margin);


	/**
	 * Returns a list of points on a line. All points are separated by
	 * stepSize. Start and end of the line are always in the list.
	 *
	 * @param stepSize abolute with of each steps
	 * @return a list of positions on the line, all separated by stepSize
	 */
	List<IVector2> getSteps(double stepSize);


	/**
	 * Smallest distance between this line segment and the given.
	 *
	 * @param line
	 * @return
	 */
	double distanceTo(final ILineSegment line);
}
