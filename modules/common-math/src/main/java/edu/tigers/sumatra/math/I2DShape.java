/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import edu.tigers.sumatra.math.boundary.IShapeBoundary;
import edu.tigers.sumatra.math.boundary.ShapeBoundary;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.intersections.IIntersections;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorDistinctStreamFilter;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;


/**
 * This is a Interface for all 2 dimensional geometric shapes,
 * such as Circles and Polygons.
 * <p>
 * A new shape only needs to implement {@link #isPointInShape)} {@link #withMargin)} and {@link #getPerimeterPath}
 * Though for some shapes a more specific implementation of the other methods might provide performance benefits
 */
public interface I2DShape
{
	/**
	 * checks if the point is inside the shape. If the point is exactly on the shape border, the result is undefined
	 * as with floating point values, no equality can be guarantied
	 * Use {@link #withMargin} to apply a margin to the shape
	 *
	 * @param point
	 * @return true if inside (borders included!!)
	 */
	boolean isPointInShape(IVector2 point);

	/**
	 * Create a new shape of the same type with an additional margin.
	 * Implementation depends on the actual shape.
	 *
	 * @param margin a positive or negative margin
	 * @return a new shape with an additional margin
	 */
	I2DShape withMargin(double margin);

	/**
	 * Create a list of IPath instances describing the perimeter of the shape
	 * The order is ensured such that each {@link IBoundedPath} ends where the next one starts
	 *
	 * @return the list of IPath instances
	 */
	List<IBoundedPath> getPerimeterPath();


	/**
	 * Calculate the length of the perimeter path
	 *
	 * @return the total length of the perimeter path
	 */
	default double getPerimeterLength()
	{
		return getPerimeterPath().stream()
				.mapToDouble(IBoundedPath::getLength)
				.sum();
	}

	/**
	 * Returns the nearest point outside a shape to a given point inside the shape.
	 * If the given point is outside the shape, return the point.
	 *
	 * @param point some point in- or outside
	 * @return the nearest point outside, if point is inside, else the point itself
	 */
	default IVector2 nearestPointOutside(IVector2 point)
	{
		if (!isPointInShape(point))
		{
			return point;
		} else
		{
			return nearestPointOnPerimeterPath(point);
		}
	}

	/**
	 * Returns the nearest point inside a shape to a given point outside the shape.
	 * If the given point is inside the shape, return the point.
	 *
	 * @param point some point in- or outside
	 * @return the nearest point inside, if point is outside, else the point itself
	 */
	default IVector2 nearestPointInside(IVector2 point)
	{
		if (isPointInShape(point))
		{
			return point;
		} else
		{
			return nearestPointOnPerimeterPath(point);
		}
	}

	default IVector2 nearestPointOnPerimeterPath(IVector2 point)
	{
		return point.nearestTo(getPerimeterPath().stream().map(path -> path.closestPointOnPath(point)).toList());
	}

	/**
	 * Get the intersection points of the shape and the line
	 *
	 * @param line some unbounded line
	 * @return all intersection points
	 */
	default List<IVector2> intersectPerimeterPath(ILine line)
	{
		return getPerimeterPath().stream()
				.map(path -> path.intersect(line))
				.flatMap(IIntersections::stream)
				.filter(VectorDistinctStreamFilter.byIsCloseTo())
				.toList();
	}

	/**
	 * Get the intersection points of the shape and the half line
	 *
	 * @param halfLine some half line
	 * @return all intersection points
	 */
	default List<IVector2> intersectPerimeterPath(IHalfLine halfLine)
	{
		return getPerimeterPath().stream()
				.map(path -> path.intersect(halfLine))
				.flatMap(IIntersections::stream)
				.filter(VectorDistinctStreamFilter.byIsCloseTo())
				.toList();
	}

	/**
	 * Get the intersection points of the shape and the line segment
	 *
	 * @param segment some line segment
	 * @return all intersection points
	 */
	default List<IVector2> intersectPerimeterPath(ILineSegment segment)
	{
		return getPerimeterPath().stream()
				.map(path -> path.intersect(segment))
				.flatMap(IIntersections::stream)
				.filter(VectorDistinctStreamFilter.byIsCloseTo())
				.toList();
	}

	/**
	 * Get the intersection points of the shape and the circle
	 *
	 * @param circle some circle
	 * @return all intersection points
	 */
	default List<IVector2> intersectPerimeterPath(ICircle circle)
	{
		return getPerimeterPath().stream()
				.map(path -> path.intersect(circle))
				.flatMap(IIntersections::stream)
				.filter(VectorDistinctStreamFilter.byIsCloseTo())
				.toList();
	}

	/**
	 * Get the intersection points of the shape and the arc
	 *
	 * @param arc some arc
	 * @return all intersection points
	 */
	default List<IVector2> intersectPerimeterPath(IArc arc)
	{

		return getPerimeterPath().stream()
				.map(path -> path.intersect(arc))
				.flatMap(IIntersections::stream)
				.filter(VectorDistinctStreamFilter.byIsCloseTo())
				.toList();
	}


	/**
	 * Get the intersection points of the shape and the path
	 *
	 * @param path some path
	 * @return all intersection points
	 */
	default List<IVector2> intersectPerimeterPath(IPath path)
	{
		if (path instanceof ILine line)
		{
			return intersectPerimeterPath(line);
		} else if (path instanceof IHalfLine halfLine)
		{
			return intersectPerimeterPath(halfLine);
		} else if (path instanceof ILineSegment segment)
		{
			return intersectPerimeterPath(segment);
		} else if (path instanceof ICircle circle)
		{
			return intersectPerimeterPath(circle);
		} else if (path instanceof IArc arc)
		{
			return intersectPerimeterPath(arc);
		}
		throw new NotImplementedException();
	}

	/**
	 * Get the intersection points of the shape with another shape
	 *
	 * @param shape some other shape
	 * @return all intersection points
	 */
	default List<IVector2> intersectShape(I2DShape shape)
	{
		return getPerimeterPath().stream()
				.map(shape::intersectPerimeterPath)
				.flatMap(List::stream)
				.filter(VectorDistinctStreamFilter.byIsCloseTo())
				.toList();
	}


	/**
	 * Check if the shape is intersecting with given path
	 *
	 * @param path the path to check
	 * @return true, if there is an intersection
	 */
	default boolean isIntersectingWithPath(IPath path)
	{
		return !intersectPerimeterPath(path).isEmpty();
	}

	/**
	 * Create a {@link ShapeBoundary} object that can be used to sort positions along the boundary of the shape
	 *
	 * @return {@link ShapeBoundary} the shape boundary instance describing this shape
	 */
	default IShapeBoundary getShapeBoundary()
	{
		return new ShapeBoundary(this);
	}
}
