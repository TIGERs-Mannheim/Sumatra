/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.tube;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Implementation of a {@link ITube}
 */
@Persistent(version = 1)
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "create")
public class Tube implements ITube
{
	private final ILineSegment lineSegment;
	private final double radius;


	/**
	 * for DB only
	 */
	@SuppressWarnings("unused")
	private Tube()
	{
		lineSegment = Lines.segmentFromPoints(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);
		radius = 1;
	}


	/**
	 * tube around LineSegment with given radius [mm]
	 * adds radius to start and endpoint of lineSegment
	 *
	 * @param lineSegment
	 * @param radius      [mm]
	 * @return
	 */
	public static Tube fromLineSegment(final ILineSegment lineSegment, final double radius)
	{
		return create(lineSegment, radius);
	}


	/**
	 * Tube around LineSegment with given radius [mm].
	 *
	 * @param start
	 * @param end
	 * @param radius
	 * @return
	 */
	public static Tube create(final IVector2 start, IVector2 end, final double radius)
	{
		return create(Lines.segmentFromPoints(start, end), radius);
	}


	@Override
	public double radius()
	{
		return radius;
	}


	@Override
	public IVector2 startCenter()
	{
		return lineSegment.getStart();
	}


	@Override
	public IVector2 endCenter()
	{
		return lineSegment.getEnd();
	}


	@Override
	public IVector2 center()
	{
		return lineSegment.getCenter();
	}


	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return lineSegment.distanceToSqr(point) < SumatraMath.square(radius);
	}


	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return lineSegment.distanceToSqr(point) < SumatraMath.square(radius + margin);
	}


	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		if (lineSegment.getLength() <= 0)
		{
			return Circle.createCircle(startCenter(), radius).nearestPointOutside(point);
		}
		if (lineSegment.distanceToSqr(point) < SumatraMath.square(radius))
		{
			var leadPoint = lineSegment.closestPointOnLine(point);
			if (leadPoint.equals(point))
			{
				var dir = lineSegment.directionVector().getNormalVector().scaleTo(radius);
				return point.addNew(dir);
			}
			return LineMath.stepAlongLine(leadPoint, point, radius);
		}
		return point;
	}


	@Override
	public IVector2 nearestPointInside(final IVector2 point)
	{
		if (lineSegment.distanceToSqr(point) < SumatraMath.square(radius))
		{
			return point;
		}
		IVector2 leadPoint = lineSegment.closestPointOnLine(point);
		return LineMath.stepAlongLine(leadPoint, point, radius - 1);
	}


	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		if (lineSegment.getLength() <= 0)
		{
			return Circle.createCircle(startCenter(), radius).lineIntersections(line);
		}
		ICircle startCircle = Circle.createCircle(startCenter(), radius);
		ICircle endCircle = Circle.createCircle(endCenter(), radius);

		List<IVector2> intersections = startCircle.lineIntersections(line).stream()
				.filter(v -> lineSegment.closestPointOnLine(v).equals(startCenter()))
				.collect(Collectors.toList());

		List<IVector2> intersectsAtEnd = endCircle.lineIntersections(line);
		for (IVector2 v : intersectsAtEnd)
		{
			if (lineSegment.closestPointOnLine(v).equals(endCenter()))
			{
				intersections.add(v);
			}
		}

		IVector2 start1 = startCenter().addNew(lineSegment.directionVector().getNormalVector().scaleTo(radius));
		IVector2 end1 = endCenter().addNew(lineSegment.directionVector().getNormalVector().scaleTo(radius));
		Lines.segmentFromPoints(start1, end1).intersectLine(line.v2()).ifPresent(intersections::add);
		IVector2 start2 = startCenter().addNew(lineSegment.directionVector().getNormalVector().scaleTo(-radius));
		IVector2 end2 = endCenter().addNew(lineSegment.directionVector().getNormalVector().scaleTo(-radius));
		Lines.segmentFromPoints(start2, end2).intersectLine(line.v2()).ifPresent(intersections::add);

		return intersections.stream().distinct().collect(Collectors.toList());
	}


	@Override
	public ITube withMargin(final double margin)
	{
		return new Tube(lineSegment, radius + margin);
	}
}
