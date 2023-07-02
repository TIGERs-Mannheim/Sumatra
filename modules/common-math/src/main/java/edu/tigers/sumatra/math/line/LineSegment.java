/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.intersections.IIntersections;
import edu.tigers.sumatra.math.intersections.ISingleIntersection;
import edu.tigers.sumatra.math.intersections.PathIntersectionMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


/**
 * Implementation of the {@link ILineSegment} interface. It provides a factory method to create new instances.
 *
 * @author Lukas Magel
 */
@Persistent
final class LineSegment extends ALine implements ILineSegment
{
	private final Vector2f start;
	private final Vector2f end;
	private final boolean valid;

	/**
	 * this field is calculated on demand
	 */
	private transient IVector2 directionVector;


	/**
	 * Used by berkely
	 */
	@SuppressWarnings("unused")
	private LineSegment()
	{
		start = Vector2f.ZERO_VECTOR;
		end = Vector2f.ZERO_VECTOR;
		valid = false;
	}


	private LineSegment(final IVector2 start, final IVector2 end)
	{
		this.start = Vector2f.copy(start);
		this.end = Vector2f.copy(end);
		valid = !start.equals(end);
	}


	/**
	 * Create a new line instance which is defined by a single {@code supportVector}, i.e. the starting point, and the
	 * offset from start to end. This means that the end point is calculated as follows:
	 * {@code supportVector + offset}.
	 *
	 * @param supportVector The origin of this line segment
	 * @param offset        The offset between start and end of this line {@code end - start}
	 * @return A new line instance
	 * @throws IllegalArgumentException If the {@code offset} has a length of zero
	 */
	public static ILineSegment fromOffset(final IVector2 supportVector, final IVector2 offset)
	{
		IVector2 end = supportVector.addNew(offset);
		return fromPoints(supportVector, end);
	}


	/**
	 * Create a new line segment which spans from the specified point {@code start} to the second point {@code end}.
	 *
	 * @param start The point from which the line segment extends
	 * @param end   The point which the line segment extends to
	 * @return A new line segment instance
	 * @throws IllegalArgumentException If the points {@code start} and {@code end} are identical. Please perform a check
	 *                                  in your code before you call this method!
	 */
	public static ILineSegment fromPoints(final IVector2 start, final IVector2 end)
	{
		return new LineSegment(start, end);
	}


	@Override
	public boolean isValid()
	{
		return valid;
	}


	@Override
	public boolean equals(final Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof ILineSegment that))
		{
			return false;
		}

		return getPathStart().equals(that.getPathStart()) && getPathEnd().equals(that.getPathEnd());
	}


	@Override
	public int hashCode()
	{
		int result = getPathStart().hashCode();
		result = (31 * result) + getPathEnd().hashCode();
		return result;
	}


	@Override
	public IVector2 getPathStart()
	{
		return start;
	}


	@Override
	public IVector2 getPathEnd()
	{
		return end;
	}


	@Override
	public IVector2 getPathCenter()
	{
		return start.addNew(directionVector().multiplyNew(0.5));
	}


	@Override
	public double getLength()
	{
		return directionVector().getLength();
	}


	@Override
	public IVector2 directionVector()
	{
		if (directionVector == null)
		{
			directionVector = end.subtractNew(start);
		}
		return directionVector;
	}


	@Override
	public IVector2 closestPointOnPath(final IVector2 point)
	{
		return LineMath.closestPointOnLineSegment(this, point);
	}


	@Override
	public ILine toLine()
	{
		return Line.createNewWithoutCopy(getPathStart(), directionVector());
	}


	@Override
	public IHalfLine toHalfLine()
	{
		return HalfLine.createNewWithoutCopy(getPathStart(), directionVector());
	}


	@Override
	public ILineSegment copy()
	{
		return fromPoints(getPathStart(), getPathEnd());
	}


	@Override
	public ISingleIntersection intersect(final ILine line)
	{
		return PathIntersectionMath.intersectLineAndLineSegment(line, this);
	}


	@Override
	public ISingleIntersection intersect(final IHalfLine halfLine)
	{
		return PathIntersectionMath.intersectHalfLineAndLineSegment(halfLine, this);
	}


	@Override
	public ISingleIntersection intersect(final ILineSegment other)
	{
		return PathIntersectionMath.intersectLineSegmentAndLineSegment(this, other);
	}


	@Override
	public IIntersections intersect(ICircle circle)
	{
		return PathIntersectionMath.intersectLineSegmentAndCircle(this, circle);
	}


	@Override
	public IIntersections intersect(IArc arc)
	{
		return PathIntersectionMath.intersectLineSegmentAndArc(this, arc);
	}


	@Override
	public IVector2 stepAlongPath(final double stepSize)
	{
		if (!isValid())
		{
			return getPathStart();
		}
		return LineMath.stepAlongLine(getPathStart(), getPathEnd(), stepSize);
	}


	@Override
	public ILineSegment withMargin(final double margin)
	{
		IVector2 newStart = LineMath.stepAlongLine(getPathStart(), getPathEnd(), -margin);
		IVector2 newEnd = LineMath.stepAlongLine(getPathEnd(), getPathStart(), -margin);
		return Lines.segmentFromPoints(newStart, newEnd);
	}


	@Override
	public String toString()
	{
		return "LineSegment(" + start + " -> " + end + ")";
	}


	@Override
	public List<IVector2> getSteps(final double stepSize)
	{
		List<IVector2> steps = new ArrayList<>();
		double len = getLength();
		for (double step = 0; step < len; step += stepSize)
		{
			steps.add(stepAlongPath(step));
		}
		steps.add(stepAlongPath(len));
		return steps;
	}


	@Override
	public double distanceTo(final ILineSegment line)
	{
		if (intersect(line).isPresent())
		{
			return 0;
		}
		return Math.sqrt(Stream.of(
				line.distanceToSqr(this.getPathStart()),
				line.distanceToSqr(this.getPathEnd()),
				this.distanceToSqr(line.getPathStart()),
				this.distanceToSqr(line.getPathEnd())
		).mapToDouble(d -> d).min().orElseThrow());
	}


	@Override
	public double distanceFromStart(IVector2 pointOnPath)
	{
		return pointOnPath.distanceTo(getPathStart());
	}
}
