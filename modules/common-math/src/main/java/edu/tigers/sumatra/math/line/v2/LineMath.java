/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;


/**
 * This class groups all Line related calculations.
 * Please consider using the methods from {@link ILine} instead of these static methods!
 *
 * @author nicolai.ommer
 * @author kai.ehrensperger
 */
public final class LineMath
{
	private static final Logger log = Logger
			.getLogger(LineMath.class.getName());
	private static final double ACCURACY = SumatraMath.getEqualTol();
	
	
	@SuppressWarnings("unused")
	private LineMath()
	{
	}
	
	
	/**
	 * calculates a point on a line between start and end, that is stepSize away from start
	 * calculation is based on Intercept theorem (Strahlensatz)
	 *
	 * @param start from
	 * @param end to
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
			log.warn(String.format("stepAlongLine(%s,%s,%s) = %s -> NaNs!", start, end, stepSize, result),
					new Exception());
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
	 * @param x a value
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
	 * @param y a value
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
	 * @param line a line
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
	 * @param line a line
	 * @param point a point
	 * @return lead point of point on line
	 */
	static Vector2 closestPointOnLine(final ILine line, final IVector2 point)
	{
		double lambda = getLeadPointLambda(point, line);
		return getPointOnLineForLambda(line, lambda);
	}
	
	
	/**
	 * This Method returns the nearest point on the line-segment to a given point.<br>
	 * If the lead point of the argument is not on the segment the
	 * nearest edge-point of the segment (start or end) is returned.
	 *
	 * @param line the line segment
	 * @param point a point
	 * @return the nearest point on the line segment
	 */
	static Vector2 closestPointOnLineSegment(final ILineSegment line, final IVector2 point)
	{
		final double lambda = getLeadPointLambda(point, line);
		if (isLambdaInRange(lambda, 0, 1))
		{
			return getPointOnLineForLambda(line, lambda);
		}
		
		final double dist1 = VectorMath.distancePPSqr(line.getStart(), point);
		final double dist2 = VectorMath.distancePPSqr(line.getEnd(), point);
		return Vector2.copy(dist1 < dist2 ? line.getStart() : line.getEnd());
	}
	
	
	/**
	 * Returns a point on the {@code halfLine} which is located closest to the specified {@code point}. If it is located
	 * behind the support vector, then the support vector itself is returned.
	 *
	 * @param halfLine
	 *           The half-line on which to find the closest point
	 * @param point
	 *           The point for which to find the closest point on the line
	 * @return
	 * 			The closest point on the line
	 */
	static Vector2 closestPointOnHalfLine(final IHalfLine halfLine, final IVector2 point)
	{
		double lambda = getLeadPointLambda(point, halfLine);
		if (isLambdaInRange(lambda, 0, Double.MAX_VALUE))
		{
			return getPointOnLineForLambda(halfLine, lambda);
		}
		return Vector2.copy(halfLine.supportVector());
	}
	
	
	/**
	 * This methods calculate the point where two lines (line1, line2) intersect.
	 * If lines are parallel, there is no unique intersection point.
	 *
	 * @param line1 first line
	 * @param line2 second line
	 * @return the intersection point of both lines, if there is one
	 */
	static Optional<IVector2> intersectionPointOfLines(final ILine line1, final ILine line2)
	{
		if (line1.isParallelTo(line2))
		{
			return Optional.empty();
		}
		final double lambda = getLineIntersectionLambda(line1, line2);
		return Optional.of(getPointOnLineForLambda(line1, lambda));
	}
	
	
	/**
	 * Calculates the intersection point of two line segments (bounded lines).<br>
	 * Only returns intersection points that are on the line segments (direction vector)
	 *
	 * @param segment1 first line segment
	 * @param segment2 second line segment
	 * @return the intersection point of the two paths if one exist
	 */
	static Optional<IVector2> intersectionPointOfSegments(final ILineSegment segment1, final ILineSegment segment2)
	{
		if (segment1.isParallelTo(segment2))
		{
			return Optional.empty();
		}
		final double lambda = getLineIntersectionLambda(segment1, segment2);
		final double delta = getLineIntersectionLambda(segment2, segment1);
		
		if (isLambdaInRange(lambda, 0, 1) && isLambdaInRange(delta, 0, 1))
		{
			return Optional.of(getPointOnLineForLambda(segment1, lambda));
		}
		return Optional.empty();
	}
	
	
	/**
	 * Calculates the intersection point of the specified {@code line} and the {@code lineSegment}
	 *
	 * @param line
	 *           The line
	 * @param lineSegment
	 *           The line segment
	 * @return
	 * 			An {@code Optional} containing the intersection point if one exists
	 */
	static Optional<IVector2> intersectionPointOfLineAndSegment(final ILine line, final ILineSegment lineSegment)
	{
		if (line.isParallelTo(lineSegment))
		{
			return Optional.empty();
		}
		
		double lambda = getLineIntersectionLambda(lineSegment, line);
		if (isLambdaInRange(lambda, 0, 1))
		{
			return Optional.of(getPointOnLineForLambda(lineSegment, lambda));
		}
		return Optional.empty();
	}
	
	
	/**
	 * Calculates the intersection point of the specified {@code halfLine} and the {@code lineSegment}
	 *
	 * @param halfLine
	 *           The half-line
	 * @param lineSegment
	 *           The line segment
	 * @return
	 * 			An {@code Optional} containing the intersection point if any exists
	 */
	static Optional<IVector2> intersectionPointOfHalfLineAndSegment(final IHalfLine halfLine,
			final ILineSegment lineSegment)
	{
		if (halfLine.isParallelTo(lineSegment))
		{
			return Optional.empty();
		}
		
		double halfLineLambda = getLineIntersectionLambda(halfLine, lineSegment);
		double segmentLambda = getLineIntersectionLambda(lineSegment, halfLine);
		
		if (isLambdaInRange(halfLineLambda, 0, Double.MAX_VALUE) && isLambdaInRange(segmentLambda, 0, 1))
		{
			return Optional.of(getPointOnLineForLambda(halfLine, halfLineLambda));
		}
		return Optional.empty();
	}
	
	
	/**
	 * Calculate the intersection point of the specified {@code line} and the {@code halfLine}.
	 *
	 * @param line
	 *           The line which for which to calculate the intersection
	 * @param halfLine
	 *           The half-line for which to calculate the intersection. It extends from the support point in the
	 *           direction of the direction vector.
	 * @return
	 */
	static Optional<IVector2> intersectionPointOfLineAndHalfLine(final ILine line, final IHalfLine halfLine)
	{
		if (line.directionVector().isParallelTo(halfLine.directionVector()))
		{
			return Optional.empty();
		}
		double lambda = getLineIntersectionLambda(halfLine, line);
		
		if (isLambdaInRange(lambda, 0, Double.MAX_VALUE))
		{
			return Optional.of(getPointOnLineForLambda(halfLine, lambda));
		}
		return Optional.empty();
	}
	
	
	/**
	 * Calculate the intersection point of the two specified half-lines {@code halfLineA} and {@code halfLineB}.
	 *
	 * @param halfLineA
	 *           A half-line
	 * @param halfLineB
	 *           A half-line
	 * @return
	 * 			An optional containing the intersection point if one exists
	 */
	static Optional<IVector2> intersectionPointOfHalfLines(final IHalfLine halfLineA,
			final IHalfLine halfLineB)
	{
		if (halfLineA.directionVector().isParallelTo(halfLineB.directionVector()))
		{
			return Optional.empty();
		}
		double lambdaA = getLineIntersectionLambda(halfLineA, halfLineB);
		double lambdaB = getLineIntersectionLambda(halfLineB, halfLineA);
		
		if (isLambdaInRange(lambdaA, 0, Double.MAX_VALUE) && isLambdaInRange(lambdaB, 0, Double.MAX_VALUE))
		{
			return Optional.of(getPointOnLineForLambda(halfLineA, lambdaA));
		}
		return Optional.empty();
	}
	
	
	/**
	 * calculates the lambda for a point if on the line. Returns NaN when the point was not
	 * part of the line
	 *
	 * @param point a point
	 * @param line a line
	 * @return lambda
	 */
	private static double getLeadPointLambda(final IVector2 point, final ILineBase line)
	{
		IVector2 supportVector = line.supportVector();
		IVector2 directionVector = line.directionVector();
		
		final IVector2 ortho = Vector2f.fromXY(directionVector.y(), -directionVector.x());
		if (directionVector.isParallelTo(ortho))
		{
			return 0;
		}
		
		return getLineIntersectionLambda(supportVector, directionVector, point, ortho);
	}
	
	
	private static double getLineIntersectionLambda(final ILineBase lineA, final ILineBase lineB)
	{
		return getLineIntersectionLambda(lineA.supportVector(), lineA.directionVector(),
				lineB.supportVector(), lineB.directionVector());
	}
	
	
	/**
	 * calculates the intersection-coefficient of the first line given as supportVector1 and directionVector1 and the
	 * second line build from supportVector2 and directionVector2.
	 *
	 * <pre>
	 * :: Let the following variables be defined as:
	 * s1 = supportVector1.x
	 * s2 = supportVector1.y
	 * d1 = directionVector1.x
	 * d2 = directionVector1.y
	 * x1 = supportVector2.x
	 * x2 = supportVector2.y
	 * r1 = directionVector2.x
	 * r2 = directionVector2.y
	 * ::
	 * Basic equations: s1 + lambda*d1 = x1 + gamma*r1
	 *                  s2 + lambda*d2 = x2 + gamma*r2
	 * ==============================================
	 * s1 + lambda*d1 = x1 + gamma*r1
	 *
	 * s1 - x1 + lambda*d1 = gamma*r1
	 *
	 * s1 - x1 + lambda*d1
	 * ------------------- = gamma
	 *          r1
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Insert into 2nd dim:
	 *
	 *                            s1 - x1 + lambda*d1
	 * s2 + lambda * d2 = x2 + (----------------------)*r2
	 *                                     r1
	 *
	 *
	 * (s2*r1) + (lambda*d2*r1) = (x2*r1) + (s1*r2) - (x1*r2) + (lambda*d1*r2)
	 *
	 * with a sharp eye one can notice some determinants of 2d-matrices...
	 *
	 *  ((r1*s2)-(r2*s1)) - ((r1*x2)-(r2*x1)) = lambda *((d1*r2)-(d2*r1))
	 *
	 *  ^^^^^^^^^^^^^^^^^    ^^^^^^^^^^^^^^^^           ^^^^^^^^^^^^^^^^^
	 *       detRS               detRX                        detDR
	 *
	 *  ==> if detDR==0 -> parallel
	 *
	 *                detRS - detRX
	 *  ==> lambda = ---------------
	 *                    detDR
	 * </pre>
	 *
	 * @param supportVector1 first support vector
	 * @param directionVector1 first direction vector
	 * @param supportVector2 second support vector
	 * @param directionVector2 second direction vector
	 * @return the lambda for the first line
	 */
	@SuppressWarnings("squid:S1244") // floating point comparison is ok here, cause it only protects against div by zero
	private static double getLineIntersectionLambda(
			final IVector2 supportVector1,
			final IVector2 directionVector1,
			final IVector2 supportVector2,
			final IVector2 directionVector2)
	{
		final double s1 = supportVector1.x();
		final double s2 = supportVector1.y();
		final double d1 = directionVector1.x();
		final double d2 = directionVector1.y();
		
		final double x1 = supportVector2.x();
		final double x2 = supportVector2.y();
		final double r1 = directionVector2.x();
		final double r2 = directionVector2.y();
		
		
		final double detRS = (r1 * s2) - (r2 * s1);
		final double detRX = (r1 * x2) - (r2 * x1);
		final double detDR = (d1 * r2) - (d2 * r1);
		
		if (Math.abs(detDR) == 0.0)
		{
			throw new IllegalStateException(
					"the two lines are parallel! Should not happen but when it does tell KaiE as this means there might be a bug");
		}
		return (detRS - detRX) / detDR;
	}
	
	
	/**
	 * checks if the given lambda is within the interval [min,max] with the predefined epsilon.
	 *
	 * @param lambda tha lambda value
	 * @param min interval min
	 * @param max interval max
	 * @return true, if lambda is in range
	 */
	private static boolean isLambdaInRange(final double lambda, final double min, final double max)
	{
		return ((min - (ACCURACY * ACCURACY)) < lambda) && (lambda < (max + (ACCURACY * ACCURACY)));
	}
	
	
	private static Vector2 getPointOnLineForLambda(final ILineBase line, final double lambda)
	{
		IVector2 s = line.supportVector();
		IVector2 d = line.directionVector();
		final double xcut = s.x() + (d.x() * lambda);
		final double ycut = s.y() + (d.y() * lambda);
		return Vector2.fromXY(xcut, ycut);
	}
}
