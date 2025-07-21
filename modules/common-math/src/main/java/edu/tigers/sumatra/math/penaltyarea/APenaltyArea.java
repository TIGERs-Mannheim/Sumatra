/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.penaltyarea;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;


public abstract class APenaltyArea implements IPenaltyArea
{
	@Getter
	protected final Rectangle rectangle;
	@Getter
	protected final IVector2 goalCenter;
	private final double length;
	private final double depth;


	/**
	 * Creates a PenaltyArea
	 *
	 * @param goalCenter
	 * @param length
	 * @param depth
	 */
	protected APenaltyArea(final IVector2 goalCenter, final double depth, final double length)
	{
		this.length = length;
		this.depth = depth;
		this.goalCenter = goalCenter;

		double centerOffset = Math.signum(goalCenter.x()) * depth / -2.;
		IVector2 center = Vector2.fromX(goalCenter.x() + centerOffset);
		rectangle = Rectangle.fromCenter(center, depth, length);
	}


	@Override
	public IPenaltyArea withRoundedCorners(double radius)
	{
		if (SumatraMath.isZero(radius))
		{
			return new PenaltyArea(goalCenter, depth, length);
		}
		return new PenaltyAreaRoundedCorners(goalCenter, depth, length, radius);
	}


	@Override
	@SuppressWarnings("squid:S1244") // equality check intended
	public IVector2 projectPointOnToPenaltyAreaBorder(IVector2 point)
	{
		if (point.x() * Math.signum(getGoalCenter().x()) >= Math.abs(getGoalCenter().x()))
		{
			if (point.y() == 0.0)
			{
				return getGoalCenter().addNew(Vector2.fromX(getDepth()));
			}
			return getGoalCenter().addNew(Vector2.fromY(Math.signum(point.y()) * getLength() / 2));
		}
		return point.nearestToOpt(intersectPerimeterPath(Lines.lineFromPoints(point, getGoalCenter())))
				.orElseGet(() -> getGoalCenter().addNew(Vector2.fromX(getDepth())));
	}


	protected double getLength()
	{
		return getRectangle().yExtent();
	}


	protected double getDepth()
	{
		return getRectangle().xExtent();
	}


	@Override
	public double distanceTo(IVector2 point)
	{
		return nearestPointInside(point).distanceTo(point);
	}


	@Override
	public double distanceToNearestPointOutside(IVector2 pos)
	{
		return nearestPointOutside(pos).distanceTo(pos);
	}


	@Override
	public IVector2 getNegCorner()
	{
		var corner = goalCenter.x() > 0 ? IRectangle.ECorner.BOTTOM_LEFT : IRectangle.ECorner.BOTTOM_RIGHT;
		return getRectangle().getCorner(corner);
	}


	@Override
	public IVector2 getPosCorner()
	{
		var corner = goalCenter.x() > 0 ? IRectangle.ECorner.TOP_LEFT : IRectangle.ECorner.TOP_RIGHT;
		return getRectangle().getCorner(corner);
	}


	@Override
	public boolean isBehindPenaltyArea(IVector2 point)
	{
		return (Math.abs(point.x()) > Math.abs(getGoalCenter().x()))
				&& ((int) Math.signum(point.x()) == (int) Math.signum(getGoalCenter().x()))
				&& Math.abs(point.y()) < getLength() / 2;
	}


	@Override
	public boolean isPointInShapeOrBehind(IVector2 point)
	{
		return isBehindPenaltyArea(point) || isPointInShape(point);
	}

}
