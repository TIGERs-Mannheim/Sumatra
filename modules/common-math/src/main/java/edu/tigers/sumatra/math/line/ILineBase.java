/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.IPath;
import edu.tigers.sumatra.math.intersections.ISingleIntersection;
import edu.tigers.sumatra.math.vector.IEuclideanDistance;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Optional;


/**
 * Base interface which groups common line semantics and operators that apply to all the different line subtypes.
 * The different line types that inherit from this interface are designed to be fault-tolerant. This means that an input
 * validation does not occur upon instantiation and new instances can be defined with invalid input parameters, such as
 * an unbounded line with a zero-length direction vector. Each implementation provides a special method
 * {@link #isValid()} to check whether the supplied instance is valid. The instance methods are tolerant and do
 * not require the line to be valid. Functions such as {@link #directionVector()} or {@link #getAngle()} use
 * {@link Optional} to convey an invalid line while other methods such as {@link #closestPointOnPath(IVector2)} will do
 * a best-effort job to return a meaningful value. The method comments specify how the individual methods handle invalid
 * line instances.
 *
 * @author Lukas Magel
 */
public interface ILineBase extends IEuclideanDistance, IPath
{
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

	@Override
	ISingleIntersection intersect(ILine line);

	@Override
	ISingleIntersection intersect(IHalfLine halfLine);

	@Override
	ISingleIntersection intersect(ILineSegment segment);
}
