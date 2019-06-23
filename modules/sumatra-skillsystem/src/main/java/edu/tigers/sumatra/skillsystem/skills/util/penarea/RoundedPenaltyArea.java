/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.penarea;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * A rectangular penalty area with rounded corners
 *
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class RoundedPenaltyArea implements IDefensePenArea
{
	
	private double lengthX;
	private double lengthY;
	private double cornerRadius;
	
	/** The rectangle left to the inner rectangle (in center direction) */
	private IRectangle leftRectangle;
	private ILineSegment leftLineSegment;
	
	/** The arc connecting the inner rectangle and the left rectangle */
	private IArc leftArc;
	
	/** The inner rectangle (near to the goal) */
	private IRectangle innerRectangle;
	private ILineSegment innerLineSegment;
	
	/** The arc connecting the inner rectangle and the right rectangle */
	private IArc rightArc;
	
	/** The rectangle right to the inner rectangle (in center direction) */
	private IRectangle rightRectangle;
	private ILineSegment rightLineSegment;
	
	
	public RoundedPenaltyArea(double lengthX, double lengthY, double cornerRadius)
	{
		this.lengthX = lengthX;
		this.lengthY = lengthY;
		this.cornerRadius = cornerRadius;
		
		IVector2 goalCenter = Geometry.getGoalOur().getCenter();
		
		IVector2 leftInnerPoint = Vector2.fromXY(goalCenter.x() + lengthX, goalCenter.y() - (lengthY / 2 - cornerRadius));
		IVector2 rightInnerPoint = Vector2.fromXY(goalCenter.x() + lengthX,
				goalCenter.y() + (lengthY / 2 - cornerRadius));
		
		innerRectangle = Rectangle.fromPoints(
				Vector2.fromXY(goalCenter.x(), goalCenter.y() - (lengthY / 2 - cornerRadius)),
				rightInnerPoint);
		innerLineSegment = Lines.segmentFromPoints(leftInnerPoint, rightInnerPoint);
		
		leftRectangle = Rectangle.fromPoints(Vector2.fromXY(goalCenter.x(), goalCenter.y() - lengthY / 2),
				Vector2.fromXY(goalCenter.x() + lengthX, goalCenter.y() - lengthY / 2));
		leftLineSegment = Lines.segmentFromPoints(Vector2.fromXY(goalCenter.x(), goalCenter.y() - lengthY / 2),
				Vector2.fromXY(goalCenter.x() + lengthX - cornerRadius, goalCenter.y() - lengthY / 2));
		
		rightRectangle = Rectangle.fromPoints(Vector2.fromXY(goalCenter.x(), goalCenter.y() + lengthY / 2),
				Vector2.fromXY(goalCenter.x() + lengthX, goalCenter.y() + lengthY / 2));
		rightLineSegment = Lines.segmentFromPoints(
				Vector2.fromXY(goalCenter.x() + lengthX - cornerRadius, goalCenter.y() + lengthY / 2),
				Vector2.fromXY(goalCenter.x(), goalCenter.y() + lengthY / 2));
		
		leftArc = getPenAreaArc(
				Vector2.fromXY(goalCenter.x() + lengthX - cornerRadius, goalCenter.y() + (lengthY / 2 - lengthY)),
				cornerRadius);
		rightArc = getPenAreaArc(
				Vector2.fromXY(goalCenter.x() + lengthX - cornerRadius, goalCenter.y() - (lengthY / 2 - lengthY)),
				cornerRadius);
	}
	
	
	private IArc getPenAreaArc(final IVector2 center, final double radius)
	{
		double startAngle = Vector2f.X_AXIS.multiplyNew(-center.x()).getAngle();
		double stopAngle = Vector2f.Y_AXIS.multiplyNew(center.y()).getAngle();
		double rotation = AngleMath.difference(stopAngle, startAngle);
		return Arc.createArc(center, radius, startAngle, rotation);
	}
	
	
	@Override
	public IDefensePenArea withMargin(final double margin)
	{
		return new RoundedPenaltyArea(lengthX + margin, lengthY + margin, cornerRadius + margin);
	}
	
	
	@Override
	public IVector2 stepAlongPenArea(final IVector2 startPoint, final double length)
	{
		return stepAlongPenArea(lengthToPointOnPenArea(startPoint) + length);
	}
	
	
	@Override
	public IVector2 stepAlongPenArea(final double length)
	{
		if (length < leftLineSegment.getLength())
		{
			return leftLineSegment.stepAlongLine(length);
		}
		
		double quarterCircleLength = AngleMath.PI_HALF * cornerRadius;
		if (length < leftLineSegment.getLength() + quarterCircleLength)
		{
			return getPointOnCircle(leftArc.center(), cornerRadius, leftArc.getStartAngle()
					+ leftArc.getRotation() * (quarterCircleLength - (length - leftLineSegment.getLength())));
		}
		
		if (length < leftLineSegment.getLength() + quarterCircleLength + innerLineSegment.getLength())
		{
			return innerLineSegment.stepAlongLine(length - leftLineSegment.getLength() - quarterCircleLength);
		}
		
		if (length < leftLineSegment.getLength() + 2 * quarterCircleLength + innerLineSegment.getLength())
		{
			return getPointOnCircle(rightArc.center(), cornerRadius,
					rightArc.getStartAngle() + rightArc.getRotation() * (quarterCircleLength
							- (length - leftLineSegment.getLength() - quarterCircleLength - innerLineSegment.getLength())));
		}
		
		if (length < leftLineSegment.getLength() + 2 * quarterCircleLength + innerLineSegment.getLength()
				+ rightLineSegment.getLength())
		{
			return rightLineSegment.stepAlongLine(
					length - leftLineSegment.getLength() - 2 * quarterCircleLength - innerLineSegment.getLength());
		}
		
		throw new IllegalArgumentException("Tried to step too long along penalty area: " + length);
	}
	
	
	private IVector2 getPointOnCircle(final IVector2 origin, final double radius, final double angle)
	{
		return Vector2f.fromXY(origin.x() - (radius * SumatraMath.cos(angle)),
				origin.y() + (radius * SumatraMath.sin(angle)));
	}
	
	
	// Suppress warning about number of conditional operators
	@SuppressWarnings("squid:S1067")
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		
		return leftRectangle.isPointInShape(point) || leftArc.isPointInShape(point)
				|| innerRectangle.isPointInShape(point) || rightArc.isPointInShape(point)
				|| rightRectangle.isPointInShape(point);
	}
	
	
	@Override
	public IVector2 projectPointOnPenaltyAreaLine(final IVector2 point)
	{
		return projectPointOnPenaltyAreaLine(point, Geometry.getGoalOur().getCenter());
	}
	
	
	private IVector2 projectPointOnPenaltyAreaLine(final IVector2 point, final IVector2 pointToBuildLine)
	{
		ILine lineToGoal = Line.fromPoints(point, pointToBuildLine);
		
		ILineSegment leftArcLine = Lines.segmentFromPoints(leftLineSegment.getEnd(), innerLineSegment.getStart());
		if (leftArcLine.intersectLine(Lines.lineFromLegacyLine(lineToGoal)).isPresent())
		{
			return leftArc.center().addNew(lineToGoal.directionVector().multiplyNew(-1).scaleToNew(leftArc.radius()));
		}
		
		ILineSegment rightArcLine = Lines.segmentFromPoints(innerLineSegment.getEnd(), rightLineSegment.getStart());
		if (rightArcLine.intersectLine(Lines.lineFromLegacyLine(lineToGoal)).isPresent())
		{
			return rightArc.center().addNew(lineToGoal.directionVector().multiplyNew(-1).scaleToNew(rightArc.radius()));
		}
		
		
		if (leftLineSegment.intersectLine(Lines.lineFromLegacyLine(lineToGoal)).isPresent())
		{
			return leftLineSegment.intersectLine(Lines.lineFromLegacyLine(lineToGoal)).get();
		}
		
		if (rightLineSegment.intersectLine(Lines.lineFromLegacyLine(lineToGoal)).isPresent())
		{
			return rightLineSegment.intersectLine(Lines.lineFromLegacyLine(lineToGoal)).get();
		}
		
		if (innerLineSegment.intersectLine(Lines.lineFromLegacyLine(lineToGoal)).isPresent())
		{
			return innerLineSegment.intersectLine(Lines.lineFromLegacyLine(lineToGoal)).get();
		}
		
		throw new IllegalStateException("Not able to project point on penalty area line! This should not happen!");
	}
	
	
	@Override
	public double lengthToPointOnPenArea(final IVector2 point)
	{
		IVector2 projectedPoint = projectPointOnPenaltyAreaLine(point);
		if (leftLineSegment.isPointOnLine(projectedPoint))
		{
			return leftLineSegment.getStart().distanceTo(projectedPoint);
		}
		
		if (projectedPoint.x() > leftLineSegment.getEnd().x() && projectedPoint.y() < innerLineSegment.getStart().y())
		{
			final double alpha = SumatraMath.acos((projectedPoint.y() - innerLineSegment.getStart().y()) / cornerRadius);
			return leftLineSegment.getLength() + Math.abs(alpha * cornerRadius);
		}
		
		double quarterCircleLength = AngleMath.PI_HALF * cornerRadius;
		
		if (innerLineSegment.isPointOnLine(projectedPoint))
		{
			return leftLineSegment.getLength() + quarterCircleLength
					+ innerLineSegment.getStart().distanceTo(projectedPoint);
		}
		
		if (projectedPoint.y() > innerLineSegment.getEnd().y() && projectedPoint.x() > rightLineSegment.getStart().x())
		{
			final double alpha = SumatraMath.acos((projectedPoint.y() - innerLineSegment.getEnd().y()) / cornerRadius);
			return leftLineSegment.getLength() + innerLineSegment.getLength() + Math.abs(alpha * cornerRadius);
		}
		
		if (rightLineSegment.isPointOnLine(projectedPoint))
		{
			return leftLineSegment.getLength() + 2 * quarterCircleLength + innerLineSegment.getLength()
					+ rightLineSegment.getStart().distanceTo(projectedPoint);
		}
		
		throw new IllegalStateException("Cannot calculate length to point on penalty area!");
	}
	
	
	@Override
	public double getLength()
	{
		double quarterCircleLength = AngleMath.PI_HALF * cornerRadius;
		return leftLineSegment.getLength() + 2 * quarterCircleLength + innerLineSegment.getLength()
				+ rightLineSegment.getLength();
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return withMargin(margin).isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point, final IVector2 pointToBuildLine)
	{
		if (isPointInShape(point))
		{
			return projectPointOnPenaltyAreaLine(point, pointToBuildLine);
		}
		
		return point;
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		return nearestPointOutside(point, Geometry.getGoalOur().getCenter());
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		List<IVector2> intersections = new ArrayList<>();
		
		if (leftArc.isIntersectingWithLine(line))
		{
			intersections.add(leftArc.lineIntersections(line).get(0));
		}
		
		if (rightArc.isIntersectingWithLine(line))
		{
			intersections.add(rightArc.lineIntersections(line).get(0));
		}
		
		leftLineSegment.intersectLine(Lines.lineFromLegacyLine(line)).ifPresent(intersections::add);
		rightLineSegment.intersectLine(Lines.lineFromLegacyLine(line)).ifPresent(intersections::add);
		innerLineSegment.intersectLine(Lines.lineFromLegacyLine(line)).ifPresent(intersections::add);
		
		return intersections;
	}
	
	
	@Override
	public IVector2 nearestPointInside(final IVector2 pos)
	{
		if (isPointInShape(pos))
		{
			return pos;
		}
		
		return projectPointOnPenaltyAreaLine(pos);
	}
	
	
	@Override
	public double getFrontLineHalfLength()
	{
		return innerLineSegment.getLength() / 2;
	}
}
