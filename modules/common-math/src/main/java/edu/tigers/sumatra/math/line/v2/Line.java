/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import java.util.Optional;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Implementation of the {@link ILine} interface. It provides multiple factory methods to create new instances.
 * 
 * @author Lukas Magel
 */
final class Line extends AUnboundedLine implements ILine
{
	
	private Line(final IVector2 supportVector, final IVector2 directionVector)
	{
		super(supportVector, directionVector);
	}
	
	
	/**
	 * Creates supportPointA new line instance which runs through the two specified points {@code supportPointA} and
	 * {@code supportPointB}. The direction vector of the created line points from {@code supportPointA} to
	 * {@code supportPointB}.
	 *
	 * @param supportPointA
	 *           The first point to define the line
	 * @param supportPointB
	 *           The seconds point to define the line
	 * @return
	 * 			A new line instance which runs through both points
	 * @throws IllegalArgumentException
	 *            If the support points are identical, i.e. {@code supportPointA.equals(supportPointB}
	 */
	public static ILine fromPoints(final IVector2 supportPointA, final IVector2 supportPointB)
	{
		return createNewFromVectorCopy(supportPointA, supportPointB.subtractNew(supportPointA));
	}
	
	
	/**
	 * Creates a new line instance which uses the specified {@code supportVector} and {@code directionVector}.
	 *
	 * @param supportVector
	 *           The support vector to use for the line
	 * @param directionVector
	 *           The direction vector to use for the line
	 * @return
	 * 			A new line instance which is defined by the two parameters
	 * @throws IllegalArgumentException
	 *            If the {@code directionVector} has a length of zero. Please perform a check in your code before you
	 *            call this method!
	 */
	public static ILine fromDirection(final IVector2 supportVector, final IVector2 directionVector)
	{
		return createNewFromVectorCopy(supportVector, directionVector);
	}
	
	
	/**
	 * Creates a new instance with the supplied support and direction vector. The direction vector is normalized.
	 * 
	 * @param supportVector
	 *           Line support vector
	 * @param directionVector
	 *           Line direction vector
	 * @return
	 * 			A new line instance
	 * @throws IllegalArgumentException
	 *            If the {@code directionVector} has a length of zero. Please perform a check in your code before you
	 *            call this method!
	 */
	static ILine createNewFromVectorCopy(final IVector2 supportVector, final IVector2 directionVector)
	{
		Vector2 sV = Vector2.copy(supportVector);
		Vector2 dV = directionVector.normalizeNew();
		
		if (!dV.isZeroVector() && dV.getAngle() < 0.0d)
		{
			dV.multiply(-1.0d);
		}
		
		return new Line(sV, dV);
	}
	
	
	/**
	 * Creates a new instance with the supplied support and direction vector. The provided vectors are not copied. The
	 * direction vector is expected to be normalized.
	 *
	 * @param supportVector
	 *           Line support vector
	 * @param directionVector
	 *           Line direction vector, normalized
	 * @return
	 * 			A new line instance
	 * @throws IllegalArgumentException
	 *            If the {@code directionVector} has a length of zero. Please perform a check in your code before you
	 *            call this method!
	 */
	static ILine createNewWithoutCopy(final IVector2 supportVector, final IVector2 directionVector)
	{
		IVector2 dV = directionVector;
		if (dV.getAngle(0.0d) < 0.0d)
		{
			dV = dV.multiplyNew(-1.0d);
		}
		
		return new Line(supportVector, dV);
	}
	
	
	private static boolean equalsForInvalid(final ILine a, final ILine b)
	{
		return !a.isValid() && !b.isValid() && a.supportVector().equals(b.supportVector());
	}
	
	
	private static boolean equalsForValid(final ILine a, final ILine b)
	{
		if (!a.directionVector().equals(b.directionVector()))
		{
			return false;
		}
		
		IVector2 supportVectorDifference = b.supportVector().subtractNew(a.supportVector());
		return supportVectorDifference.isParallelTo(a.directionVector());
	}
	
	
	@Override
	public ILine copy()
	{
		IVector2 supportVector = supportVector();
		IVector2 directionVector = directionVector();
		return createNewFromVectorCopy(supportVector, directionVector);
	}
	
	
	@Override
	public final boolean equals(final Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other == null || !(other instanceof ILine))
		{
			return false;
		}
		
		final ILine that = (ILine) other;
		
		if (this.isValid() && that.isValid())
		{
			return equalsForValid(this, that);
		}
		return equalsForInvalid(this, that);
	}
	
	
	@Override
	public final int hashCode()
	{
		/*
		 * This is not an optimal solution as it does not consider the support vector for hash code creation. The issue
		 * with the support vector is that although the support vectors can be different, the instances can still
		 * represent the same logical lines. The correct way to handle this would be to find the y or x intercept of the
		 * line and use that for the hash code. For the sake of simplicity and since it does not break the equals/hashCode
		 * contract, only the direction vector is considered. Since the direction vector is supposed to be normalized, the
		 * hashCode method can be invoked on it directly.
		 */
		return directionVector().hashCode();
	}
	
	
	@Override
	public Optional<Double> getYIntercept()
	{
		return LineMath.getYIntercept(this);
	}
	
	
	@Override
	public Optional<Double> getXValue(final double y)
	{
		return LineMath.getXValue(this, y);
	}
	
	
	@Override
	public Optional<Double> getYValue(final double x)
	{
		return LineMath.getYValue(this, x);
	}
	
	
	@Override
	public ILine getOrthogonalLine()
	{
		IVector2 sV = supportVector();
		IVector2 dV = directionVector().turnNew(Math.PI / 2);
		return createNewFromVectorCopy(sV, dV);
	}
	
	
	@Override
	public IVector2 closestPointOnLine(final IVector2 point)
	{
		if (!isValid())
		{
			return supportVector();
		}
		return LineMath.closestPointOnLine(this, point);
	}
	
	
	@Override
	public ILine toLine()
	{
		return this;
	}
	
	
	@Override
	public Optional<IVector2> intersectLine(final ILine other)
	{
		if (this.isValid() && other.isValid())
		{
			return LineMath.intersectionPointOfLines(this, other);
		}
		return Optional.empty();
	}
	
	
	@Override
	public Optional<IVector2> intersectHalfLine(final IHalfLine halfLine)
	{
		if (this.isValid() && halfLine.isValid())
		{
			return LineMath.intersectionPointOfLineAndHalfLine(this, halfLine);
		}
		return Optional.empty();
	}
	
	
	@Override
	public Optional<IVector2> intersectSegment(final ILineSegment segment)
	{
		if (this.isValid() && segment.isValid())
		{
			return LineMath.intersectionPointOfLineAndSegment(this, segment);
		}
		return Optional.empty();
	}
	
}
