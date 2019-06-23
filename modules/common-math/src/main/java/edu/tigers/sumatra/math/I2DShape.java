/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * This is a Interface for all 2 dimensional geometric shapes,
 * such as Circles and Polygons.
 * 
 * @author Malte
 */
public interface I2DShape
{
	/**
	 * checks if the point is inside the shape
	 * 
	 * @param point
	 * @return true if inside (borders included!!)
	 */
	default boolean isPointInShape(final IVector2 point)
	{
		return isPointInShape(point, 0.0);
	}
	
	
	/**
	 * checks if the point is inside the shape. If the point is exactly on the shape border, the result is undefined
	 * as with floating point values, no equality can be guarantied
	 *
	 * @param point
	 * @param margin
	 * @return true if inside
	 */
	default boolean isPointInShape(IVector2 point, double margin)
	{
		throw new NotImplementedException();
	}
	
	
	/**
	 * Check if the shape is intersecting with given line
	 *
	 * @param line the line to check
	 * @return true, if there is an intersection
	 */
	default boolean isIntersectingWithLine(ILine line)
	{
		throw new NotImplementedException();
	}
	
	
	/**
	 * Returns the neares point outside a shape to a given point inside the shape.
	 * If the given point is outside the shape, return the point.
	 * 
	 * @param point some point in- or outside
	 * @return the nearest point outside, if point is inside, else the point itself
	 */
	default IVector2 nearestPointOutside(IVector2 point)
	{
		throw new NotImplementedException();
	}
	
	
	/**
	 * Returns the neares point inside a shape to a given point outside the shape.
	 * If the given point is inside the shape, return the point.
	 *
	 * @param point some point in- or outside
	 * @return the nearest point inside, if point is outside, else the point itself
	 */
	default IVector2 nearestPointInside(final IVector2 point)
	{
		throw new NotImplementedException();
	}
	
	
	/**
	 * Get the intersection points of the shape and line
	 * 
	 * @param line some line
	 * @return all intersection points
	 */
	default List<IVector2> lineIntersections(ILine line)
	{
		throw new NotImplementedException();
	}
	
	
	/**
	 * @param line the line segment
	 * @return true, if the line intersects the shape and intersection point is on segment
	 */
	default boolean isIntersectingWithLineSegment(ILine line)
	{
		List<IVector2> intersecs = lineIntersections(line);
		Rectangle rect = Rectangle.fromLineSegment(line);
		for (IVector2 inters : intersecs)
		{
			if (rect.isPointInShape(inters))
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Create a new shape of the same type with an additional margin.
	 * Implementation depends on the actual shape.
	 *
	 * @param margin a positiv or negativ margin
	 * @return a new shape with an additional margin
	 */
	default I2DShape withMargin(double margin)
	{
		throw new NotImplementedException();
	}
}
