/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.vector.IEuclideanDistance;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


/**
 * Circle interface.
 */
public interface ICircle extends ICircular, IEuclideanDistance, IBoundedPath
{
	/**
	 * Get the intersection points of the two tangential lines that cross the external points.
	 *
	 * @param externalPoint some point
	 * @return two tangential intersections
	 */
	List<IVector2> tangentialIntersections(final IVector2 externalPoint);

	@Override
	ICircle withMargin(final double margin);
}
