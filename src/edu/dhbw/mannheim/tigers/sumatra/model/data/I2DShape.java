/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

/**
 * This is a Interface for all 2 dimensional geometric shapes, 
 * such as Circles and Polygons.
 * 
 * @author Malte
 * 
 */
public interface I2DShape
{

	/**
	 * Calculates the area ("Flächeninhalt") of this shape.
	 */
	public float getArea();

	public boolean isPointInShape(IVector2 point);
	public boolean isLineIntersectingShape(ILine line);
	

	/**
	 * Returns the neares point outside a shape to a given point inside the shape.
	 * If the given point is outside the shape, return the point.
	 */
	public IVector2 nearestPointOutside(IVector2 point);
}
