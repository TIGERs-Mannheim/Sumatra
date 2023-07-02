/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.triangle;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.List;


/**
 * Triangle related calculations.
 * Please consider using the methods from {@link ITriangle} instead of these static methods!
 */
public final class TriangleMath
{

	@SuppressWarnings("unused")
	private TriangleMath()
	{
	}


	/**
	 * Create a new triangle by adding a margin.<br>
	 * The margin triangle is calculated by creating new edge lines that are spaced {@code margin} mm from the original
	 * edge. The new corner points are then calculated by intersecting all three lines.
	 *
	 * @param triangle some triangle
	 * @param margin   some margin
	 * @return a new triangle
	 */
	public static ITriangle withMargin(final ITriangle triangle, final double margin)
	{
		List<IVector2> points = triangle.getCorners();
		IVector2 a = points.get(0);
		IVector2 b = points.get(1);
		IVector2 c = points.get(2);

		IVector2 ab = b.subtractNew(a);
		IVector2 bc = c.subtractNew(b);
		IVector2 ca = a.subtractNew(c);

		/*
		 * Calculate the normal of each edge line. The normal has the length of the margin and points in the direction
		 * that the edge line needs to be moved to obtain the new edge line of the triangle with margin.
		 */
		Vector2 abNorm = Vector2.fromXY(ab.y(), -ab.x()).scaleTo(margin);
		Vector2 bcNorm = Vector2.fromXY(bc.y(), -bc.x()).scaleTo(margin);
		Vector2 caNorm = Vector2.fromXY(ca.y(), -ca.x()).scaleTo(margin);

		/*
		 * Check if the normal is pointing outward and flip it if is pointing inward.
		 * The normal points inward if the angle between the normal vector and the vector connecting the other two points
		 * in the triangle is less than 90 degrees.
		 * The most intuitive way to perform this check would be to calculate the scalar product of norm(AB) * AC and flip
		 * the norm vector if the result value is greater than 0. Since this would require calculating the vector AC we
		 * use the vector CA instead and reverse the condition.
		 */
		if (abNorm.scalarProduct(ca) < 0)
		{
			abNorm.multiply(-1.0d);
		}

		if (bcNorm.scalarProduct(ab) < 0)
		{
			bcNorm.multiply(-1.0d);
		}

		if (caNorm.scalarProduct(bc) < 0)
		{
			caNorm.multiply(-1.0d);
		}

		/*
		 * Add the corner points to each normal to obtain the new support vectors for each of the edges.
		 */
		Vector2 abNewSV;
		Vector2 bcNewSV;
		Vector2 caNewSV;
		if (margin > 0)
		{
			abNewSV = abNorm.add(a);
			bcNewSV = bcNorm.add(b);
			caNewSV = caNorm.add(c);
		} else
		{

			abNewSV = abNorm.multiply(-1).add(a);
			bcNewSV = bcNorm.multiply(-1).add(b);
			caNewSV = caNorm.multiply(-1).add(c);
		}
		/*
		 * Calculate the new corner points by intersecting each of the new edge lines which are composed of the new
		 * support vector and the original direction vector.
		 */
		ILine caLine = Lines.lineFromDirection(caNewSV, ca);
		ILine abLine = Lines.lineFromDirection(abNewSV, ab);
		ILine bcLine = Lines.lineFromDirection(bcNewSV, bc);
		var newA = caLine.intersect(abLine).asOptional();
		var newB = abLine.intersect(bcLine).asOptional();
		var newC = bcLine.intersect(caLine).asOptional();

		if (newA.isPresent() && newB.isPresent() && newC.isPresent())
		{
			return Triangle.fromCorners(newA.get(), newB.get(), newC.get());
		}
		throw new IllegalStateException();
	}


	/**
	 * @param triangle some triangle
	 * @param point    some point
	 * @return true, if point is inside triangle
	 */
	public static boolean isPointInShape(final ITriangle triangle, final IVector2 point)
	{
		List<IVector2> points = triangle.getCorners();
		IVector2 a = points.get(0);
		IVector2 b = points.get(1);
		IVector2 c = points.get(2);

		double v1X = a.x();
		double v2X = b.x();
		double v3X = c.x();
		double v1Y = a.y();
		double v2Y = b.y();
		double v3Y = c.y();

		double pointX = point.x();
		double pointY = point.y();

		double d = ((-v2Y * v3X) + (v1Y * (-v2X + v3X)) + (v1X * (v2Y - v3Y)) + (v2X * v3Y)) / 2.0;
		double sign = d < 0 ? -1 : 1;
		double s = (((v1Y * v3X) - (v1X * v3Y)) + ((v3Y - v1Y) * pointX) + ((v1X - v3X) * pointY)) * sign;
		double t = (((v1X * v2Y) - (v1Y * v2X)) + ((v1Y - v2Y) * pointX) + ((v2X - v1X) * pointY)) * sign;
		return (s >= 0) && (t >= 0) && ((s + t) <= (2 * d * sign));
	}


	/**
	 * A triangle is defined by three points(p1,p2,p3).
	 * This methods calculates the point(p4) where the bisector("Winkelhalbierende") of the angle(alpha) at p1 cuts the
	 * line p2-p3.
	 * <pre>
	 *        p4
	 *  p2----x----p3
	 *    \   |   /
	 *     \  |  /
	 *      \^|^/
	 *       \|/<--alpha
	 *       p1
	 * </pre>
	 *
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return p4
	 * @author Malte
	 */
	public static Vector2 bisector(final IVector2 p1, final IVector2 p2, final IVector2 p3)
	{
		if (p1.equals(p2) || p1.equals(p3))
		{
			return Vector2.copy(p1);
		}
		if (p2.equals(p3))
		{
			return Vector2.copy(p2);
		}
		final Vector2 p1p2 = p2.subtractNew(p1);
		final Vector2 p1p3 = p3.subtractNew(p1);
		final Vector2 p3p2 = p2.subtractNew(p3);

		p3p2.scaleTo(p3p2.getLength2() / ((p1p2.getLength2() / p1p3.getLength2()) + 1));
		p3p2.add(p3);

		return p3p2;
	}
}
