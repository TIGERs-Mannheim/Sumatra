/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;


/**
 * Vector related calculations.
 * Please consider using the methods from {@link IVector}, {@link IVector2}, etc. instead of these static methods!
 *
 * @author nicolai.ommer
 */
public final class VectorMath
{
	@SuppressWarnings("unused")
	private VectorMath()
	{
	}
	
	
	/**
	 * Calculate the angle between x-axis and the vector
	 * 
	 * @param vector some non-zero vector
	 * @return angle between x-axis and vector in [-PI..PI], or 0 if vector is zero
	 */
	static double getAngle(IVector2 vector)
	{
		return SumatraMath.atan2(vector.y(), vector.x());
	}
	
	
	/**
	 * Calculate angle difference between both vectors, so that:
	 * <br>
	 * angle(fromVector) + angle = angle(toVector)
	 * <br>
	 * So the operation is: <br>
	 * angle(toVector) - angle(fromVector)
	 *
	 * @param fromVector first non-zero vector
	 * @param toVector second non-zero vector
	 * @return difference in [-PI..PI]
	 */
	static Optional<Double> angleDifference(IVector2 fromVector, IVector2 toVector)
	{
		if (toVector.isZeroVector() || fromVector.isZeroVector())
		{
			return Optional.empty();
		}
		return Optional.of(AngleMath.difference(toVector.getAngle(), fromVector.getAngle()));
	}
	
	
	/**
	 * Returns distance between two points
	 *
	 * @see IVector2#distanceTo(IVector2)
	 * @param a first point
	 * @param b second point
	 * @return euclidean distance
	 */
	public static double distancePP(final IVector2 a, final IVector2 b)
	{
		return a.subtractNew(b).getLength2();
	}
	
	
	/**
	 * Squared distance between too points
	 *
	 * @param a first point
	 * @param b secound point
	 * @return The squared distance between two points
	 */
	public static double distancePPSqr(final IVector2 a, final IVector2 b)
	{
		final double abX = a.x() - b.x();
		final double abY = a.y() - b.y();
		return (abX * abX) + (abY * abY);
	}
	
	
	/**
	 * Get the nearest point to p from the list
	 *
	 * @param p point to compare against
	 * @param points list of points to compare
	 * @return nearest point in list to point p
	 */
	static IVector2 nearestTo(final IVector2 p, final Collection<IVector2> points)
	{
		Validate.notEmpty(points);
		IVector2 closest = null;
		double closestDist = Double.MAX_VALUE;
		for (IVector2 vec : points)
		{
			double dist = distancePPSqr(vec, p);
			if (closestDist > dist)
			{
				closestDist = dist;
				closest = vec;
			}
		}
		Validate.notNull(closest);
		return closest;
	}
	
	
	/**
	 * Get the farthest point to p from the list
	 *
	 * @param p point to compare against
	 * @param points list of points to compare
	 * @return farthest point in list to point p
	 */
	static IVector2 farthestTo(final IVector2 p, final Collection<IVector2> points)
	{
		Validate.notEmpty(points);
		IVector2 farthest = null;
		double farthestDist = Double.MIN_VALUE;
		for (IVector2 vec : points)
		{
			double dist = distancePPSqr(vec, p);
			if (farthestDist < dist)
			{
				farthestDist = dist;
				farthest = vec;
			}
		}
		Validate.notNull(farthest);
		return farthest;
	}
}
