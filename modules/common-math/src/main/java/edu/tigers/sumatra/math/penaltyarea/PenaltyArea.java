/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.penaltyarea;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.util.List;


/**
 * Class representing a rectangular penalty area
 */
@Persistent
public class PenaltyArea extends APenaltyArea
{
	/**
	 * Used by berkely
	 */
	@SuppressWarnings("unused")
	private PenaltyArea()
	{
		super(Vector2f.ZERO_VECTOR, 1, 2);
	}


	/**
	 * Creates a PenaltyArea
	 *
	 * @param goalCenter
	 * @param length
	 * @param depth
	 */
	public PenaltyArea(IVector2 goalCenter, double depth, double length)
	{
		super(goalCenter, depth, length);
	}


	@Override
	public IPenaltyArea withMargin(double margin)
	{
		double newDepth = Math.max(0, getDepth() + margin);
		double newLength = Math.max(0, getLength() + margin * 2);
		return new PenaltyArea(getGoalCenter(), newDepth, newLength);
	}


	@Override
	public List<IBoundedPath> getPerimeterPath()
	{
		double lowerX = getGoalCenter().x();
		double upperX = lowerX - Math.signum(getGoalCenter().x()) * getDepth();
		double negY = -getLength() / 2;
		double posY = getLength() / 2;
		var p1 = Vector2.fromXY(lowerX, posY);
		var p2 = Vector2.fromXY(upperX, posY);
		var p3 = Vector2.fromXY(upperX, negY);
		var p4 = Vector2.fromXY(lowerX, negY);
		return List.of(
				Lines.segmentFromPoints(p1, p2),
				Lines.segmentFromPoints(p2, p3),
				Lines.segmentFromPoints(p3, p4)
		);
	}


	@Override
	public double getPerimeterLength()
	{
		return getLength() + 2 * getDepth();
	}


	@Override
	public boolean isPointInShape(IVector2 point)
	{
		return getRectangle().isPointInShape(point);
	}


	@Override
	public IVector2 nearestPointInside(IVector2 point)
	{
		return getRectangle().nearestPointInside(point);
	}


	@Override
	public IVector2 nearestPointOutside(IVector2 point)
	{
		if (isPointInShapeOrBehind(point))
		{
			return point.nearestTo(
					getPerimeterPath().stream()
							.map(e -> e.closestPointOnPath(point))
							.toList());
		}
		return point;
	}
}
