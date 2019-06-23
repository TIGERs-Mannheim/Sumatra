/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


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
	 * @see <a href="https://de.wikipedia.org/wiki/Kreistangente">Kreistangente</a>
	 * @param circle a circle
	 * @param externalPoint some point
	 * @return two tangential intersections: [right, left]
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
	 * Get the intersection points of the shape and line
	 *
	 * @see <a href="http://mathworld.wolfram.com/Circle-LineIntersection.html">Mathmatical Theory</a>
	 * @param circle a circle
	 * @param line some line
	 * @return all intersection points
	 */
	public static List<IVector2> lineIntersectionsCircle(ICircular circle, final ILine line)
	{
		final List<IVector2> result = new ArrayList<>();
		
		if (line.directionVector().isZeroVector())
		{
			if (abs(line.supportVector().distanceTo(circle.center()) - circle.radius()) < 1e-4)
			{
				result.add(line.supportVector());
			}
			return result;
		}
		
		final double dx = line.directionVector().x();
		final double dy = line.directionVector().y();
		final double dr = line.directionVector().getLength2();
		final Vector2 newSupport = line.supportVector().subtractNew(circle.center());
		final double det = (newSupport.x() * (newSupport.y() + dy)) - ((newSupport.x() + dx) * newSupport.y());
		
		final double inRoot = (circle.radius() * circle.radius() * dr * dr) - (det * det);
		
		if (inRoot < 0)
		{
			return result;
		}
		
		if (SumatraMath.isZero(inRoot))
		{
			final Vector2 temp = Vector2.fromXY(
					(det * dy) / (dr * dr),
					(-det * dx) / (dr * dr));
			// because of moved coordinate system (newSupport):
			temp.add(circle.center());
			
			result.add(temp);
			
			return result;
		}
		final double sqRoot = SumatraMath.sqrt(inRoot);
		
		final Vector2 temp1 = Vector2.fromXY(
				((det * dy) + (dx * sqRoot)) / (dr * dr),
				((-det * dx) + (dy * sqRoot)) / (dr * dr));
		final Vector2 temp2 = Vector2.fromXY(
				((det * dy) - (dx * sqRoot)) / (dr * dr),
				((-det * dx) - (dy * sqRoot)) / (dr * dr));
		// because of moved coordinate system (newSupport):
		temp1.add(circle.center());
		temp2.add(circle.center());
		
		result.add(temp1);
		result.add(temp2);
		return result;
	}
	
	
	/**
	 * Get intersection points between circle and line segment
	 * 
	 * @param circle the circle
	 * @param line the line segment
	 * @return all intersection points
	 */
	public static List<IVector2> lineSegmentIntersections(final ICircle circle, final ILine line)
	{
		List<IVector2> candidates = lineIntersectionsCircle(circle, line);
		Rectangle rect = Rectangle.fromLineSegment(line);
		candidates.removeIf(c -> !rect.isPointInShape(c));
		return candidates;
	}
	
	
	/**
	 * Calculate the nearest point outside the circle
	 * 
	 * @param circle a circle
	 * @param point a point inside or outside
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
	
	
	/**
	 * Check if given point is within the circle
	 * 
	 * @param circle a circle
	 * @param point a point
	 * @param margin some margin
	 * @return true, if point is in circle+margin
	 */
	public static boolean isPointInCircle(ICircular circle, IVector2 point, double margin)
	{
		return point.distanceToSqr(circle.center()) <= Math.pow(circle.radius() + margin, 2);
	}
	
	
	/**
	 * Check if a given point is within an arc
	 * 
	 * @param arc an arc
	 * @param point some point
	 * @param margin some margin
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
		return abs(AngleMath.difference(a, b)) < (abs(arc.getRotation()) / 2.0);
	}
	
	
	/**
	 * Get line intersections for an arc. Only the outer arc is considered, not the straight lines.
	 * 
	 * @param arc an arc
	 * @param line a line
	 * @return all intersections of line and arc
	 */
	public static List<IVector2> lineIntersectionsArc(final IArc arc, final ILine line)
	{
		return lineIntersectionsCircle(arc, line).stream()
				.filter(intersections -> isPointInArc(arc, intersections, 1e-6))
				.collect(Collectors.toCollection(ArrayList::new));
	}
	
	
	/**
	 * @param arc an arc
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
	
	
	/**
	 * calculates a point on a circle defined by center and current vectors
	 * performs a projection (rotation) of {@link IVector2}<br>
	 * Note: Consider using {@link edu.tigers.sumatra.math.vector.AVector2#turnNew(double)}
	 *
	 * @param current point on circle
	 * @param center of circle
	 * @param angle of rotation in radians
	 * @return projected point
	 * @see edu.tigers.sumatra.math.vector.AVector2#turnNew(double)
	 */
	public static Vector2 stepAlongCircle(final IVector2 current, final IVector2 center, final double angle)
	{
		return current.subtractNew(center).turn(angle).add(center);
	}
}
