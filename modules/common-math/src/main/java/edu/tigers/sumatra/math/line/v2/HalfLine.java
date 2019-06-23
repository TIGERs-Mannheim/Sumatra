/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import java.util.Optional;

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
	 * @param supportVector
	 *           The support vector which defines the starting point of the created half-line
	 * @param directionVector
	 *           The direction vector which defines the direction in which the half-line extends
	 * @return
	 * 			A new {@code IHalfLine} instance
	 * @throws IllegalArgumentException
	 *            If the {@code directionVector} has a length of zero. Please perform a check in your code before you
	 *            call this method!
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
	 * @param supportVector The support vector of the new half-line instance
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
			return true;
		if (other == null || !(other instanceof IHalfLine))
			return false;
		
		final IHalfLine that = (IHalfLine) other;
		
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
	public boolean isPointInFront(final IVector2 point)
	{
		return isValid() && LineMath.isPointInFront(this, point);
	}
	
	
	@Override
	public IVector2 closestPointOnLine(final IVector2 point)
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
	public Optional<IVector2> intersectLine(final ILine line)
	{
		return line.intersectHalfLine(this);
	}
	
	
	@Override
	public Optional<IVector2> intersectHalfLine(final IHalfLine other)
	{
		if (this.isValid() && other.isValid())
		{
			return LineMath.intersectionPointOfHalfLines(this, other);
		}
		return Optional.empty();
	}
	
	
	@Override
	public Optional<IVector2> intersectSegment(final ILineSegment segment)
	{
		if (this.isValid() && segment.isValid())
		{
			return LineMath.intersectionPointOfHalfLineAndSegment(this, segment);
		}
		return Optional.empty();
	}
}
