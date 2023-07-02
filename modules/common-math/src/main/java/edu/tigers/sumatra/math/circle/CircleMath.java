/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IPath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;


/**
 * Circle related calculations.
 * Please consider using the methods from {@link ICircle} instead of these static methods!
 *
 * @author nicolai.ommer
 */
public final class CircleMath
{

	@SuppressWarnings("unused")
	private CircleMath()
	{
	}


	/**
	 * Get the intersection points of the two tangential lines that cross the external points.
	 *
	 * @param circle        a circle
	 * @param externalPoint some point
	 * @return two tangential intersections: [right, left]
	 * @see <a href="https://de.wikipedia.org/wiki/Kreistangente">Kreistangente</a>
	 */
	public static List<IVector2> tangentialIntersections(final ICircle circle, final IVector2 externalPoint)
	{
		IVector2 dir = externalPoint.subtractNew(circle.center());
		double d = Math.max(circle.radius(), dir.getLength2());
		double alpha = SumatraMath.acos(circle.radius() / d);
		double beta = dir.getAngle();

		List<IVector2> points = new ArrayList<>(2);
		points.add(Vector2.fromAngleLength(beta + alpha, circle.radius()).add(circle.center()));
		points.add(Vector2.fromAngleLength(beta - alpha, circle.radius()).add(circle.center()));
		return points;
	}


	/**
	 * Calculate the nearest point outside the circle
	 *
	 * @param circle a circle
	 * @param point  a point inside or outside
	 * @return the nearest point outside, if point is inside. The point else.
	 */
	public static IVector2 nearestPointOutsideCircle(final ICircular circle, final IVector2 point)
	{
		final Vector2 direction = point.subtractNew(circle.center());
		final double factor = circle.radius() / direction.getLength2();

		if (Double.isFinite(factor))
		{
			if (factor <= 1)
			{
				return point;
			}
			direction.multiply(factor);
			direction.add(circle.center());
			return direction;
		}
		return point.addNew(Vector2f.fromXY(circle.radius(), 0));
	}


	public static IVector2 nearestPointInsideCircle(final ICircular circle, final IVector2 point)
	{
		if (circle.isPointInShape(point))
		{
			return point;
		}
		IVector2 pointToCircle = circle.center().subtractNew(point);
		return point.addNew(pointToCircle.scaleToNew(pointToCircle.getLength() - circle.radius()));
	}


	/**
	 * Check if given point is within the circle
	 *
	 * @param circle a circle
	 * @param point  a point
	 * @return true, if point is in circle+margin
	 */
	public static boolean isPointInCircle(ICircular circle, IVector2 point, double margin)
	{
		return point.distanceToSqr(circle.center()) <= SumatraMath.square(circle.radius() + margin);
	}


	/**
	 * Check if a given point is within an arc
	 *
	 * @param arc   an arc
	 * @param point some point
	 * @return true, if the point is within the arc
	 */
	public static boolean isPointInArc(final IArc arc, final IVector2 point, double margin)
	{
		if (!CircleMath.isPointInCircle(arc, point, margin))
		{
			return false;
		}
		if (arc.center().equals(point))
		{
			return true;
		}

		IVector2 dir = point.subtractNew(arc.center());
		double a = dir.getAngle();
		double b = AngleMath.normalizeAngle(arc.getStartAngle() + (arc.getRotation() / 2.0));
		return abs(AngleMath.difference(a, b)) <= (abs(arc.getRotation()) / 2.0) + IPath.LINE_MARGIN;
	}


	/**
	 * @param arc   an arc
	 * @param point a point
	 * @return nearest point outside the arc
	 */
	public static IVector2 nearestPointOutsideArc(final IArc arc, final IVector2 point)
	{
		IVector2 npo = nearestPointOutsideCircle(arc, point);
		if (isPointInArc(arc, npo, 1e-6))
		{
			return npo;
		}
		return point;
	}


	public static IVector2 nearestPointOnCircleLine(final ICircle circle, final IVector2 point)
	{
		IVector2 center = circle.center();
		IVector2 centerToPoint = point.subtractNew(center);
		if (centerToPoint.isZeroVector())
		{
			return circle.getPathStart();
		}
		var halfLine = Lines.halfLineFromDirection(center, centerToPoint);
		return point.nearestToOpt(circle.intersect(halfLine).asList())
				.orElseThrow(() -> new IllegalArgumentException(
						"Not exactly one intersection between half line starting from within a circle, this is impossible"));
	}


	public static IVector2 nearestPointOnArcLine(final IArc arc, final IVector2 point)
	{
		IVector2 center = arc.center();
		IVector2 centerToPoint = point.subtractNew(center);
		IVector2 startPoint = arc.center().addNew(Vector2.fromAngle(arc.getStartAngle()).scaleToNew(arc.radius()));
		IVector2 endPoint = arc.center()
				.addNew(Vector2.fromAngle(arc.getStartAngle() + arc.getRotation()).scaleToNew(arc.radius()));
		if (centerToPoint.isZeroVector())
		{
			return startPoint;
		}
		var halfLine = Lines.halfLineFromDirection(center, centerToPoint);
		var intersections = arc.intersect(halfLine).asList();

		if (intersections.isEmpty())
		{
			// only consider corners
			if (startPoint.distanceTo(point) < endPoint.distanceTo(point))
			{
				return startPoint;
			}
			return endPoint;
		} else if (intersections.size() == 1)
		{
			return intersections.get(0);
		}
		throw new IllegalArgumentException(
				"More than one intersection between half line starting from within a circle, this is impossible");
	}


	/**
	 * calculates a point on a circle defined by center and current vectors
	 * performs a projection (rotation) of {@link IVector2}<br>
	 * Note: Consider using {@link edu.tigers.sumatra.math.vector.AVector2#turnNew(double)}
	 *
	 * @param current point on circle
	 * @param center  of circle
	 * @param angle   of rotation in radians
	 * @return projected point
	 * @see edu.tigers.sumatra.math.vector.AVector2#turnNew(double)
	 */
	public static Vector2 stepAlongCircle(final IVector2 current, final IVector2 center, final double angle)
	{
		return current.subtractNew(center).turn(angle).add(center);
	}
}
