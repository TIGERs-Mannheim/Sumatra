/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.04.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.triangle;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;


/**
 * Mutable implementation of {@link ITriangle}.
 * 
 * @author Malte
 */
@Persistent
public class Triangle extends ATriangle
{
	private final List<IVector2>	points	= new ArrayList<IVector2>();
	
	
	@SuppressWarnings("unused")
	private Triangle()
	{
	}
	
	
	/**
	 * @param a
	 * @param b
	 * @param c
	 */
	public Triangle(final IVector2 a, final IVector2 b, final IVector2 c)
	{
		points.add(a);
		points.add(b);
		points.add(c);
	}
	
	
	/**
	 * @param triangle
	 */
	public Triangle(final Triangle triangle)
	{
		points.addAll(triangle.points);
	}
	
	
	@Override
	public List<IVector2> getCorners()
	{
		return points;
	}
	
	
	@Override
	public double getArea()
	{
		double x1 = getCorners().get(0).x();
		double x2 = getCorners().get(1).x();
		double x3 = getCorners().get(2).x();
		double y1 = getCorners().get(0).y();
		double y2 = getCorners().get(1).y();
		double y3 = getCorners().get(2).y();
		return Math.abs(((x1 * (y2 - y3)) + (x2 * (y3 - y1)) + (x3 * (y1 - y2))) / 2.0);
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		IVector2 a = getCorners().get(0);
		IVector2 b = getCorners().get(1);
		IVector2 c = getCorners().get(2);
		
		return isPointInShape(a, b, c, point);
	}
	
	
	private static boolean isPointInShape(final IVector2 a, final IVector2 b, final IVector2 c, final IVector2 point)
	{
		double v1X = a.x();
		double v2X = b.x();
		double v3X = c.x();
		double v1Y = a.y();
		double v2Y = b.y();
		double v3Y = c.y();
		
		double pointX = point.x();
		double pointY = point.y();
		
		double A = ((-v2Y * v3X) + (v1Y * (-v2X + v3X)) + (v1X * (v2Y - v3Y)) + (v2X * v3Y)) / 2.0;
		double sign = A < 0 ? -1 : 1;
		double s = (((v1Y * v3X) - (v1X * v3Y)) + ((v3Y - v1Y) * pointX) + ((v1X - v3X) * pointY)) * sign;
		double t = (((v1X * v2Y) - (v1Y * v2X)) + ((v1Y - v2Y) * pointX) + ((v2X - v1X) * pointY)) * sign;
		return (s > 0) && (t > 0) && ((s + t) < (2 * A * sign));
	}
	
	
	/**
	 * This function calculates whether a point is located inside or in the specified margin around the triangle. To
	 * perform the calculation it creates a new triangle instance that represents the triangle with margin and invokes
	 * the {@link #isPointInShape(IVector2)} function on it.
	 * The margin triangle is calculated by creating new edge lines that are spaced {@code margin} mm from the original
	 * edge. The new corner points are then calculated by intersecting all three lines.
	 */
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
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
		Vector2 abNorm = new Vector2(ab.y(), -ab.x()).scaleTo(margin);
		Vector2 bcNorm = new Vector2(bc.y(), -bc.x()).scaleTo(margin);
		Vector2 caNorm = new Vector2(ca.y(), -ca.x()).scaleTo(margin);
		
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
		Vector2 abNewSV = abNorm.add(a);
		Vector2 bcNewSV = bcNorm.add(b);
		Vector2 caNewSV = caNorm.add(c);
		
		try
		{
			/*
			 * Calculate the new corner points by intersecting each of the new edge lines which are composed of the new
			 * support vector and the original direction vector.
			 */
			IVector2 newA = GeoMath.intersectionPoint(caNewSV, ca, abNewSV, ab);
			Vector2 newB = GeoMath.intersectionPoint(abNewSV, ab, bcNewSV, bc);
			IVector2 newC = GeoMath.intersectionPoint(bcNewSV, bc, caNewSV, ca);
			
			return isPointInShape(newA, newB, newC, point);
		} catch (MathException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	@Override
	public boolean isLineIntersectingShape(final ILine line)
	{
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		throw new UnsupportedOperationException("Not implemented yet!");
	}
}
