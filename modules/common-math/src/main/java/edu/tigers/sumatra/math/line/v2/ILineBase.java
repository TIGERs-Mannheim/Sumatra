/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.math.line.v2;

import java.util.Optional;

import edu.tigers.sumatra.math.vector.IEuclideanDistance;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Base interface which groups common line semantics and operators that apply to all the different line subtypes.
 * The different line types that inherit from this interface are designed to be fault-tolerant. This means that an input
 * validation does not occur upon instantiation and new instances can be defined with invalid input parameters, such as
 * an unbounded line with a zero-length direction vector. Each implementation provides a special method
 * {@link #isValid()} to check whether the supplied instance is valid. The instance methods are tolerant and do
 * not require the line to be valid. Functions such as {@link #directionVector()} or {@link #getAngle()} use
 * {@link Optional} to convey an invalid line while other methods such as {@link #closestPointOnLine(IVector2)} will do
 * a best-effort job to return a meaningful value. The method comments specify how the individual methods handle invalid
 * line instances.
 *
 * @author Lukas Magel
 */
public interface ILineBase extends IEuclideanDistance
{
	/**
	 * Returns true if this line instance is properly defined. Whether or not a line instance is valid depends on the
	 * input parameters that were used to define it. The result of other methods depends on the validity of a line.
	 * A half-line requires a non-zero direction vector to be valid, whereas a line segment must be defined through two
	 * non-equal support points to be properly defined.
	 *
	 * @return {@code true} if the line instance is properly defined, {@code false} otherwise
	 */
	boolean isValid();


	/**
	 * Creates a legacy line to use old functions
	 *
	 * @return a new legacy line with same bias and slope
	 */
	edu.tigers.sumatra.math.line.Line toLegacyLine();


	/**
	 * Returns the direction vector of this line. It represents the direction in which this line points. Please note that
	 * even a bounded line segment has a direction vector which points from start to end. Since the different line types
	 * are zero-vector tolerant, the direction vector can also be zero.
	 *
	 * @return The direction vector of this line instance which can have a length of zero
	 */
	IVector2 directionVector();


	/**
	 * Returns the support vector of this line. The support vector represents the starting point of the line, i.e. where
	 * it is anchored. Please note that even a bounded line segment has a support vector which equals the start vector.
	 *
	 * @return The support vector of this line instance which can have a length of zero
	 */
	IVector2 supportVector();


	/**
	 * Returns the slope of this line. Considering the regular definition {@code y = m * x + b} of a line, this method
	 * returns the parameter {@code m} of this instance.
	 *
	 * @return The slope value, or an empty optional if the line is not valid, or the line is parallel
	 * to the y axis and the slope therefore unbounded.
	 */
	Optional<Double> getSlope();


	/**
	 * Returns the angle bounded by the direction vector of this line and the x-axis. Considering the regular definition
	 * {@code y = m * x + b} of a line, this method returns arctan(m).
	 *
	 * @return The angle in radiant with a range of [-Pi, Pi] or an empty optional if this line is not valid
	 * @see IVector2#getAngle()
	 */
	Optional<Double> getAngle();


	/**
	 * Create a deep copy of the {@code line} instance. The resulting instance does not share its variables with the
	 * current instance.
	 *
	 * @return A new line instance which is equal to the other one
	 */
	ILineBase copy();


	/**
	 * Returns true if this line is horizontal, i.e. its direction vector is parallel to the x axis. Please note that a
	 * line can only be considered horizontal if it is valid.
	 *
	 * @return {@code true} if the line is valid and the direction vector is parallel to the x axis
	 * @see IVector2#isHorizontal()
	 */
	boolean isHorizontal();


	/**
	 * Returns true if this line is vertical, i.e. its direction vector is parallel to the y axis. Please note that a
	 * line can only be considered vertical if it is valid.
	 *
	 * @return {@code true} if the line is valid and the direction vector is parallel to the y axis
	 * @see IVector2#isVertical()
	 */
	boolean isVertical();


	/**
	 * Returns {@code true} if the specified point is located on this line. The check uses a small margin value and
	 * verifies if the specified point is located inside this margin around the line. Please note that the result can
	 * vary depending on the sub type, i.e. the function might return true for a line instance but false for a half-line
	 * instance with the same support and direction vector.
	 * If the line instance is not properly defined (i.e. a line segment with to identical points or a half-line with
	 * zero direction vector) then this method only returns true if the point is located on the line origin (for a line
	 * or half-line) or on one of the segment points (for a line segment).
	 *
	 * @param point The point for which this check is to be performed
	 * @return {@code true} if the line is valid and the point is located on it
	 */
	boolean isPointOnLine(IVector2 point);


	/**
	 * Returns a point on the line which is located closest to the specified point. For an unbounded line this is always
	 * the lead point. For a half-line or a line segment this is either the lead point or one of the bounding points if
	 * the lead point is not located on the segment.
	 * If the line instance is not valid, this method returns the line origin (for a line or half-line) or one of the
	 * segment points (for a line segment).
	 *
	 * @param point The point for which to find the closest point on the line
	 * @return A point on the line located closest to the specified point
	 */
	IVector2 closestPointOnLine(IVector2 point);


	/**
	 * Returns {@code true} if the specified {@code line} is parallel to this instance, i.e. the direction vectors of
	 * both lines are linear dependent. This is only the case if both lines are valid and the method will always return
	 * {@code false} if at least one of them is not valid.
	 *
	 * @param line The line which to compare to this instance
	 * @return {@code true} if both lines are valid according to {@link #isValid()} and parallel to each other
	 * @see IVector2#isParallelTo(IVector2)
	 */
	boolean isParallelTo(final ILineBase line);


	/**
	 * Converts this instance into an unbounded line. The resulting line uses the same support and direction vector as
	 * this line instance. Please note that the resulting {@link ILine} instance is only valid if this instance is also
	 * valid.
	 *
	 * @return A line instance which uses the same support and direction vector as the current instance
	 */
	ILine toLine();


	/**
	 * Intersect this line instance with the specified unbounded {@code line} and return the intersection vector.
	 *
	 * @param line The line for which to calculate an intersection with this instance
	 * @return The intersection point of both lines or an empty {@link Optional} if at least one of the two lines is not
	 * valid, the lines are parallel or this line instance is not unbounded and the intersection would be
	 * located outside the range.
	 */
	Optional<IVector2> intersectLine(ILine line);


	/**
	 * Intersect this line instance with the specified {@code halfLine} and return the intersection point.
	 *
	 * @param halfLine The half-line for which to calculate an intersection with this instance
	 * @return The intersection point of both lines r an empty {@link Optional} if at least one of the two lines is not
	 * valid, the lines are parallel or the intersection point was located outside their bounds.
	 */
	Optional<IVector2> intersectHalfLine(IHalfLine halfLine);


	/**
	 * Intersect this line instance with the specified line {@code segment} and return the intersection point.
	 *
	 * @param segment The line segment for which to calculate an intersection with this instance
	 * @return The intersection point of both lines r an empty {@link Optional} if at least one of the two lines is not
	 * valid, the lines are parallel or the intersection point was located outside their bounds.
	 */
	Optional<IVector2> intersectSegment(ILineSegment segment);


	/**
	 * Calculate the distance between the specified {@code point} and the line instance. This value is not defined for an
	 * invalid line instance and the method will consequently throw a {@code RuntimeException}.
	 *
	 * @param point target point
	 * @return The distance between the specified {@code point} and this line instance
	 * @throws IllegalArgumentException If this line is not valid according to the {@link #isValid()} method
	 */
	@Override
	double distanceTo(IVector2 point);

	/**
	 * Same as {@link #distanceTo(IVector2)}, but returns the squared distance to avoid sqrt calculation.
	 *
	 * @param point
	 * @return
	 */
	double distanceToSqr(IVector2 point);
}
