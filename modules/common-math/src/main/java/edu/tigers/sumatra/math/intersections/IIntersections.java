/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.intersections;

import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;
import java.util.stream.Stream;


/**
 * Interface to access the intersection points between two IPath instances
 */
public interface IIntersections
{
	/**
	 * @return Return the intersection point(s) as an unmodifiable list
	 */
	List<IVector2> asList();

	/**
	 * @return Return a stream of the intersection point(s)
	 */
	Stream<IVector2> stream();

	/**
	 * @return true if no intersection point exists
	 */
	boolean isEmpty();

	/**
	 * @return the number of intersections detected
	 */
	int size();
}
