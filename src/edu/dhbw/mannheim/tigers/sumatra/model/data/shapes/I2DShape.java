/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;


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
	float getArea();
	
	
	/**
	 * @param point
	 * @return
	 */
	boolean isPointInShape(IVector2 point);
	
	
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
}
