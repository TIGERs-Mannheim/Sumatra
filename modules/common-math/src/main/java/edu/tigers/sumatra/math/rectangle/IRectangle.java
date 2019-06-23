/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.rectangle;

import java.util.List;
import java.util.Random;

import edu.tigers.sumatra.export.IJsonString;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Regular Rectangle interface.
 * All regular rectangle sides are either parallel to the X- or the Y-Axis.
 * Therefore, it is impossible to create skewed rectangles.
 *
 * @author Malte
 */
public interface IRectangle extends I2DShape, IJsonString
{
	/**
	 * Absolute value of the length of the side
	 * that is parallel to the Y-Axis.
	 * 
	 * @return
	 */
	double yExtent();
	
	
	/**
	 * Absolute value of the length of the side
	 * that is parallel to the X-Axis.
	 * 
	 * @return
	 */
	double xExtent();
	
	
	/**
	 * @return max x among all 4 corners
	 */
	double maxX();
	
	
	/**
	 * @return min x among all 4 corners
	 */
	double minX();
	
	
	/**
	 * @return max y among all 4 corners
	 */
	double maxY();
	
	
	/**
	 * @return min y among all 4 corners
	 */
	double minY();
	
	
	/**
	 * Generates a random point within the shape.
	 * 
	 * @param rnd
	 * @return
	 */
	IVector2 getRandomPointInShape(Random rnd);
	
	
	/**
	 * Center of the rectangle
	 * 
	 * @return
	 */
	IVector2 center();
	
	
	/**
	 * Get all four points of the rectangle.
	 * Starting at topLeft, going counter clockwise.
	 * 
	 * @return List of corner points.
	 */
	List<IVector2> getCorners();
	
	
	/**
	 * Get the rectangle edges in counter-clockwise order.
	 * Starting with the left edge.
	 * 
	 * @return List of all edges
	 */
	List<ILine> getEdges();
	
	
	/**
	 * Create a new rectangle with a given margin in each direction
	 * 
	 * @param margin a positive or negative margin
	 * @return a new rectangle
	 */
	@Override
	IRectangle withMargin(double margin);
	
	
	/**
	 * Create a new rectangle with a given margin in each direction
	 *
	 * @param xMargin a positive or negative margin for x direction
	 * @param yMargin a positive or negative margin for y direction
	 * @return a new rectangle
	 */
	IRectangle withMarginXy(double xMargin, double yMargin);
	
	
	/**
	 * @param point some point
	 * @param margin a positive or negative margin
	 * @return nearest point inside rectangle with margin
	 * @note rather use {@link IRectangle#withMargin(double)}
	 */
	IVector2 nearestPointInside(final IVector2 point, double margin);
	
	
	/**
	 * Get the nearest point inside the rectangle, if point is outside.<br>
	 * Use pointToBuildLine to move the point to the rectangle border
	 * 
	 * @param point the point to check and move
	 * @param pointToBuildLine a point to build a line for generating intersections on the rectangle border
	 * @return a point inside the rectangle
	 */
	IVector2 nearestPointInside(IVector2 point, IVector2 pointToBuildLine);
	
	
	/**
	 * @return a mirrored shape
	 */
	IRectangle mirror();
}
