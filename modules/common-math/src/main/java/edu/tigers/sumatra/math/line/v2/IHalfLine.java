/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 17, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.sumatra.math.line.v2;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * This interface represents a half-line. It is defined by an initial starting point called the support vector and
 * extends indefinitely in the direction of the direction vector.
 *
 * @author Lukas Magel
 */
public interface IHalfLine extends IUnboundedLine
{
	@Override
	IHalfLine copy();

	/**
	 * Returns true if the specified {@code point} is located in the direction of the half-line. For a half-line with
	 * support vector A and direction vector B this is the case if the angle between B and (point - A) is less than 90
	 * degrees.
	 *
	 * @param point
	 *           The point for which to perform the check
	 * @return {@code true} if the line is valid according to {@link #isValid()} and the point is located in the
	 *         direction of this half-line.
	 */
	boolean isPointInFront(IVector2 point);
	
}
