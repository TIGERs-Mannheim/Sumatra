/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import java.util.Optional;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Implementation of the {@link ILineSegment} interface. It provides a factory method to create new instances.
 * 
 * @author Lukas Magel
 */
final class LineSegment extends ALine implements ILineSegment
{
	private final IVector2	start;
	private final IVector2	end;
	
	private final boolean	isValid;
	private final IVector2	directionVector;
	private final IVector2	displacement;
	
	
	private LineSegment(final IVector2 start, final IVector2 end)
	{
		this.start = start;
		this.end = end;
		
		this.displacement = end.subtractNew(start);
		this.directionVector = displacement.normalizeNew();
		this.isValid = !directionVector.isZeroVector();
	}
	
	
	/**
	 * Create a new line instance which is defined by a single {@code supportVector}, i.e. the starting point, and the
	 * offset from start to end. This means that the end point is calculated as follows:
	 * {@code supportVector + offset}.
	 * 
	 * @param supportVector
	 *           The origin of this line segment
	 * @param offset
	 *           The offset between start and end of this line {@code end - start}
	 * @return
	 * 			A new line instance
	 * @throws IllegalArgumentException If the {@code offset} has a length of zero
	 */
	public static ILineSegment fromOffset(final IVector2 supportVector, final IVector2 offset)
	{
		IVector2 end = supportVector.addNew(offset);
		return createNewFromVectorCopy(supportVector, end);
	}
	
	
	/**
	 * Create a new line segment which spans from the specified point {@code start} to the second point {@code end}.
	 * 
	 * @param start
	 *           The point from which the line segment extends
	 * @param end
	 *           The point which the line segment extends to
	 * @return
	 * 			A new line segment instance
	 * @throws IllegalArgumentException If the points {@code start} and {@code end} are identical. Please perform a check
	 *            in your code before you call this method!
	 */
	public static ILineSegment fromPoints(final IVector2 start, final IVector2 end)
	{
		return createNewFromVectorCopy(start, end);
	}
	
	
	private static ILineSegment createNewFromVectorCopy(final IVector2 start, final IVector2 end)
	{
		IVector2 segmentStart = Vector2.copy(start);
		IVector2 segmentEnd = Vector2.copy(end);
		
		return new LineSegment(segmentStart, segmentEnd);
	}
	
	
	@Override
	public boolean isValid()
	{
		return isValid;
	}
	
	
	@Override
	public final boolean equals(final Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other == null || !(other instanceof ILineSegment))
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
		result = 31 * result + getEnd().hashCode();
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
	public IVector2 getDisplacement()
	{
		return displacement;
	}
	
	
	@Override
	public double getLength()
	{
		return getEnd().subtractNew(getStart()).getLength();
	}
	
	
	@Override
	public IVector2 directionVector()
	{
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
		IVector2 segmentStart = getStart();
		IVector2 segmentEnd = getEnd();
		return createNewFromVectorCopy(segmentStart, segmentEnd);
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
		if (this.isValid() && other.isValid())
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
}
