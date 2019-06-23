/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.shapes;

import java.util.List;

import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;


/**
 * This is a Interface for all 2 dimensional geometric shapes,
 * such as Circles and Polygons.
 * 
 * @author Malte
 */
public interface I2DShape
{
	
	/**
	 * Calculates the area ("Flaecheninhalt") of this shape.
	 * 
	 * @return
	 */
	double getArea();
	
	
	/**
	 * @param point
	 * @return
	 */
	default boolean isPointInShape(final IVector2 point)
	{
		return isPointInShape(point, 0.0d);
	}
	
	
	/**
	 * @param point
	 * @param margin like the margin in css, the area around the shape with the thickness of this value
	 * @return
	 */
	boolean isPointInShape(IVector2 point, double margin);
	
	
	/**
	 * @param line
	 * @return
	 */
	boolean isLineIntersectingShape(ILine line);
	
	
	/**
	 * Returns the neares point outside a shape to a given point inside the shape.
	 * If the given point is outside the shape, return the point.
	 * 
	 * @param point
	 * @return
	 */
	IVector2 nearestPointOutside(IVector2 point);
	
	
	/**
	 * Get the intersection points of the shape and line
	 * 
	 * @param line
	 * @return
	 */
	List<IVector2> lineIntersections(ILine line);
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param start
	 * @param end
	 * @return
	 */
	default boolean isLineSegmentIntersectingShape(final IVector2 start, final IVector2 end)
	{
		List<IVector2> intersecs = lineIntersections(Line.newLine(start, end));
		Rectangle rect = new Rectangle(start, end);
		for (IVector2 inters : intersecs)
		{
			if (rect.isPointInShape(inters))
			{
				return true;
			}
		}
		return false;
	}
}
