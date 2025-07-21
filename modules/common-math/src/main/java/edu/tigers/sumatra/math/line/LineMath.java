/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.intersections.PathIntersectionMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;


/**
 * This class groups all Line related calculations.
 * Please consider using the methods from {@link ILine} instead of these static methods!
 */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LineMath
{
	/**
	 * calculates a point on a line between start and end, that is stepSize away from start
	 * calculation is based on Intercept theorem (Strahlensatz)
	 *
	 * @param start    from
	 * @param end      to
	 * @param stepSize distance
	 * @return
	 */
	public static IVector2 stepAlongLine(final IVector2 start, final IVector2 end, final double stepSize)
	{
		final double distanceSqr = VectorMath.distancePPSqr(start, end);
		if (distanceSqr < 1e-10)
		{
			return end;
		}

		final double distance = SumatraMath.sqrt(distanceSqr);
		final double coefficient = stepSize / distance;

		final double xDistance = end.x() - start.x();
		final double yDistance = end.y() - start.y();

		final IVector2 result = Vector2f.fromXY(
				(xDistance * coefficient) + start.x(),
				(yDistance * coefficient) + start.y());

		if (Double.isNaN(result.x()) || Double.isNaN(result.y()))
		{
			log.warn("stepAlongLine({},{},{}) = {} -> NaNs!", start, end, stepSize, result, new Exception());
			return Vector2f.zero();
		}

		return result;
	}


	/**
	 * @param line a line
	 * @return the slope of this line, if line is not parallel to y-axis
	 */
	static Optional<Double> getSlope(final ILineBase line)
	{
		if (line.directionVector().isZeroVector() || line.isVertical())
		{
			return Optional.empty();
		}
		return Optional.of(line.directionVector().y() / line.directionVector().x());
	}


	/**
	 * Returns the y-intercept of this Line.
	 * This is the y value where x == 0
	 *
	 * @param line a line
	 * @return the y-intercept of this line if
	 */
	static Optional<Double> getYIntercept(final ILine line)
	{
		if (line.directionVector().isZeroVector() || line.isVertical())
		{
			return Optional.empty();
		}
		double factor = (-line.supportVector().x()) / line.directionVector().x();
		return Optional.of((factor * line.directionVector().y()) + line.supportVector().y());
	}


	/**
	 * Returns the y value to a given x input. <br>
	 * y = m * x + n<br>
	 * Value is not defined for vertical lines
	 *
	 * @param line a line
	 * @param x    a value
	 * @return y value if line is not vertical
	 */
	static Optional<Double> getYValue(final ILine line, final double x)
	{
		Optional<Double> slope = getSlope(line);
		Optional<Double> yIntercept = getYIntercept(line);
		if (slope.isPresent() && yIntercept.isPresent())
		{
			return Optional.of((x * slope.get()) + yIntercept.get());
		}
		return Optional.empty();
	}


	/**
	 * Returns the x value to a given y input. <br>
	 * x = (y - n) / m <br>
	 * Value is not defined for horizontal lines
	 *
	 * @param line a line
	 * @param y    a value
	 * @return x value if line is not horizontal
	 */
	static Optional<Double> getXValue(final ILine line, final double y)
	{
		if (line.isVertical())
		{
			return Optional.of(line.supportVector().x());
		} else if (line.isHorizontal())
		{
			return Optional.empty();
		} else
		{
			Optional<Double> yIntercept = getYIntercept(line);
			Optional<Double> slope = getSlope(line);
			if (yIntercept.isPresent() && slope.isPresent())
			{
				return Optional.of((y - yIntercept.get()) / slope.get());
			}
			return Optional.empty();
		}
	}


	/**
	 * Calculate the angle of the direction vector to the x-axis (just like IVector2#getAngle
	 *
	 * @param line some line
	 * @return the angle, if direction vector is not zero
	 */
	static Optional<Double> getAngle(final ILineBase line)
	{
		if (line.directionVector().isZeroVector())
		{
			return Optional.empty();
		}
		return Optional.of(line.directionVector().getAngle());
	}


	/**
	 * Checks if a point lies in front of the line.<br>
	 * The point is considered lying in front, if it is
	 * within a 90 degree opening on any side of
	 * the direction vector.
	 *
	 * @param line  a line
	 * @param point Point to check
	 * @return True if the point is in front of the line
	 */
	static boolean isPointInFront(final IHalfLine line, final IVector2 point)
	{
		Vector2 b = point.subtractNew(line.supportVector());
		// angle above 90deg
		return line.directionVector().normalizeNew().scalarProduct(b.normalize()) >= 0;
	}


	/**
	 * Create the lead point on a straight line (Lot faellen).
	 *
	 * @param line  a line
	 * @param point a point
	 * @return lead point of point on line
	 */
	static IVector2 closestPointOnLine(final ILine line, final IVector2 point)
	{
		double lambda = getLeadPointLambda(point, line);
		return getPointOnLineForLambda(line, lambda);
	}


	/**
	 * This Method returns the nearest point on the line-segment to a given point.<br>
	 * If the lead point of the argument is not on the segment the
	 * nearest edge-point of the segment (start or end) is returned.
	 *
	 * @param line  the line segment
	 * @param point a point
	 * @return the nearest point on the line segment
	 */
	static IVector2 closestPointOnLineSegment(final ILineSegment line, final IVector2 point)
	{
		final double lambda = getLeadPointLambda(point, line);
		if (PathIntersectionMath.isLineLambdaInRange(lambda, 0, 1))
		{
			return getPointOnLineForLambda(line, lambda);
		}

		final double dist1 = VectorMath.distancePPSqr(line.getPathStart(), point);
		final double dist2 = VectorMath.distancePPSqr(line.getPathEnd(), point);
		return Vector2.copy(dist1 < dist2 ? line.getPathStart() : line.getPathEnd());
	}


	/**
	 * Returns a point on the {@code halfLine} which is located closest to the specified {@code point}. If it is located
	 * behind the support vector, then the support vector itself is returned.
	 *
	 * @param halfLine The half-line on which to find the closest point
	 * @param point    The point for which to find the closest point on the line
	 * @return The closest point on the line
	 */
	static IVector2 closestPointOnHalfLine(final IHalfLine halfLine, final IVector2 point)
	{
		double lambda = getLeadPointLambda(point, halfLine);
		if (PathIntersectionMath.isLineLambdaInRange(lambda, 0, Double.MAX_VALUE))
		{
			return getPointOnLineForLambda(halfLine, lambda);
		}
		return Vector2.copy(halfLine.supportVector());
	}


	/**
	 * calculates the lambda for a point if on the line. Returns NaN when the point was not
	 * part of the line
	 *
	 * @param point a point
	 * @param line  a line
	 * @return lambda
	 */
	private static double getLeadPointLambda(final IVector2 point, final ILineBase line)
	{
		IVector2 supportVector = line.supportVector();
		IVector2 directionVector = line.directionVector();
		var directionLengthSqr = directionVector.getLengthSqr();

		if (SumatraMath.isZero(directionLengthSqr))
		{
			return 0;
		}

		return directionVector.scalarProduct(point.subtractNew(supportVector)) / directionVector.getLengthSqr();
	}


	public static IVector2 getPointOnLineForLambda(final ILineBase line, final double lambda)
	{
		IVector2 s = line.supportVector();
		IVector2 d = line.directionVector();
		final double xcut = s.x() + (d.x() * lambda);
		final double ycut = s.y() + (d.y() * lambda);
		return Vector2.fromXY(xcut, ycut);
	}
}
