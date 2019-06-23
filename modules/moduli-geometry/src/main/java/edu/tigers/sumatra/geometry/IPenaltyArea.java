/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import java.util.List;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPenaltyArea extends I2DShape
{
	/**
	 * Perimeter of the front curve of penalty area
	 *
	 * @return the perimeter (Umfang) for the front curve
	 */
	double getPerimeterFrontCurve();
	
	
	/**
	 * @param length in [0,circumference]
	 * @return
	 */
	IVector2 stepAlongPenArea(double length);
	
	
	/**
	 * Check if the point is inside this penArea or behind
	 *
	 * @param point a point
	 * @return true, if inside or behind
	 */
	boolean isPointInShapeOrBehind(IVector2 point);
	
	
	/**
	 * Creates nearest Point outside of shape that is the closest to the current point
	 * Three possibilities:
	 * <ul>
	 * <li>If point outside penArea and inside field</li>
	 * <ul>
	 * <li>Same point is returned</li>
	 * </ul>
	 * <li>Else (point inside or behind penArea)</li>
	 * <ul>
	 * <li>Find intersections on front curve, using pointToBuildLine</li>
	 * <li>If intersections found, use closest</li>
	 * <li>Else fallback to {@link #nearestPointOutside(IVector2)}</li>
	 * </ul>
	 * </ul>
	 *
	 * @param point the point to check
	 * @param pointToBuildLine the point to use to move the point outside
	 * @return the nearest point outside the penalty area, guarantied to be inside field
	 */
	IVector2 nearestPointOutside(IVector2 point, IVector2 pointToBuildLine);
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Find the line intersections on the outer curve. The goal line is not considered.
	 * </p>
	 *
	 * @param line some line
	 * @return all intersections. This can be zero to two intersections.
	 */
	@Override
	List<IVector2> lineIntersections(final ILine line);
	
	
	/**
	 * @return the inner rectangle
	 */
	IRectangle getInnerRectangle();
	
	
	/**
	 * @return the center of the goal within the penalty area
	 */
	IVector2 getGoalCenter();
	
	
	/**
	 * @return the front-line of the penalty area from positive y to negative y
	 */
	ILineSegment getFrontLine();
	
	
	/**
	 * @return the radiusOfPenaltyArea
	 */
	double getRadius();
	
	
	/**
	 * @return the length of the full front line
	 */
	double getFrontLineLength();
	
	
	/**
	 * @return the half length of the front line
	 */
	double getFrontLineHalfLength();
	
	
	/**
	 * @return the arc on the negative (y) side
	 */
	IArc getArcNeg();
	
	
	/**
	 * @return the arc on the positive (y) side
	 */
	IArc getArcPos();
	
	
	/**
	 * Check if a point is behind the penalty area, considering the width of the penalty area
	 *
	 * @param point
	 * @return true if the point is behind the penalty area
	 */
	boolean isBehindPenaltyArea(final IVector2 point);


	/**
	 * @param point
	 * @return length of projected point on Penalty Area
	 */
	double lengthToPointOnPenArea(final IVector2 point);

	/**
	 * @param startPoint
	 * @param length
	 * @return position, which is <length> away form startPoint on Penalty Area
	 */
	IVector2 stepAlongPenArea(final IVector2 startPoint, final double length);


	/**
	 * @param point
	 * @return projected Point of <point>
	 */
	IVector2 projectPointOnPenaltyAreaLine(final IVector2 point);

	/**
	 * @return total Length of PenaltyArea
	 */
	double getLength();

	/**
	 * Create a new Penalty Area of the same type with an additional margin.
	 *
	 * @param margin a positiv or negativ margin
	 * @return a new shape with an additional margin
	 */
	@Override
	IPenaltyArea withMargin(double margin);
}
