/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.intersections.IIntersections;
import edu.tigers.sumatra.math.intersections.ISingleIntersection;
import edu.tigers.sumatra.math.intersections.PathIntersectionMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Implementation of the {@link IHalfLine} interface. It represents a half-line which extends indefinitely from its
 * starting point. The class provide a static factory method to create new instances.
 *
 * @author Lukas Magel
 */
final class HalfLine extends AUnboundedLine implements IHalfLine
{

	private HalfLine(final IVector2 supportVector, final IVector2 directionVector)
	{
		super(supportVector, directionVector);
	}


	/**
	 * Create a new {@link IHalfLine} instance which extends from the specified {@code supportVector} in the direction of
	 * {@code directionVector} indefinitely.
	 *
	 * @param supportVector   The support vector which defines the starting point of the created half-line
	 * @param directionVector The direction vector which defines the direction in which the half-line extends
	 * @return A new {@code IHalfLine} instance
	 * @throws IllegalArgumentException If the {@code directionVector} has a length of zero. Please perform a check in your code before you
	 *                                  call this method!
	 */
	public static IHalfLine fromDirection(final IVector2 supportVector, final IVector2 directionVector)
	{
		IVector2 sV = Vector2.copy(supportVector);
		IVector2 dV = directionVector.normalizeNew();
		return createNewWithoutCopy(sV, dV);
	}


	/**
	 * Create a new instance without copying the vector parameters. Only use this method if you're sure
	 * that the two vector parameters will not be modified through side-effects.
	 *
	 * @param supportVector   The support vector of the new half-line instance
	 * @param directionVector The direction vector of the new half-line instance
	 * @return A new half-line instance which uses the two vector parameters
	 */
	static IHalfLine createNewWithoutCopy(final IVector2 supportVector, final IVector2 directionVector)
	{
		return new HalfLine(supportVector, directionVector);
	}


	@Override
	public boolean equals(final Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof IHalfLine that))
		{
			return false;
		}

		return this.supportVector().equals(that.supportVector())
				&& this.directionVector().equals(that.directionVector());
	}


	@Override
	public int hashCode()
	{
		int result = supportVector().hashCode();
		result = 31 * result + directionVector().hashCode();
		return result;
	}


	@Override
	public IVector2 closestPointOnPath(final IVector2 point)
	{
		if (!isValid())
		{
			return supportVector();
		}
		return LineMath.closestPointOnHalfLine(this, point);
	}


	@Override
	public ILine toLine()
	{
		return Line.createNewWithoutCopy(supportVector(), directionVector());
	}


	@Override
	public IHalfLine copy()
	{
		IVector2 supportVector = supportVector().copy();
		IVector2 directionVector = directionVector().copy();
		return new HalfLine(supportVector, directionVector);
	}


	@Override
	public ISingleIntersection intersect(final ILine line)
	{
		return PathIntersectionMath.intersectLineAndHalfLine(line, this);
	}


	@Override
	public ISingleIntersection intersect(final IHalfLine other)
	{
		return PathIntersectionMath.intersectHalfLineAndHalfLine(this, other);
	}


	@Override
	public ISingleIntersection intersect(final ILineSegment segment)
	{
		return PathIntersectionMath.intersectHalfLineAndLineSegment(this, segment);
	}


	@Override
	public IIntersections intersect(ICircle circle)
	{
		return PathIntersectionMath.intersectHalfLineAndCircle(this, circle);
	}


	@Override
	public IIntersections intersect(IArc arc)
	{
		return PathIntersectionMath.intersectHalfLineAndArc(this, arc);
	}


	@Override
	public boolean isPointInFront(final IVector2 point)
	{
		return isValid() && LineMath.isPointInFront(this, point);
	}


	@Override
	public ILineSegment toLineSegment(double length)
	{
		return LineSegment.fromOffset(supportVector(), directionVector().scaleToNew(length));
	}


	@Override
	public String toString()
	{
		return "HalfLine(" + supportVector() + " + s * " + directionVector() + ")";
	}
}
