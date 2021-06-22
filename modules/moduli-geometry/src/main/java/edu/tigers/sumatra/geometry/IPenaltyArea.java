/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPenaltyArea extends I2DShape
{
	/**
	 * Check if the point is inside this penArea or behind
	 *
	 * @param point a point
	 * @return true, if inside or behind
	 */
	boolean isPointInShapeOrBehind(IVector2 point);


	/**
	 * <p>
	 * Find the line intersections on the outer curve. The goal line is not considered.
	 * </p>
	 *
	 * @param line some unbounded line
	 * @return all intersections. This can be zero to two intersections.
	 */
	List<IVector2> lineIntersections(edu.tigers.sumatra.math.line.v2.ILine line);


	/**
	 * <p>
	 * Find the line intersections on the outer curve. The goal line is not considered.
	 * </p>
	 *
	 * @param line some line segment
	 * @return all intersections. This can be zero to two intersections.
	 */
	List<IVector2> lineIntersections(ILineSegment line);


	/**
	 * <p>
	 * Find the line intersections on the outer curve. The goal line is not considered.
	 * </p>
	 *
	 * @param line some half line
	 * @return all intersections. This can be zero to two intersections.
	 */
	List<IVector2> lineIntersections(IHalfLine line);


	/**
	 * {@inheritDoc}
	 * <p>
	 * Find the line intersections on the outer curve. The goal line is not considered.
	 * </p>
	 *
	 * @param line some legacy line (treated as a segment)
	 * @return all intersections. This can be zero to two intersections.
	 */
	@Override
	List<IVector2> lineIntersections(final ILine line);


	/**
	 * @return the center of the goal within the penalty area
	 */
	IVector2 getGoalCenter();


	/**
	 * @return the rectangle of the penaltyArea
	 */
	IRectangle getRectangle();


	/**
	 * @return Position of the front corner with negative y (looking towards opponent goal)
	 */
	IVector2 getNegCorner();


	/**
	 * @return Position of the front corner with positive y (looking towards opponent goal)
	 */
	IVector2 getPosCorner();


	/**
	 * Check if a point is behind the penalty area, considering the width of the penalty area
	 *
	 * @param point
	 * @return true if the point is behind the penalty area
	 */
	boolean isBehindPenaltyArea(final IVector2 point);


	/**
	 * Create a new Penalty Area of the same type with an additional margin.
	 *
	 * @param margin a positive or negative margin
	 * @return a new shape with an additional margin
	 */
	@Override
	IPenaltyArea withMargin(double margin);


	/**
	 * Projects a point on the penalty area using the line from the given point to the goal center
	 *
	 * @param point the point to project
	 * @return the projected point
	 */
	IVector2 projectPointOnToPenaltyAreaBorder(IVector2 point);


	/**
	 * @return the drawable shapes to draw this penalty area
	 */
	List<IDrawableShape> getDrawableShapes();


	/**
	 * Distance from penalty area border to point.
	 * If point is inside, the distance is always zero.
	 *
	 * @param point some point
	 * @return the distance to this point
	 */
	double distanceTo(final IVector2 point);


	/**
	 * Calculates the distance between the supplied {@code pos} and the edge of the penalty area.
	 * If the point does not lie inside the penalty area, a distance of 0 is returned.
	 *
	 * @param pos
	 * @return
	 */
	double distanceToNearestPointOutside(final IVector2 pos);


	/**
	 * Calculate the area that is cut off with the given points.
	 * From and to build a line segment. The area of the penalty area facing outwards is returned.
	 * If the line segment crosses the goal line, the area will be 0.
	 *
	 * @param from first point
	 * @param to second point
	 * @return the area in m^2
	 */
	default double intersectionArea(final IVector2 from, final IVector2 to)
	{
		throw new UnsupportedOperationException();
	}
}
