/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.04.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.I2DShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;


/**
 * Regular Rectangle interface.
 * All regular rectangles sides are either parallel to the X- or the Y-Axis.
 * Therefore, it is impossible to create skewed rectangles.
 * Every rectangle is defined by 2 points which are opposite vertices.
 * 
 * <pre>
 * topLeft                  topRight
 *       -------------------
 *       |     xExtend     |
 *       |                 | yExtend
 *       |                 |
 *       -------------------
 * bottomLeft             bottomRight
 * </pre>
 * 
 * @author Malte
 */
public interface IRectangle extends I2DShape
{
	/**
	 * Absolute value of the length of the side
	 * that is parallel to the Y-Axis.
	 * 
	 * @return
	 */
	float yExtend();
	
	
	/**
	 * Absolute value of the length of the side
	 * that is parallel to the X-Axis.
	 * 
	 * @return
	 */
	float xExtend();
	
	
	/**
	 * Corner which is in the top-left regarding to a cartesian coordinate system.
	 * 
	 * @return
	 */
	IVector2 topLeft();
	
	
	/**
	 * Corner which is in the top-right regarding to a cartesian coordinate system.
	 * 
	 * @return
	 */
	IVector2 topRight();
	
	
	/**
	 * Corner which is in the bottom-left regarding to a cartesian coordinate system.
	 * 
	 * @return
	 */
	IVector2 bottomLeft();
	
	
	/**
	 * Corner which is in the bottom-right regarding to a cartesian coordinate system.
	 * 
	 * @return
	 */
	IVector2 bottomRight();
	
	
	/**
	 * Generates a random point within the shape.
	 * 
	 * @return
	 */
	IVector2 getRandomPointInShape();
	
	
	/**
	 * Center of the rectangle
	 * 
	 * @return
	 */
	IVector2 getMidPoint();
	
	
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
	List<Line> getEdges();
	
	
	/**
	 * Get intersection points of a line with this rectangle.
	 * Only the the points in the direction of the line direction vector are returned.
	 * 
	 * @param line Line of interest.
	 * @return Points of intersection. List is empty if no intersections exist.
	 * @throws MathException Thrown if the line is equal to an edge.
	 */
	List<IVector2> getIntersectionPoints(ILine line) throws MathException;
	
	
	/**
	 * Get the distant intersection point of a line with this rectangle.
	 * 
	 * @param line Line of interest
	 * @return The distant intersection point
	 * @throws MathException Thrown if the line does not intersect or if the line is equal to an edge.
	 */
	IVector2 getDistantIntersectionPoint(ILine line) throws MathException;
	
	
	/**
	 * Get the near intersection point of a line with this rectangle.
	 * 
	 * @param line Line of interest
	 * @return The distant intersection point
	 * @throws MathException Thrown if the line does not intersect or if the line is equal to an edge.
	 */
	IVector2 getNearIntersectionPoint(ILine line) throws MathException;
}
