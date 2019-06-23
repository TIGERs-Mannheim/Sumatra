/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.penarea;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Represents a rectangular penalty area built around a simple rectangle.
 *
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class RectifiedPenaltyArea implements IDefensePenArea
{
	private final IRectangle rectangle;
	private final IVector2 goalCenter;


	public RectifiedPenaltyArea(double lengthX, double lengthY)
	{
		this(Geometry.getGoalOur().getCenter(), lengthX, lengthY);
	}


	public RectifiedPenaltyArea(IVector2 center, double lengthX, double lengthY)
	{
		goalCenter = center;

		IVector2 lowerLeft = Vector2.fromXY(center.x(),center.y() - lengthY / 2);
		IVector2 upperRight = Vector2.fromXY(center.x() - Math.signum(center.x()) * lengthX,center.y() + lengthY / 2);
		rectangle = Rectangle.fromPoints(lowerLeft, upperRight);
	}


	private List<ILineSegment> getEdges()
	{
		List<ILineSegment> edges = new ArrayList<>();
		double lowerX = goalCenter.x();
		double upperX = lowerX - Math.signum(goalCenter.x()) * rectangle.xExtent();
		double negY = -rectangle.yExtent() / 2;
		double posY = rectangle.yExtent() / 2;
		IVector2 p1 = Vector2.fromXY(lowerX, negY);
		IVector2 p2 = Vector2.fromXY(upperX, negY);
		IVector2 p3 = Vector2.fromXY(upperX, posY);
		IVector2 p4 = Vector2.fromXY(lowerX, posY);
		edges.add(Lines.segmentFromPoints(p1, p2));
		edges.add(Lines.segmentFromPoints(p2, p3));
		edges.add(Lines.segmentFromPoints(p3, p4));
		return edges;
	}


	@Override
	public IDefensePenArea withMargin(final double margin)
	{
		return new RectifiedPenaltyArea(goalCenter, Math.max(0, rectangle.xExtent() + margin),
				Math.max(0, rectangle.yExtent() + margin * 2));
	}


	@Override
	public IVector2 stepAlongPenArea(final double length)
	{
		List<ILineSegment> edges = getEdges();
		double steppedLength = 0;
		for (ILineSegment edge : edges)
		{
			if (steppedLength + edge.getLength() < length)
			{
				steppedLength += edge.getLength();
			} else
			{
				return edge.stepAlongLine(length - steppedLength);
			}
		}

		throw new IllegalArgumentException(
				"Tried to step too long along penalty area: " + length + " (max.: " + steppedLength + ")");
	}


	@Override
	public IVector2 stepAlongPenArea(final IVector2 startPoint, final double length)
	{
		double lengthToPoint = lengthToPointOnPenArea(startPoint);
		return stepAlongPenArea(Math.min(getLength(), Math.max(0, lengthToPoint + length)));
	}


	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return rectangle.isPointInShape(point);
	}


	@Override
	public IVector2 projectPointOnPenaltyAreaLine(final IVector2 point)
	{
		IVector2 projectedPoint = Vector2.fromXY(Math.max(point.x(), goalCenter.x()), point.y());
		ILine pointToGoal = Line.fromPoints(projectedPoint, goalCenter);

		if (pointToGoal.directionVector().isZeroVector())
		{
			pointToGoal = Line.fromPoints(projectedPoint, Geometry.getCenter());
		}

		List<IVector2> intersections = lineIntersections(pointToGoal);

		if (!intersections.isEmpty())
		{
			return intersections.stream().min(Comparator.comparingDouble(v -> v.distanceTo(projectedPoint)))
					.orElseThrow(IllegalStateException::new);
		}

		throw new IllegalArgumentException("Cannot find intersection with penalty area. This is a serious bug!");
	}


	@Override
	public double lengthToPointOnPenArea(final IVector2 point)
	{
		IVector2 intersectionPoint = projectPointOnPenaltyAreaLine(point);
		if (intersectionPoint == null)
		{
			throw new UnsupportedOperationException(
					"Line to goal does not intersect penalty area line - start point is probably behind goal");
		}

		List<ILineSegment> edges = getEdges();
		double steppedLength = 0;
		for (ILineSegment edge : edges)
		{
			if (edge.isPointOnLine(intersectionPoint))
			{
				steppedLength += edge.getStart().distanceTo(intersectionPoint);
				break;
			} else
			{
				steppedLength += edge.getLength();
			}
		}

		return steppedLength;
	}


	@Override
	public double getLength()
	{
		List<ILineSegment> edges = getEdges();
		double steppedLength = 0;
		for (ILineSegment edge : edges)
		{
			steppedLength += edge.getLength();
		}
		return steppedLength;
	}


	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return rectangle.isPointInShape(point, margin);
	}


	@Override
	public IVector2 nearestPointOutside(final IVector2 point, final IVector2 pointToBuildLine)
	{
		if (!isPointInShape(point))
		{
			return point;
		}

		return lineIntersections(Line.fromPoints(point, pointToBuildLine)).stream()
				.min(Comparator.comparingDouble(v -> v.distanceTo(point)))
				.orElse(Vector2.fromXY(rectangle.maxX(), point.y()));
	}


	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		return nearestPointOutside(point, goalCenter);
	}


	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		List<IVector2> intersections = new ArrayList<>();
		List<ILineSegment> edges = getEdges();
		for (ILineSegment edge : edges)
		{
			edge.intersectLine(Lines.lineFromLegacyLine(line)).ifPresent(intersections::add);
		}

		return intersections.stream().distinct().collect(Collectors.toList());
	}


	@Override
	public IVector2 nearestPointInside(final IVector2 pos)
	{
		if (isPointInShape(pos))
		{
			return pos;
		}

		return nearestPointOutside(goalCenter, pos);
	}


	@Override
	public double getFrontLineHalfLength()
	{
		return rectangle.yExtent() / 2;
	}
}
