/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import edu.tigers.sumatra.math.ellipse.IEllipse;
import edu.tigers.sumatra.math.vector.IEuclideanDistance;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;

import java.util.List;


/**
 * Circle interface.
 */
public interface ICircle extends ICircular, IEuclideanDistance
{
	/**
	 * Get the intersection points of the two tangential lines that cross the external points.
	 *
	 * @param externalPoint some point
	 * @return two tangential intersections
	 */
	List<IVector2> tangentialIntersections(final IVector2 externalPoint);


	/**
	 * Project <i>this</i> to ground assuming it is at <i>height</i> height and the projection origin is <i>origin</i>.
	 *
	 * @param origin Projection origin
	 * @param height Height of the circle before projection.
	 * @return Projected circle => ellipse
	 */
	IEllipse projectToGround(final IVector3 origin, final double height);

	@Override
	ICircle withMargin(final double margin);
}
