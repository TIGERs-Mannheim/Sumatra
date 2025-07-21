/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.penaltyarea;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.List;


public class PenaltyAreaRoundedCorners extends APenaltyArea
{
	private final double radius;

	private final IRectangle frontRectangle;
	private final IRectangle backRectangle;
	private final IArc negArc; // Negative y for PenAreaOur
	private final IArc posArc; // Positive y for PenAreaOur


	public PenaltyAreaRoundedCorners(IVector2 goalCenter, double depth, double length, double radius)
	{
		super(goalCenter, depth, length);
		this.radius = radius;

		double sign = Math.signum(getGoalCenter().x());


		double lowerX = getGoalCenter().x();
		double upperX = lowerX - sign * depth;
		double middleX = upperX + sign * radius;
		double outerY = getLength() / 2;
		double middleY = outerY - radius;

		frontRectangle = Rectangle.fromPoints(Vector2.fromXY(upperX, -middleY), Vector2.fromXY(middleX, middleY));
		backRectangle = Rectangle.fromPoints(Vector2.fromXY(lowerX, -outerY), Vector2.fromXY(middleX, outerY));


		posArc = Arc.createArc(Vector2.fromXY(middleX, -sign * middleY), radius,
				-sign * AngleMath.PI_HALF, -AngleMath.PI_HALF);
		negArc = Arc.createArc(Vector2.fromXY(middleX, sign * middleY), radius,
				AngleMath.PI_HALF + sign * AngleMath.PI_HALF, -AngleMath.PI_HALF);

	}


	@Override
	public boolean isPointInShape(IVector2 point)
	{
		if (!getRectangle().isPointInShape(point))
		{
			// Rectangle serves as a bounding box
			return false;
		}
		return frontRectangle.isPointInShape(point)
				|| backRectangle.isPointInShape(point)
				|| posArc.isPointInShape(point)
				|| negArc.isPointInShape(point);
	}


	@Override
	public List<IBoundedPath> getPerimeterPath()
	{

		double sign = Math.signum(getGoalCenter().x());
		double lowerX = getGoalCenter().x();
		double upperX = lowerX - sign * getDepth();
		double middleX = upperX + sign * radius;
		double outerY = getLength() / 2;
		double middleY = outerY - radius;

		return List.of(
				Lines.segmentFromPoints(Vector2.fromXY(lowerX, -sign * outerY), Vector2.fromXY(middleX, -sign * outerY)),
				posArc,
				Lines.segmentFromPoints(Vector2.fromXY(upperX, -sign * middleY), Vector2.fromXY(upperX, sign * middleY)),
				negArc,
				Lines.segmentFromPoints(Vector2.fromXY(middleX, sign * outerY), Vector2.fromXY(lowerX, sign * outerY))
		);
	}


	@Override
	public double getPerimeterLength()
	{
		return 2 * posArc.getLength() + 2 * getDepth() + getLength() - 4 * radius;
	}


	@Override
	public IPenaltyArea withMargin(double margin)
	{
		double newDepth = Math.max(0, getDepth() + margin);
		double newLength = Math.max(0, getLength() + margin * 2);
		return new PenaltyAreaRoundedCorners(getGoalCenter(), newDepth, newLength, margin);
	}
}
