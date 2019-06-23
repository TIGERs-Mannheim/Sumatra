/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import java.util.Optional;

import edu.tigers.sumatra.math.IEuclideanDistance;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Line interface. A Line is defined by its support and direction vector.
 * The direction vector may define a line segment.
 * 
 * @author Malte
 */
public interface ILine extends IEuclideanDistance
{
	/**
	 * @return the support vector
	 */
	IVector2 supportVector();
	
	
	/**
	 * @return the direction vector
	 */
	IVector2 directionVector();
	
	
	/**
	 * @return support vector
	 */
	IVector2 getStart();
	
	
	/**
	 * @return support + direction vector
	 */
	IVector2 getEnd();
	
	
	/**
	 * @return the slope of this line, if line is not parallel to y-axis
	 */
	Optional<Double> getSlope();
	
	
	/**
	 * Returns the y-intercept of this Line.
	 * This is the y value where x == 0
	 * 
	 * @return the y-intercept of this line if
	 */
	Optional<Double> getYIntercept();
	
	
	/**
	 * Returns the y value to a given x input. <br>
	 * y = m * x + n<br>
	 * Value is not defined for vertical lines
	 * 
	 * @param x a value
	 * @return y value if line is not vertical
	 */
	Optional<Double> getYValue(double x);
	
	
	/**
	 * Returns the x value to a given y input. <br>
	 * x = (y - n) / m <br>
	 * Value is not defined for horizontal lines
	 * 
	 * @param y a value
	 * @return x value if line is not horizontal
	 */
	Optional<Double> getXValue(double y);
	
	
	/**
	 * Calculate the angle of the direction vector to the x-axis (just like IVector2#getAngle
	 *
	 * @return the angle, if direction vector is not zero
	 */
	Optional<Double> getAngle();
	
	
	/**
	 * Returns an orthogonal Line to the given line.
	 * Its direction vector is turned 90 degree clockwise and normalized.
	 * 
	 * @return an orthogonal line
	 */
	ILine getOrthogonalLine();
	
	
	/**
	 * @return
	 */
	boolean isHorizontal();
	
	
	/**
	 * @return
	 */
	boolean isVertical();
	
	
	/**
	 * Checks if a point lies in front of this line.<br>
	 * The point is considered lying in front, if it is
	 * within a 90 degree opening in each direction from
	 * the direction vector.
	 * 
	 * @param point Point to check
	 * @return True if the point is in front of this line
	 */
	boolean isPointInFront(IVector2 point);
	
	
	/**
	 * Check if the given point is on this line <b>segment</b>.
	 * The direction vector is considered as the segment.
	 *
	 * @param point some point
	 * @return true, if the given point is on this line segment
	 */
	boolean isPointOnLineSegment(final IVector2 point);
	
	
	/**
	 * Check if the given point is on this line <b>segment</b>.
	 * The direction vector is considered as the segment.
	 * 
	 * @param point some point
	 * @param margin some margin
	 * @return true, if the given point is on this line segment
	 */
	boolean isPointOnLineSegment(final IVector2 point, final double margin);
	
	
	/**
	 * @param line some line
	 * @return true, if given line is parallel to this line
	 */
	boolean isParallelTo(ILine line);
	
	
	/**
	 * Find intersection point of two continuous lines.
	 *
	 * @see ILine#intersectionOfSegments(ILine)
	 * @see ILine#isPointOnLineSegment(IVector2, double)
	 * @param line other line
	 * @return intersection point between both lines, if one exists.
	 */
	Optional<IVector2> intersectionWith(ILine line);
	
	
	/**
	 * Find intersection point of two line segments.<br>
	 * Intersection points must be on both line segments.<br>
	 *
	 * @see ILine#intersectionWith(ILine)
	 * @see ILine#isPointOnLineSegment(IVector2, double)
	 * @param line other line
	 * @return intersection point between both line segments, if one exists.
	 */
	Optional<IVector2> intersectionOfSegments(ILine line);
	
	
	/**
	 * Calculate the lead point with given point.
	 * 
	 * @param point some point
	 * @return the lead point on this line
	 */
	Vector2 leadPointOf(IVector2 point);
	
	
	/**
	 * Calculate the nearest point on this line, given a point.
	 *
	 * @param point some point
	 * @return the nearest point on the continuous line
	 */
	Vector2 nearestPointOnLine(final IVector2 point);
	
	
	/**
	 * This Method returns the nearest point on the line-segment to a given point.<br>
	 * If the lead point of the argument is not on the segment the
	 * nearest edge-point of the segment (start or end) is returned.
	 *
	 * @param point some point
	 * @return nearest point on this line segment
	 */
	Vector2 nearestPointOnLineSegment(final IVector2 point);
}
