/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.intersections.IIntersections;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IEuclideanDistance;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;
import java.util.Optional;


/**
 * Base interface which groups common path semantics and operators that apply to all the different path subtypes.
 * The different path types that inherit from this interface are designed to be fault-tolerant. This means that an input
 * validation does not occur upon instantiation and new instances can be defined with invalid input parameters, such as
 * an unbounded line with a zero-length direction vector. Each implementation provides a special method
 * {@link #isValid()} to check whether the supplied instance is valid. The instance methods are tolerant and do
 * not require the path to be valid. The method comments specify how the individual methods handle invalid
 * path instances.
 * <p>
 * Make sure to add new path implementations to {@link I2DShape#intersectPerimeterPath(IPath)}
 */
public interface IPath extends IEuclideanDistance
{
	double LINE_MARGIN = 1e-6;

	/**
	 * Returns true if this path instance is properly defined. Whether or not a path instance is valid depends on the
	 * input parameters that were used to define it. The result of other methods depends on the validity of a path.
	 * A half-line requires a non-zero direction vector to be valid, whereas a line segment must be defined through two
	 * non-equal support points to be properly defined.
	 *
	 * @return {@code true} if the path instance is properly defined, {@code false} otherwise
	 */
	boolean isValid();


	/**
	 * Intersect this path instance with the specified unbounded {@code line} and return the intersection vector.
	 *
	 * @param line The line for which to calculate an intersection with this instance
	 * @return The intersection point of both paths or an empty {@link List} if at least one of the two lines is not
	 * valid, the lines are parallel or this line instance is not unbounded and the intersection would be
	 * located outside the range.
	 */
	IIntersections intersect(ILine line);


	/**
	 * Intersect this path instance with the specified {@code halfLine} and return the intersection point.
	 *
	 * @param halfLine The half-line for which to calculate an intersection with this instance
	 * @return The intersection point of both lines r an empty {@link Optional} if at least one of the two lines is not
	 * valid, the lines are parallel or the intersection point was located outside their bounds.
	 */
	IIntersections intersect(IHalfLine halfLine);


	/**
	 * Intersect this path instance with the specified line {@code segment} and return the intersection point.
	 *
	 * @param segment The line segment for which to calculate an intersection with this instance
	 * @return The intersection point of both lines r an empty {@link Optional} if at least one of the two lines is not
	 * valid, the lines are parallel or the intersection point was located outside their bounds.
	 */
	IIntersections intersect(ILineSegment segment);

	/**
	 * Intersect this path instance with the specified line {@code segment} and return the intersection point.
	 *
	 * @param circle The circle for which to calculate an intersection with this instance
	 * @return The intersection point of both lines r an empty {@link Optional} if at least one of the two lines is not
	 * valid, the lines are parallel or the intersection point was located outside their bounds.
	 */
	IIntersections intersect(ICircle circle);

	/**
	 * Intersect this path instance with the specified line {@code segment} and return the intersection point.
	 *
	 * @param arc The arc for which to calculate an intersection with this instance
	 * @return The intersection point of both lines r an empty {@link Optional} if at least one of the two lines is not
	 * valid, the lines are parallel or the intersection point was located outside their bounds.
	 */
	IIntersections intersect(IArc arc);


	/**
	 * Returns a point on the path which is located closest to the specified point. For an unbounded line this is always
	 * the lead point. For a half-line or a line segment this is either the lead point or one of the bounding points if
	 * the lead point is not located on the segment.
	 * If the line instance is not valid, this method returns the line origin (for a line or half-line) or one of the
	 * segment points (for a line segment).
	 *
	 * @param point The point for which to find the closest point on the path
	 * @return A point on the line located closest to the specified point
	 */
	IVector2 closestPointOnPath(IVector2 point);

	/**
	 * Calculate the distance between the specified {@code point} and the path instance. This value is not defined for an
	 * invalid path instance and the method will consequently throw a {@code RuntimeException}.
	 *
	 * @param point target point
	 * @return The distance between the specified {@code point} and this line instance
	 * @throws IllegalArgumentException If this line is not valid according to the {@link #isValid()} method
	 */
	@Override
	default double distanceTo(IVector2 point)
	{
		return SumatraMath.sqrt(distanceToSqr(point));
	}

	/**
	 * Same as {@link #distanceTo(IVector2)}, but returns the squared distance to avoid sqrt calculation.
	 *
	 * @param point
	 * @return
	 */
	default double distanceToSqr(IVector2 point)
	{
		return closestPointOnPath(point).distanceToSqr(point);
	}

	/**
	 * Returns {@code true} if the specified point is located on this path. The check uses a small margin value and
	 * verifies if the specified point is located inside this margin around the line. Please note that the result can
	 * vary depending on the sub type, i.e. the function might return true for a line instance but false for a half-line
	 * instance with the same support and direction vector.
	 * If the path instance is not properly defined (i.e. a line segment with to identical points or a half-line with
	 * zero direction vector) then this method only returns true if the point is located on the line origin (for a line
	 * or half-line) or on one of the segment points (for a line segment).
	 *
	 * @param point The point for which this check is to be performed
	 * @return {@code true} if the line is valid and the point is located on it
	 */
	default boolean isPointOnPath(IVector2 point)
	{
		return distanceToSqr(point) <= LINE_MARGIN * LINE_MARGIN;
	}

}
