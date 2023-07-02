/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.intersections;

import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Optional;

/**
 * Interface to access the intersection points between two IPath instances
 * If only a single interception point is possible (line to line intersections)
 */
public interface ISingleIntersection extends IIntersections
{
	/**
	 * @return Return the intersection point as an Optional
	 */
	Optional<IVector2> asOptional();

	/**
	 * @return Return true if the intersection point exists
	 */
	boolean isPresent();
}
