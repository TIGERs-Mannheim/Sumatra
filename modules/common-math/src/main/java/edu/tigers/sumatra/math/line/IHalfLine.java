/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * This interface represents a half-line. It is defined by an initial starting point called the support vector and
 * extends indefinitely in the direction of the direction vector.
 *
 * @author Lukas Magel
 */
public interface IHalfLine extends ILineBase
{
	@Override
	IHalfLine copy();
	
	
	/**
	 * Returns true if the specified {@code point} is located in the direction of the line. For a line with
	 * support vector A and direction vector B this is the case if the angle between B and (point - A) is less than 90
	 * degrees.
	 *
	 * @param point
	 *           The point for which to perform the check
	 * @return {@code true} if the line is valid according to {@link #isValid()} and the point is located in the
	 *         direction of this half-line.
	 */
	boolean isPointInFront(IVector2 point);

	/**
	 * Returns a new line segment using the same support vector of the half line with a specified length
	 *
	 * @param length of the new line segment
	 * @return
	 */
	ILineSegment toLineSegment(double length);
}
