/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;

import java.util.Optional;


/**
 * Line related calculations.
 * Please consider using the methods from {@link ILine} instead of these static methods!
 *
 * @author nicolai.ommer
 * @author kai.ehrensperger
 */
public final class LineMath
{
	private static final double ACCURACY = SumatraMath.getEqualTol();
	
	
	@SuppressWarnings("unused")
	private LineMath()
	{
	}
	
	
	/**
	 * @param line a line
	 * @return the slope of this line, if line is not parallel to y-axis
	 */
	public static Optional<Double> getSlope(ILine line)
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
	public static Optional<Double> getYIntercept(ILine line)
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
	public static Optional<Double> getYValue(ILine line, final double x)
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
	public static Optional<Double> getXValue(ILine line, final double y)
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
	public static Optional<Double> getAngle(ILine line)
	{
		if (line.directionVector().isZeroVector())
		{
			return Optional.empty();
		}
		return Optional.of(line.directionVector().getAngle());
	}
	
	
	/**
	 * Create the lead point on a straight line (Lot faellen).
	 *
	 * @see ILine#leadPointOf(IVector2)
	 * @param line a line
	 * @param point a point
	 * @return lead point of point on line
	 */
	public static Vector2 leadPointOnLine(final ILine line, final IVector2 point)
	{
		return getPointOnLineForLambda(
				line,
				getLeadPointLambda(point, line));
	}
	
	
	/**
	 * This methods calculate the point where two lines (line1, line2) intersect.
	 * If lines are parallel, there is no unique intersection point.
	 *
	 * @see ILine#intersectionWith(ILine)
	 * @param line1 first line
	 * @param line2 second line
	 * @return the intersection point of both lines, if there is one
	 */
	public static Optional<IVector2> intersectionPoint(final ILine line1, final ILine line2)
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
	 * @see ILine#intersectionOfSegments(ILine)
	 * @param line1 first line segment
	 * @param line2 second line segment
	 * @return the intersection point of the two paths if one exist
	 */
	public static Optional<IVector2> intersectionPointOfSegments(
			final ILine line1,
			final ILine line2)
	{
		if (line1.isParallelTo(line2))
		{
			return Optional.empty();
		}
		final double lambda = getLineIntersectionLambda(line1, line2);
		final double delta = getLineIntersectionLambda(line2, line1);
		if (isLambdaInRange(lambda, 0, 1) && isLambdaInRange(delta, 0, 1))
		{
			return Optional.of(getPointOnLineForLambda(line1, lambda));
		}
		return Optional.empty();
	}
	
	
	/**
	 * calculates the intersection point of a line with a line-segment (path).
	 *
	 * @param line a unbounded line
	 * @param segment a line segment
	 * @return the intersection point on the path or null if not intersecting
	 */
	public static Optional<IVector2> intersectionPointWithSegment(final ILine line, final ILine segment)
	{
		final IVector2 pLine = line.supportVector();
		final IVector2 vLine = line.directionVector();
		if (vLine.isParallelTo(segment.directionVector()))
		{
			return Optional.empty();
		}
		final double lambda = getLineIntersectionLambda(segment.supportVector(), segment.directionVector(), pLine, vLine);
		if (isLambdaInRange(lambda, 0, 1))
		{
			return Optional.of(getPointOnLineForLambda(segment.supportVector(), segment.directionVector(), lambda));
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
	public static Optional<IVector2> intersectionPointOfLineAndHalfLine(final ILine line, final ILine halfLine)
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
	public static Optional<IVector2> intersectionPointOfHalfLineAndHalfLine(final ILine halfLineA, final ILine halfLineB)
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
	 * Check if the given point is on this line <b>segment</b>.
	 * The direction vector is considered as the segment.
	 *
	 * @param line a line
	 * @param point some point
	 * @param margin some margin
	 * @return true, if the given point is on this line segment
	 */
	public static boolean isPointOnLineSegment(ILine line, final IVector2 point, final double margin)
	{
		IVector2 lp = leadPointOnLine(line, point);
		double dist = VectorMath.distancePP(point, lp);
		if (dist > margin)
		{
			return false;
		}
		double lineLength = line.directionVector().getLength();
		return VectorMath.distancePP(lp, line.supportVector()) <= (lineLength + margin)
				&& VectorMath.distancePP(lp, line.supportVector().addNew(line.directionVector())) <= (lineLength + margin);
	}
	
	
	/**
	 * Checks if a point lies in front of the line.<br>
	 * The point is considered lying in front, if it is
	 * within a 90 degree opening in each direction from
	 * the direction vector.
	 *
	 * @param line a line
	 * @param point Point to check
	 * @return True if the point is in front of the line
	 */
	public static boolean isPointInFront(final ILine line, final IVector2 point)
	{
		Vector2 b = point.subtractNew(line.supportVector());
		// angle above 90deg
		return line.directionVector().normalizeNew().scalarProduct(b.normalize()) >= 0;
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
	public static Vector2 nearestPointOnLineSegment(final ILine line, final IVector2 point)
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
	public static Vector2 nearestPointOnHalfLine(final ILine halfLine, final IVector2 point)
	{
		if (!halfLine.isPointInFront(point))
		{
			return Vector2.copy(halfLine.supportVector());
		}
		
		return leadPointOnLine(halfLine, point);
	}
	
	
	/**
	 * calculates the intersection-coefficient of the first line given as supportVector1 and directionVector1 and the
	 * second line
	 * build from supportVector2 and directionVector2.
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
	 * @throws IllegalStateException if both lines are parallel
	 */
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
		
		if (Math.abs(detDR) < (ACCURACY * ACCURACY))
		{
			throw new IllegalStateException(
					"the two lines are parallel! Should not happen but when it does tell KaiE as this means there might be a bug");
		}
		return (detRS - detRX) / detDR;
	}
	
	
	private static double getLineIntersectionLambda(
			final ILine line1,
			final ILine line2)
	{
		return getLineIntersectionLambda(line1.supportVector(), line1.directionVector(),
				line2.supportVector(), line2.directionVector());
	}
	
	
	/**
	 * returns point on line with support-vector s and direction vector d with the given lambda.
	 * solves axpy of vector line function
	 *
	 * @param s support vector
	 * @param d direction vector
	 * @param lambda the lambda value
	 * @return point on line
	 */
	private static Vector2 getPointOnLineForLambda(final IVector2 s, final IVector2 d, final double lambda)
	{
		final double xcut = s.x() + (d.x() * lambda);
		final double ycut = s.y() + (d.y() * lambda);
		return Vector2.fromXY(xcut, ycut);
	}
	
	
	private static Vector2 getPointOnLineForLambda(final ILine line, final double lambda)
	{
		return getPointOnLineForLambda(line.supportVector(), line.directionVector(), lambda);
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
	
	
	/**
	 * calculates the lambda for a point if on the line. Returns NaN when the point was not
	 * part of the line
	 *
	 * @param point a point
	 * @param line a line
	 * @return lambda
	 */
	private static double getLeadPointLambda(final IVector2 point, ILine line)
	{
		final IVector2 ortho = Vector2f.fromXY(line.directionVector().y(), -line.directionVector().x());
		if (line.directionVector().isParallelTo(ortho))
		{
			return 0;
		}
		
		return getLineIntersectionLambda(line.supportVector(), line.directionVector(), point, ortho);
	}
	
	
	/**
	 * Calculates the distance between a point and a line.
	 *
	 * @param point a point
	 * @param line a line
	 * @return euclidean distance between point and line
	 */
	public static double distancePL(final IVector2 point, final ILine line)
	{
		return VectorMath.distancePP(point, leadPointOnLine(line, point));
	}
	
	
	/**
	 * Calculates the distance between a specified {@code point} and the {@code halfLine}.
	 *
	 * @param point
	 *           A point for which to calculate the distance to the half-line
	 * @param halfLine
	 *           A half-line
	 * @return
	 * 			euclidean distance between the point and the half-line
	 */
	public static double distancePointHalfLine(final IVector2 point, final ILine halfLine)
	{
		return VectorMath.distancePP(point, nearestPointOnHalfLine(halfLine, point));
	}
	
	
	/**
	 * Calculates the distance between a specified {@code point} and the {@code lineSegment}.
	 * 
	 * @param point
	 *           The point for which to calculate the minimum distance to the line segment
	 * @param lineSegment
	 *           The line segment for which calculate the distance to the point
	 * @return
	 * 			The absolute euclidean distance
	 */
	public static double distancePointLineSegment(final IVector2 point, final ILine lineSegment)
	{
		return VectorMath.distancePP(point, nearestPointOnLineSegment(lineSegment, point));
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
		return edu.tigers.sumatra.math.line.v2.LineMath.stepAlongLine(start, end, stepSize);
	}
}
