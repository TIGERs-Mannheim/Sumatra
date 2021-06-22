/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
	}


	private LineSegment(final IVector2 start, final IVector2 end)
	{
		this.start = Vector2f.copy(start);
		this.end = Vector2f.copy(end);
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
		return !start.equals(end);
	}


	@Override
	public final boolean equals(final Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof ILineSegment))
		{
			return false;
		}

		final ILineSegment that = (ILineSegment) other;

		return getStart().equals(that.getStart()) && getEnd().equals(that.getEnd());
	}


	@Override
	public final int hashCode()
	{
		int result = getStart().hashCode();
		result = (31 * result) + getEnd().hashCode();
		return result;
	}


	@Override
	public IVector2 getStart()
	{
		return start;
	}


	@Override
	public IVector2 getEnd()
	{
		return end;
	}


	@Override
	public IVector2 getCenter()
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
	public IVector2 closestPointOnLine(final IVector2 point)
	{
		return LineMath.closestPointOnLineSegment(this, point);
	}


	@Override
	public ILine toLine()
	{
		return Line.createNewWithoutCopy(getStart(), directionVector());
	}


	@Override
	public IHalfLine toHalfLine()
	{
		return HalfLine.createNewWithoutCopy(getStart(), directionVector());
	}


	@Override
	public ILineSegment copy()
	{
		return fromPoints(getStart(), getEnd());
	}


	@Override
	public Optional<IVector2> intersectLine(final ILine line)
	{
		return line.intersectSegment(this);
	}


	@Override
	public Optional<IVector2> intersectHalfLine(final IHalfLine halfLine)
	{
		return halfLine.intersectSegment(this);
	}


	@Override
	public Optional<IVector2> intersectSegment(final ILineSegment other)
	{
		if (isValid() && other.isValid())
		{
			return LineMath.intersectionPointOfSegments(this, other);
		}
		return Optional.empty();
	}


	@Override
	public IVector2 stepAlongLine(final double stepSize)
	{
		if (!isValid())
		{
			return getStart();
		}
		return LineMath.stepAlongLine(getStart(), getEnd(), stepSize);
	}


	@Override
	public ILineSegment withMargin(final double margin)
	{
		IVector2 newStart = LineMath.stepAlongLine(getStart(), getEnd(), -margin);
		IVector2 newEnd = LineMath.stepAlongLine(getEnd(), getStart(), -margin);
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
			steps.add(stepAlongLine(step));
		}
		steps.add(stepAlongLine(len));
		return steps;
	}


	@Override
	public double distanceTo(final ILineSegment line)
	{
		if (intersectSegment(line).isPresent())
		{
			return 0;
		}
		return Math.sqrt(List.of(
				line.distanceToSqr(this.getStart()),
				line.distanceToSqr(this.getEnd()),
				this.distanceToSqr(line.getStart()),
				this.distanceToSqr(line.getEnd())
		).stream().mapToDouble(d -> d).min().orElseThrow());
	}
}
