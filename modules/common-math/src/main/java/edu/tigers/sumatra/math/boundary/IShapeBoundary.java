/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.boundary;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Comparator;
import java.util.Optional;


/**
 * The boundary of an arbitrary shape that is used to sort and creat positions along the shape
 */
public interface IShapeBoundary extends Comparator<IVector2>
{

	/**
	 * @return the shape which is encapsulated with this boundary
	 */
	I2DShape getShape();

	/**
	 * @return the start position where this shape boundary starts
	 */
	IVector2 getStart();

	/**
	 * @return the end position where this shape boundary end (For closed shapes this will be the same as start)
	 */
	IVector2 getEnd();

	/**
	 * Check if the given point is inside the boundary
	 *
	 * @param point
	 * @return
	 */
	boolean isPointInShape(IVector2 point);


	/**
	 * Return a new ShapeBoundary around a new shape with the applied margin
	 *
	 * @param margin
	 * @return
	 */
	IShapeBoundary withMargin(final double margin);


	/**
	 * Get the closest point on the shape boundary
	 *
	 * @param p
	 * @return
	 */
	IVector2 closestPoint(IVector2 p);


	/**
	 * Get the intersection between the boundary and a half line from start through end
	 *
	 * @param pStart
	 * @param pEnd
	 * @return
	 */

	IVector2 projectPoint(final IVector2 pStart, final IVector2 pEnd);


	/**
	 * Get the first corner on the shape between pFrom and pTo
	 *
	 * @param pFrom point on boundary
	 * @param pTo   point on boundary
	 * @return empty if no corner is between from and to, and the first corner if at least on is in between
	 */
	Optional<IVector2> nextIntermediateCorner(final IVector2 pFrom, final IVector2 pTo);


	/**
	 * Distance on the shape boundary between p and the start
	 *
	 * @param p Some point on the shape boundary
	 * @return The absolute distance
	 */
	double distanceFromStart(final IVector2 p);


	/**
	 * Distance on the shape boundary between p and the end
	 *
	 * @param p Some point on the perimeter
	 * @return The absolute distance
	 */
	double distanceFromEnd(final IVector2 p);


	/**
	 * Get the distance on the shape boundary between p1 and p2
	 *
	 * @param p1 point on ShapeBoundary
	 * @param p2 point on ShapeBoundary
	 * @return the absolute distance (The sign does not represent any kind of direction information)
	 */
	double distanceBetween(final IVector2 p1, final IVector2 p2);


	/**
	 * Step along the boundary from the boundary path
	 *
	 * @param distance Must be greater than 0
	 * @return The point on the boundary or empty if distance is smaller than zero, or gets out of bounds
	 */
	Optional<IVector2> stepAlongBoundary(double distance);


	/**
	 * Step along the boundary starting at a random point on the shape
	 *
	 * @param start
	 * @param distance Must be greater than 0
	 * @return The point on the boundary or empty if distance is smaller than zero, or gets out of bounds
	 */
	Optional<IVector2> stepAlongBoundary(final IVector2 start, final double distance);

}
