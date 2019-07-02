/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Class representing a penalty area
 * The PenaltyArea is built out of one rectangle in the middle, with two quarter circles on the top and the
 * bottom half
 * A little sketch with the given points
 *
 * <pre>
 *         circlePointUpperNeg   penalty mark   circlePointUpperPos
 *                    v             v             v
 * |------------------+-------------+-------------+------------------|
 * |outer Rectangle ..|[-------front Line--------]|..                |
 * |           ....:  |                           |  :....           |
 * |         .:       |                           |       :.         |
 * |       .:         |                           |         :.       |
 * |      :           |                           |           :      |
 * |     :            |   centre Rectangle        |            :     |
 * |    :             |                           |             :    |
 * |   :              |                           |              :   |
 * |  :   neg-circle  |                           | pos-circle    :  |
 * | :                |                           |                : |
 * |:                 |                           |                 :|
 * +------------------+-------------+-------------+------------------+
 * ^                  ^             ^             ^                  ^
 * |           circleCentreNeg  goal centre  circleCentrePos    circlePointLowerPos
 * circlePointLowerNeg
 * </pre>
 *
 * @author Malte, Frieder, KaiE
 */
@Persistent
public class LegacyPenArea implements IPenaltyArea
{
	private final double radius;
	private final double frontLineLength;
	private final double xSignum;
	
	private final IArc arcNeg;
	private final IArc arcPos;
	private final ILineSegment frontLine;
	
	
	@SuppressWarnings("unused") // used by berkeley
	private LegacyPenArea()
	{
		radius = 0;
		frontLineLength = 0;
		xSignum = 0;
		arcNeg = null;
		arcPos = null;
		frontLine = null;
	}
	
	
	/**
	 * @param radius of the quarter circles
	 * @param frontLineLength length of the front line
	 */
	public LegacyPenArea(final double radius, final double frontLineLength, final double xSignum)
	{
		this.radius = radius;
		this.frontLineLength = frontLineLength;
		this.xSignum = xSignum;
		
		double lowerX = xSignum * Geometry.getFieldLength() / 2;
		final double offsetFromCentre = 0.5 * frontLineLength;
		IVector2 circleCentrePos = Vector2f.fromXY(lowerX, offsetFromCentre);
		IVector2 circleCentreNeg = Vector2f.fromXY(lowerX, -offsetFromCentre);
		IVector2 circlePointUpperNeg = Vector2f.fromXY(circleCentreNeg.x() - xSignum * radius, circleCentreNeg.y());
		IVector2 circlePointUpperPos = Vector2f.fromXY(circleCentrePos.x() - xSignum * radius, circleCentrePos.y());
		
		frontLine = Lines.segmentFromPoints(circlePointUpperPos, circlePointUpperNeg);
		arcNeg = getPenAreaArc(circleCentreNeg, radius);
		arcPos = getPenAreaArc(circleCentrePos, radius);
	}
	
	
	@Override
	public LegacyPenArea withMargin(final double margin)
	{
		return new LegacyPenArea(Math.max(0, radius + margin), frontLineLength, xSignum);
	}
	
	
	public IVector2 stepAlongPenArea(final double length)
	{
		final double quarterCircleLength = AngleMath.PI_HALF * getRadius();
		if ((0 <= length) && (length <= quarterCircleLength))
		{
			return getPointOnCircle(arcPos.center(), getRadius(),
					(length / getRadius()) + (AngleMath.PI_HALF));
		} else if (length < (quarterCircleLength + frontLineLength))
		{
			return LineMath.stepAlongLine(frontLine.getStart(), frontLine.getEnd(), length - quarterCircleLength);
		} else if (length <= ((quarterCircleLength * 2) + frontLineLength))
		{
			return getPointOnCircle(arcNeg.center(), getRadius(),
					((length - quarterCircleLength - frontLineLength) / getRadius()) + (AngleMath.PI));
		} else
		{
			throw new IllegalArgumentException("Tried to step too long along penalty area: " + length);
		}
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return getInnerRectangle().isPointInShape(point) ||
				arcPos.isPointInShape(point) ||
				arcNeg.isPointInShape(point);
	}
	
	
	/**
	 * Projects a Point on PenaltyArea
	 *
	 * @param point that should be projected
	 * @return the projected Point
	 */
	
	public IVector2 projectPointOnPenaltyAreaLine(final IVector2 point)
	{
		IVector2 refPointOnGoalLine;
		// check whether point is in front of front line or in front of quarter circles
		if ((point.y() <= frontLine.getStart().y()) && (point.y() >= frontLine.getEnd().y()))
		{
			// front line, project straight on goal line
			refPointOnGoalLine = Vector2.fromXY(Geometry.getGoalOur().getCenter().x(), point.y());
		} else if (point.y() <= frontLine.getEnd().y())
		{
			refPointOnGoalLine = arcNeg.center();
		} else
		{
			refPointOnGoalLine = arcPos.center();
		}
		// build a line from point to goal and check where it intersects the penalty area line
		Line pointToGoal = Line.fromPoints(point, refPointOnGoalLine);
		// duplicate line in the other direction in case the point is inside penalty area
		IVector2 pFrom = pointToGoal.getStart().subtractNew(pointToGoal.directionVector());
		pointToGoal = Line.fromPoints(pFrom, refPointOnGoalLine);
		List<IVector2> penaltyAreaIntersections = lineIntersections(pointToGoal);
		if (penaltyAreaIntersections.isEmpty())
		{
			// point is probably exactly on goal line
			final double goalLineXVal = Geometry.getGoalOur().getCenter().x();
			final double epsilon = 1e-6;
			if ((point.x() > (goalLineXVal - epsilon)) && (point.x() < (goalLineXVal + epsilon)))
			{
				if (point.y() < 0)
				{
					return arcNeg.center().addNew(Vector2.fromY(-getRadius()));
				}
				
				return arcPos.center().addNew(Vector2.fromY(getRadius()));
			}
			
			return null;
		}
		return penaltyAreaIntersections.get(0);
	}
	
	
	/**
	 * @param point
	 * @return
	 */
	
	public double lengthToPointOnPenArea(final IVector2 point)
	{
		// step 1: get the penalty area line length to startPoint
		double startPointLengthOnPenArea;
		final double quarterCircLength = AngleMath.PI_HALF * radius;
		
		IVector2 intersectionPoint = projectPointOnPenaltyAreaLine(point);
		if (intersectionPoint == null)
		{
			throw new UnsupportedOperationException(
					"Line to goal does not intersect penalty area line - start point is probably behind goal");
		}
		
		// positive circle
		if (frontLine.getStart().y() < intersectionPoint.y())
		{
			final double alpha = SumatraMath.acos((intersectionPoint.y() - frontLine.getStart().y()) / getRadius());
			startPointLengthOnPenArea = Math.abs(alpha * getRadius());
		}
		// front line
		else if (frontLine.getEnd().y() < intersectionPoint.y())
		{
			final double additionalLengthOnFrontLine = Math.abs(intersectionPoint.y() - frontLine.getStart().y());
			startPointLengthOnPenArea = quarterCircLength + additionalLengthOnFrontLine;
		}
		// negative circle
		else
		{
			final double alpha = SumatraMath.asin((frontLine.getEnd().y() - intersectionPoint.y()) / getRadius());
			final double secondCircleSectorLength = Math.abs(alpha * getRadius());
			startPointLengthOnPenArea = quarterCircLength + frontLineLength + secondCircleSectorLength;
		}
		// avoid tried to step to long along penalty area exception
		if (startPointLengthOnPenArea < 0)
		{
			return 0;
		}
		if (startPointLengthOnPenArea > getLength())
			return getLength();
		
		return startPointLengthOnPenArea;
	}
	
	
	/**
	 * @param startPoint
	 * @param length
	 * @return the point that is <length> away from <startPoint> when stepping on penalty area line
	 */
	
	public IVector2 stepAlongPenArea(final IVector2 startPoint, final double length)
	{
		double lengthToStartPoint = lengthToPointOnPenArea(startPoint);
		return stepAlongPenArea(lengthToStartPoint + length);
	}
	
	
	/**
	 * @return total Length of PenaltyArea
	 */
	
	public double getLength()
	{
		return (radius * Math.PI) + frontLineLength;
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return withMargin(margin).isPointInShape(point);
	}
	
	
	@Override
	public boolean isPointInShapeOrBehind(final IVector2 point)
	{
		return isBehindPenaltyArea(point) || isPointInShape(point);
	}
	
	
	@Override
	public IVector2 projectPointOnToPenaltyAreaBorder(final IVector2 point)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final edu.tigers.sumatra.math.line.v2.ILine line)
	{
		return lineIntersections(line.toLegacyLine());
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILineSegment line)
	{
		List<IVector2> allIntersections = lineIntersections(line.toLegacyLine());
		allIntersections.removeIf(p -> !line.isPointOnLine(p));
		return allIntersections;
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final IHalfLine line)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector2 nearestPointInside(final IVector2 point)
	{
		if (isPointInShape(point))
		{
			return point;
		}
		return nearestPointOutside(Vector2.fromXY(arcNeg.center().x(), 0.0), point);
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		if (isBehindPenaltyArea(point))
		{
			return getPointOutsideForPointBehindGoalLine(point);
		}
		
		if (getInnerRectangle().isPointInShape(point))
		{
			return Vector2f.fromXY(frontLine.getStart().x(), point.y());
		}
		
		IArc arc = point.y() > 0 ? arcPos : arcNeg;
		if (arc.isPointInShape(point))
		{
			return LineMath.stepAlongLine(arc.center(), point, getRadius());
		}
		return point;
	}
	
	
	public IVector2 nearestPointOutside(final IVector2 point, final IVector2 pointToBuildLine)
	{
		if (!isPointInShapeOrBehind(point))
		{
			return point;
		}
		if (isBehindGoalLine(pointToBuildLine))
		{
			return getPointOutsideForPointBehindGoalLine(pointToBuildLine);
		}
		
		return point.nearestToOpt(lineIntersections(Line.fromPoints(point, pointToBuildLine)))
				.orElseGet(() -> nearestPointOutside(point));
	}
	
	
	@Override
	public boolean isIntersectingWithLine(final ILine line)
	{
		return !lineIntersections(line).isEmpty();
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		List<IVector2> result = new ArrayList<>();
		
		frontLine.intersectLine(Lines.lineFromPoints(line.getStart(), line.getEnd())).ifPresent(result::add);
		result.addAll(arcNeg.lineIntersections(line));
		result.addAll(arcPos.lineIntersections(line));
		
		return result;
	}
	
	
	private IVector2 getPointOutsideForPointBehindGoalLine(final IVector2 pointToBuildLine)
	{
		if (Math.abs(pointToBuildLine.y()) < getFrontLineHalfLength())
		{
			return Vector2f.fromXY(frontLine.getStart().x(), pointToBuildLine.y());
		}
		return Vector2.fromXY(arcNeg.center().x(), Math.signum(pointToBuildLine.y()) * getMaxAbsY());
	}
	
	
	private IArc getPenAreaArc(final IVector2 center, final double radius)
	{
		double startAngle = Vector2f.X_AXIS.multiplyNew(-center.x()).getAngle();
		double stopAngle = Vector2f.Y_AXIS.multiplyNew(center.y()).getAngle();
		double rotation = AngleMath.difference(stopAngle, startAngle);
		return Arc.createArc(center, radius, startAngle, rotation);
	}
	
	
	private IVector2 getPointOnCircle(final IVector2 origin, final double radius, final double angle)
	{
		return Vector2f.fromXY(origin.x() - (radius * SumatraMath.cos(angle)),
				origin.y() + (radius * SumatraMath.sin(angle)));
	}
	
	
	@Override
	public boolean isBehindPenaltyArea(final IVector2 point)
	{
		return isBehindGoalLine(point) &&
				(Math.abs(point.y()) <= getMaxAbsY());
	}
	
	
	private boolean isBehindGoalLine(final IVector2 point)
	{
		if (xSignum > 0)
		{
			return point.x() >= arcNeg.center().x();
		}
		return point.x() <= arcNeg.center().x();
	}
	
	
	/**
	 * @return the maximum absolute Y-value of this penalty area
	 */
	private double getMaxAbsY()
	{
		return getFrontLineHalfLength() + getRadius();
	}
	
	
	private IRectangle getInnerRectangle()
	{
		return Rectangle.fromPoints(frontLine.getStart(), arcNeg.center());
	}
	
	
	@Override
	public IVector2 getGoalCenter()
	{
		return Vector2.fromXY(arcNeg.center().x(), 0.0);
	}
	
	
	@Override
	public IRectangle getRectangle()
	{
		return getInnerRectangle();
	}
	
	
	@Override
	public IVector2 getNegCorner()
	{
		return frontLine.getEnd();
	}
	
	
	@Override
	public IVector2 getPosCorner()
	{
		return frontLine.getStart();
	}
	
	
	@Override
	public List<IDrawableShape> getDrawableShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>(3);
		shapes.add(new DrawableArc(arcNeg, Color.white));
		shapes.add(new DrawableArc(arcPos, Color.white));
		shapes.add(new DrawableRectangle(getInnerRectangle(), Color.white));
		return shapes;
	}
	
	
	@Override
	public double distanceTo(final IVector2 point)
	{
		return nearestPointInside(point).distanceTo(point);
	}
	
	
	@Override
	public double distanceToNearestPointOutside(final IVector2 pos)
	{
		return nearestPointOutside(pos).distanceTo(pos);
	}
	
	
	public double getRadius()
	{
		return radius;
	}
	
	
	public double getFrontLineHalfLength()
	{
		return 0.5 * frontLineLength;
	}
}