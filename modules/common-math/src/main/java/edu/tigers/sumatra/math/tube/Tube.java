/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.tube;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


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
		return lineSegment.getPathStart();
	}


	@Override
	public IVector2 endCenter()
	{
		return lineSegment.getPathEnd();
	}


	@Override
	public IVector2 center()
	{
		return lineSegment.getPathCenter();
	}


	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return lineSegment.distanceToSqr(point) < SumatraMath.square(radius);
	}


	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		if (lineSegment.getLength() <= 0)
		{
			return Circle.createCircle(startCenter(), radius).nearestPointOutside(point);
		}
		if (isPointInShape(point))
		{
			var leadPoint = lineSegment.closestPointOnPath(point);
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
		if (isPointInShape(point))
		{
			return point;
		}
		IVector2 leadPoint = lineSegment.closestPointOnPath(point);
		return LineMath.stepAlongLine(leadPoint, point, radius);
	}


	@Override
	public IVector2 nearestPointOnPerimeterPath(IVector2 point)
	{
		var closest = lineSegment.closestPointOnPath(point);
		if (closest.isCloseTo(point))
		{
			return lineSegment.directionVector().getNormalVector().scaleToNew(radius).add(closest);
		}

		return point.subtractNew(closest).scaleTo(radius).add(closest);
	}


	@Override
	public List<IBoundedPath> getPerimeterPath()
	{
		if (SumatraMath.isZero(lineSegment.getLength()))
		{
			return List.of(Circle.createCircle(lineSegment.getPathStart(), radius));
		}

		var offset = lineSegment.directionVector().getNormalVector().scaleTo(radius);

		List<IVector2> corners = new ArrayList<>();
		corners.add(lineSegment.getPathStart().addNew(offset));
		corners.add(lineSegment.getPathEnd().addNew(offset));
		corners.add(lineSegment.getPathStart().subtractNew(offset));
		corners.add(lineSegment.getPathEnd().subtractNew(offset));

		var angle = AngleMath.normalizeAngle(lineSegment.directionVector().getAngle() + AngleMath.PI_HALF);

		return List.of(
				Arc.createArc(lineSegment.getPathStart(), radius, angle, AngleMath.PI),
				Lines.segmentFromPoints(corners.get(0), corners.get(1)),
				Arc.createArc(lineSegment.getPathEnd(), radius, angle - AngleMath.PI, AngleMath.PI),
				Lines.segmentFromPoints(corners.get(3), corners.get(2))
		);
	}


	@Override
	public ITube withMargin(final double margin)
	{
		return new Tube(lineSegment, radius + margin);
	}


	@Override
	public double distanceTo(IVector2 point)
	{
		return lineSegment.distanceTo(point) - radius;
	}
}
