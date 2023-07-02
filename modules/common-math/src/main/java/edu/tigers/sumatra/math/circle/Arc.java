/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.intersections.IIntersections;
import edu.tigers.sumatra.math.intersections.PathIntersectionMath;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.util.List;


/**
 * Implementation of {@link IArc}
 */
@Persistent
public class Arc implements IArc
{
	private final double startAngle;
	private final double rotation;
	private final Vector2f center;
	private final double radius;


	/**
	 * Used by berkely
	 */
	@SuppressWarnings("unused")
	protected Arc()
	{
		center = Vector2f.ZERO_VECTOR;
		radius = 1;
		startAngle = 0;
		rotation = 1;
	}


	/**
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 */
	protected Arc(final IVector2 center, final double radius, final double startAngle, final double rotation)
	{
		this.center = Vector2f.copy(center);
		this.radius = radius;
		this.startAngle = AngleMath.normalizeAngle(startAngle);
		this.rotation = rotation;
	}


	/**
	 * @param arc
	 */
	protected Arc(final IArc arc)
	{
		center = Vector2f.copy(arc.center());
		radius = arc.radius();
		startAngle = arc.getStartAngle();
		rotation = arc.getRotation();
	}


	/**
	 * Create a new arc
	 *
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 * @return
	 */
	public static IArc createArc(final IVector2 center, final double radius, final double startAngle,
			final double rotation)
	{
		return new Arc(center, radius, startAngle, rotation);
	}


	@Override
	public IArc mirror()
	{
		return createArc(center().multiplyNew(-1), radius, startAngle + AngleMath.PI, rotation);
	}


	@Override
	public double radius()
	{
		return radius;
	}


	@Override
	public IVector2 center()
	{
		return center;
	}


	@Override
	public final double getStartAngle()
	{
		return startAngle;
	}


	@Override
	public final double getRotation()
	{
		return rotation;
	}


	@Override
	public final boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof IArc arc))
			return false;

		return center.equals(arc.center())
				&& SumatraMath.isEqual(radius, arc.radius())
				&& SumatraMath.isEqual(startAngle, arc.getStartAngle())
				&& SumatraMath.isEqual(rotation, arc.getRotation());
	}


	@Override
	public final int hashCode()
	{
		int result;
		long temp;
		temp = Double.doubleToLongBits(getStartAngle());
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getRotation());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + center.hashCode();
		temp = Double.doubleToLongBits(radius);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}


	@Override
	public String toString()
	{
		return "Arc{" +
				"startAngle=" + startAngle +
				", rotation=" + rotation +
				", center=" + center +
				", radius=" + radius +
				'}';
	}


	@Override
	public List<IBoundedPath> getPerimeterPath()
	{
		return List.of(
				Lines.segmentFromPoints(center, getPathStart()),
				this,
				Lines.segmentFromPoints(getPathEnd(), center)
		);
	}


	@Override
	public IArc withMargin(double margin)
	{
		return new Arc(center, radius + margin, startAngle, rotation);
	}


	@Override
	public boolean isValid()
	{
		return radius > SumatraMath.getEqualTol() && center.isFinite() && !SumatraMath.isZero(rotation);
	}


	@Override
	public IIntersections intersect(ILine line)
	{
		return PathIntersectionMath.intersectLineAndArc(line, this);
	}


	@Override
	public IIntersections intersect(IHalfLine halfLine)
	{
		return PathIntersectionMath.intersectHalfLineAndArc(halfLine, this);
	}


	@Override
	public IIntersections intersect(ILineSegment segment)
	{
		return PathIntersectionMath.intersectLineSegmentAndArc(segment, this);
	}


	@Override
	public IIntersections intersect(ICircle circle)
	{
		return PathIntersectionMath.intersectCircleAndArc(circle, this);
	}


	@Override
	public IIntersections intersect(IArc other)
	{
		return PathIntersectionMath.intersectArcAndArc(this, other);
	}


	@Override
	public IVector2 closestPointOnPath(IVector2 point)
	{
		return CircleMath.nearestPointOnArcLine(this, point);
	}


	@Override
	public boolean isPointOnPath(IVector2 point)
	{
		if (Math.abs(center.distanceTo(point) - radius) > LINE_MARGIN)
		{
			return false;
		}
		return PathIntersectionMath.isCirclePointOnCircular(this, point);
	}


	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return CircleMath.isPointInArc(this, point, 0.0);
	}


	@Override
	public IVector2 getPathStart()
	{
		return center.addNew(Vector2.fromX(radius).turn(startAngle));
	}


	@Override
	public IVector2 getPathEnd()
	{
		return center.addNew(Vector2.fromX(radius).turn(startAngle + rotation));
	}


	@Override
	public IVector2 getPathCenter()
	{
		return center.addNew(Vector2.fromX(radius).turn(startAngle + 0.5 * rotation));
	}


	@Override
	public double getLength()
	{
		return Math.abs(rotation) * radius;
	}


	@Override
	public IVector2 stepAlongPath(double stepSize)
	{
		var angle = AngleMath.normalizeAngle(startAngle + (stepSize / getLength() * rotation));
		return center.addNew(Vector2.fromX(radius).turn(angle));
	}


	@Override
	public double distanceFromStart(IVector2 pointOnPath)
	{
		var anglePointOnPath = Vector2.fromPoints(center, pointOnPath).getAngle();
		return AngleMath.diffAbs(startAngle, anglePointOnPath) * radius;
	}
}
