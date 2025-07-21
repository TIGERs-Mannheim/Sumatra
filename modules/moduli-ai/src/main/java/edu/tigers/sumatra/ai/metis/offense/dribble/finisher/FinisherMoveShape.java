/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble.finisher;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.penarea.EFinisherMoveShapeSection;
import edu.tigers.sumatra.math.quadrilateral.Quadrilateral;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class FinisherMoveShape
{
	private double x1;
	private double x2;
	private double width;
	private double radius;

	@Configurable(defValue = "600.0")
	private static double finisherMoveShapeCircleRadius = 600;

	@Configurable(defValue = "200.0")
	private static double samplingStepDistanceOnFinisherMoveShape = 200;

	@Getter
	private ILineSegment lineLeft;
	@Getter
	private ILineSegment lineBottom;
	@Getter
	private ILineSegment lineRight;
	@Getter
	private IArc leftArc;
	@Getter
	private IArc rightArc;


	private FinisherMoveShape(double x1, double x2, double width, double radius)
	{
		this.x1 = x1;
		this.x2 = x2;
		this.width = width;
		this.radius = radius;
		IVector2 topLeftCorner = Vector2.fromXY(x2, width);
		IVector2 bottomLeftCorner = Vector2.fromXY(x1, width);
		IVector2 bottomRightCorner = Vector2.fromXY(x1, -width);
		IVector2 topRightCorner = Vector2.fromXY(x2, -width);

		lineLeft = Lines.segmentFromPoints(topLeftCorner, bottomLeftCorner.addNew(Vector2.fromX(radius)));
		lineBottom = Lines.segmentFromPoints(
				bottomLeftCorner.subtractNew(Vector2.fromY(radius)),
				bottomRightCorner.addNew(Vector2.fromY(radius))
		);
		lineRight = Lines.segmentFromPoints(bottomRightCorner.addNew(Vector2.fromX(radius)), topRightCorner);

		var leftCircleCenter = bottomLeftCorner.addNew(Vector2.fromXY(radius, -radius));
		leftArc = Arc.createArc(leftCircleCenter, radius, Vector2.fromX(-1).getAngle(), -AngleMath.PI_HALF);

		var rightCircleCenter = bottomRightCorner.addNew(Vector2.fromXY(radius, radius));
		rightArc = Arc.createArc(rightCircleCenter, radius, Vector2.fromX(-1).getAngle(), AngleMath.PI_HALF);
	}


	public FinisherMoveShape withMargin(double margin)
	{
		return new FinisherMoveShape(x1 - margin, x2, width + margin, radius);
	}


	public double getMaxLength()
	{
		double lengthSide = lineLeft.getLength();
		double lengthOfArc = (0.5 * leftArc.radius() * Math.PI);
		double lengthBottom = lineBottom.getLength();
		return lengthSide * 2 + lengthOfArc * 2 + lengthBottom;
	}


	public double getStepPositionOnShape(IVector2 pos)
	{
		double lengthSide = lineLeft.getLength();
		double lengthOfArc = (0.5 * leftArc.radius() * Math.PI);
		double lengthBottom = lineBottom.getLength();

		double distLineLeft = lineLeft.distanceTo(pos);
		double distLineRight = lineRight.distanceTo(pos);
		double distLineBottom = lineBottom.distanceTo(pos);

		double distArcLeft = CircleMath.nearestPointOnArcLine(leftArc, pos).distanceTo(pos);
		double distArcRight = CircleMath.nearestPointOnArcLine(rightArc, pos).distanceTo(pos);

		if (distLineLeft < distArcLeft
				&& distLineLeft < distLineBottom
				&& distLineLeft < distArcRight
				&& distLineLeft < distLineRight)
		{
			return lineLeft.closestPointOnPath(pos).distanceTo(lineLeft.getPathStart());
		}

		if (distArcLeft < distLineLeft
				&& distArcLeft < distLineBottom
				&& distArcLeft < distArcRight
				&& distArcLeft < distLineRight)
		{
			IVector2 pointOnArc = CircleMath.nearestPointOutsideArc(leftArc, pos);
			IVector2 pointOnArcToCenter = leftArc.center().subtractNew(pointOnArc);

			IVector2 arcStart = CircleMath.stepAlongCircle(
					lineLeft.getPathEnd(),
					leftArc.center(),
					0
			);
			IVector2 arcStartToCenter = leftArc.center().subtractNew(arcStart);
			double angle = pointOnArcToCenter.angleToAbs(arcStartToCenter).orElse(0.0);

			double distanceOnArc = 2 * Math.PI * leftArc.radius() * (AngleMath.rad2deg(angle) / 360.0);
			return lengthSide + distanceOnArc;
		}

		if (distLineBottom < distArcLeft
				&& distLineBottom < distLineRight
				&& distLineBottom < distArcRight
				&& distLineBottom < distLineLeft)
		{
			return lengthSide + lengthOfArc + lineBottom.closestPointOnPath(pos).distanceTo(lineBottom.getPathStart());
		}

		if (distArcRight < distLineLeft
				&& distArcRight < distLineBottom
				&& distArcRight < distArcLeft
				&& distArcRight < distLineRight)
		{
			IVector2 pointOnArc = CircleMath.nearestPointOutsideArc(rightArc, pos);
			IVector2 pointOnArcToCenter = rightArc.center().subtractNew(pointOnArc);

			IVector2 arcStart = CircleMath.stepAlongCircle(
					lineRight.getPathStart(),
					rightArc.center(),
					0
			);
			IVector2 arcStartToCenter = rightArc.center().subtractNew(arcStart);
			double angle = pointOnArcToCenter.angleToAbs(arcStartToCenter).orElse(0.0);

			double distanceOnArc = lengthOfArc - 2 * Math.PI * rightArc.radius() * (AngleMath.rad2deg(angle) / 360.0);
			return lengthSide + distanceOnArc + lengthBottom + lengthOfArc;
		}

		if (distLineRight < distArcLeft
				&& distLineRight < distLineBottom
				&& distLineRight < distArcRight
				&& distLineRight < distLineLeft)
		{
			return lengthSide + lengthBottom + lengthOfArc * 2 +
					lineRight.closestPointOnPath(pos).distanceTo(lineRight.getPathStart());
		}

		return 0;
	}


	public IRectangle getBoundingRectangle()
	{
		return Rectangle.fromPoints(
				lineLeft.getPathStart(),
				lineRight.getPathStart().addNew(Vector2.fromX(-leftArc.radius()))
		);
	}


	public IVector2 stepOnShape(double step)
	{
		double lengthSide = lineLeft.getLength();
		double lengthOfArc = (0.5 * leftArc.radius() * Math.PI);
		double lengthBottom = lineBottom.getLength();

		IVector2 position;
		position = switch (getSection(step))
		{
			case EFinisherMoveShapeSection.LEFT_SIDE -> lineLeft.stepAlongPath(Math.max(0, step));
			case LEFT_ARC -> CircleMath.stepAlongCircle(
					lineLeft.getPathEnd(),
					leftArc.center(),
					SumatraMath.relative(step - lengthSide, 0, lengthOfArc) * AngleMath.PI_HALF
			);
			case BOTTOM_LINE -> lineBottom.stepAlongPath(step - lengthSide - lengthOfArc);
			case RIGHT_ARC -> CircleMath.stepAlongCircle(
					lineBottom.getPathEnd(),
					rightArc.center(),
					SumatraMath.relative(step - lengthSide - lengthOfArc - lengthBottom, 0, lengthOfArc)
							* AngleMath.PI_HALF
			);
			case RIGHT_SIDE -> lineRight.stepAlongPath(step - lengthSide - lengthOfArc - lengthBottom - lengthOfArc);
		};
		return position;
	}


	private EFinisherMoveShapeSection getSection(double pos)
	{
		double lengthSide = lineLeft.getLength();
		double lengthOfArc = (0.5 * leftArc.radius() * Math.PI);
		double lengthBottom = lineBottom.getLength();

		if (pos < 0)
		{
			return EFinisherMoveShapeSection.LEFT_SIDE;
		}
		if (pos < lengthSide)
		{
			return EFinisherMoveShapeSection.LEFT_SIDE;
		} else if (pos < lengthSide + lengthOfArc)
		{
			return EFinisherMoveShapeSection.LEFT_ARC;
		} else if (pos < lengthSide + lengthOfArc + lengthBottom)
		{
			return EFinisherMoveShapeSection.BOTTOM_LINE;
		} else if (pos < lengthSide + lengthOfArc + lengthBottom + lengthOfArc)
		{
			return EFinisherMoveShapeSection.RIGHT_ARC;
		}
		return EFinisherMoveShapeSection.RIGHT_SIDE;
	}


	public boolean isPointInShape(IVector2 point)
	{
		if (!getBoundingRectangle().isPointInShape(point))
		{
			// optimization for most cases
			return false;
		}

		return CircleMath.isPointInArc(getLeftArc(), point, 0) ||
				CircleMath.isPointInArc(getRightArc(), point, 0) ||
				Quadrilateral.fromCorners(
						lineRight.getPathStart(), lineBottom.getPathEnd(), lineBottom.getPathStart(),
						lineLeft.getPathEnd()
				).isPointInShape(point) ||
				Quadrilateral.fromCorners(
								lineLeft.getPathStart(), lineLeft.getPathEnd(), lineRight.getPathStart(),
								lineRight.getPathEnd()
						)
						.isPointInShape(point);
	}


	public List<IVector2> intersectCircle(ICircle circle)
	{
		var intersections = Stream.of(
				leftArc.intersect(circle).stream(),
				rightArc.intersect(circle).stream(),
				lineBottom.intersect(circle).stream(),
				lineLeft.intersect(circle).stream(),
				lineRight.intersect(circle).stream()
		).flatMap(e -> e).toList();
		List<IVector2> filteredIntersections = new ArrayList<>();
		for (var intersection : intersections)
		{
			if (filteredIntersections.stream().filter(e -> e.distanceTo(intersection) < 1e-3).findAny().isEmpty())
			{
				filteredIntersections.add(intersection);
			}
		}
		return filteredIntersections;
	}


	public static FinisherMoveShape generateFinisherMoveMovementShape(IVector2 lineBase)
	{
		double yMin = determineYMin(lineBase);
		double y = Math.min(yMin + 1000, Math.max(Math.abs(lineBase.y()), yMin));

		double xMax = determineXMax(lineBase);
		double x = Math.max(xMax - 1900, Math.min(lineBase.x(), xMax));

		var finisherMoveShapeCandidate = new FinisherMoveShape(
				x, Geometry.getFieldLength() / 2.0, y,
				finisherMoveShapeCircleRadius
		);

		return redefineShapeToFitLineBaseOnCornerPoints(
				lineBase, yMin, y, xMax, x, Geometry.getFieldLength(), finisherMoveShapeCandidate);
	}


	private static FinisherMoveShape redefineShapeToFitLineBaseOnCornerPoints(
			IVector2 lineBase, double yMin, double y,
			double xMax, double x, double fieldLength,
			FinisherMoveShape finisherMoveShapeCandidate
	)
	{
		var boundingRectangle = finisherMoveShapeCandidate.getBoundingRectangle();
		double distToBounding = finisherMoveShapeCandidate.stepOnShape(
						finisherMoveShapeCandidate.getStepPositionOnShape(lineBase))
				.distanceTo(boundingRectangle.nearestPointOutside(lineBase));
		if (distToBounding > 1e-5)
		{
			// modify Finisher Shape to fit corner circles with lineBase pos
			IVector2 basePoint = finisherMoveShapeCandidate.stepOnShape(
					finisherMoveShapeCandidate.getStepPositionOnShape(lineBase));
			var baseToBall = lineBase.subtractNew(basePoint);
			double offsetX = baseToBall.x();
			double offsetY = baseToBall.y();
			y += Math.abs(offsetY);
			x -= Math.abs(offsetX);
			y = Math.min(yMin + 1000, Math.max(y, yMin));
			x = Math.max(xMax - 1900, Math.min(x, xMax));
			finisherMoveShapeCandidate = new FinisherMoveShape(
					x, fieldLength / 2.0, y,
					finisherMoveShapeCircleRadius
			);
		}
		return finisherMoveShapeCandidate;
	}


	private static double determineXMax(IVector2 lineBase)
	{
		double xMax = Geometry.getFieldLength() / 2.0 - Geometry.getPenaltyAreaTheir().getRectangle().xExtent()
				- Geometry.getBotRadius() * 3 + Geometry.getBallRadius();
		if (lineBase.x() > Geometry.getFieldLength() / 2.0 - Geometry.getPenaltyAreaTheir().getRectangle().xExtent())
		{
			xMax = xMax - (1 - SumatraMath.relative(
					Math.max(0, Math.abs(lineBase.y()) - Geometry.getPenaltyAreaTheir().getRectangle().yExtent() / 2.0), 0,
					400
			)) * 500;
		}
		return xMax;
	}


	private static double determineYMin(IVector2 lineBase)
	{
		double yMin = Geometry.getPenaltyAreaTheir().getRectangle().yExtent() / 2.0 + Geometry.getBotRadius() * 2;
		if (Math.abs(lineBase.y()) < Geometry.getPenaltyAreaTheir().getRectangle().yExtent() / 2.0)
		{
			yMin = yMin + (1 - SumatraMath.relative(
					Geometry.getFieldLength() / 2.0 - Geometry.getPenaltyAreaTheir().getRectangle().xExtent() - lineBase.x(),
					0, 500
			)) * 500;
		}
		return yMin;
	}


	public List<IVector2> sampleAlongFinisherMoveShape(ICircle dribblingCircle)
	{
		List<IVector2> samples = new ArrayList<>();
		for (double i = 0; i < this.getMaxLength(); i += samplingStepDistanceOnFinisherMoveShape)
		{
			var sample = this.stepOnShape(i);
			if (dribblingCircle.isPointInShape(sample))
			{
				samples.add(sample);
			}
		}
		return samples;
	}
}
