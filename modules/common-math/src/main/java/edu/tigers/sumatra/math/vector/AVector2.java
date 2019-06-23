/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;


/**
 * This base class allows transparent implementation of immutable math functions for {@link Vector2} and
 * {@link Vector2f}
 * 
 * @see Vector2
 * @see Vector2f
 * @author Gero
 */
@Persistent(version = 1)
public abstract class AVector2 extends AVector implements IVector2
{
	/** Vector2f(1,0) */
	public static final IVector2 X_AXIS = Vector2f.fromXY(1, 0);
	/** Vector2f(0,1) */
	public static final IVector2 Y_AXIS = Vector2f.fromXY(0, 1);
	/** Vector2f(0,0) */
	public static final IVector2 ZERO_VECTOR = Vector2f.fromXY(0, 0);
	
	
	@Override
	public double z()
	{
		throw new IllegalAccessError("Vector has no z-part!");
	}
	
	
	/**
	 * The String must be a pair of comma or space separated double values.
	 * Additional spaces are considered
	 * 
	 * @param value
	 * @return
	 * @throws NumberFormatException
	 */
	public static IVector2 valueOf(final String value)
	{
		return Vector2.copy(AVector.valueOf(value));
	}
	
	
	@Override
	public double get(final int i)
	{
		switch (i)
		{
			case 0:
				return x();
			case 1:
				return y();
			default:
				throw new IllegalArgumentException("Invalid index: " + i);
		}
		
	}
	
	
	@Override
	public synchronized double getAngle()
	{
		return VectorMath.getAngle(this);
	}
	
	
	@Override
	public synchronized double getAngle(final double defAngle)
	{
		if (isZeroVector())
		{
			return defAngle;
		}
		return getAngle();
	}
	
	
	@Override
	public synchronized Vector2 addNew(final IVector vector)
	{
		final Vector2 result = Vector2.copy(this);
		return result.add(vector);
	}
	
	
	@Override
	public synchronized Vector2 subtractNew(final IVector vector)
	{
		final Vector2 result = Vector2.copy(this);
		return result.subtract(vector);
	}
	
	
	@Override
	public synchronized Vector2 multiplyNew(final double factor)
	{
		final Vector2 result = Vector2.zero();
		result.setX(x() * factor);
		result.setY(y() * factor);
		
		return result;
	}
	
	
	@Override
	public synchronized Vector2 multiplyNew(final IVector vector)
	{
		return Vector2.fromXY(
				x() * vector.x(),
				y() * vector.y());
	}
	
	
	@Override
	public synchronized Vector2 scaleToNew(final double newLength)
	{
		final double oldLength = getLength2();
		if (!SumatraMath.isZero(oldLength))
		{
			return multiplyNew(newLength / oldLength);
		}
		// You tried to scale a null-vector to a non-zero length! Vector stays unaffected.
		// but this is normal Math. if vector is zero, result is zero too
		return Vector2.copy(this);
	}
	
	
	@Override
	public synchronized Vector2 turnNew(final double angle)
	{
		return Vector2.fromXY(
				(x() * AngleMath.cos(angle)) - (y() * AngleMath.sin(angle)),
				(y() * AngleMath.cos(angle)) + (x() * AngleMath.sin(angle)));
	}
	
	
	@Override
	public synchronized Vector2 applyNew(final Function<Double, Double> function)
	{
		return Vector2.fromXY(
				function.apply(x()),
				function.apply(y()));
	}
	
	
	@Override
	public synchronized double scalarProduct(final IVector2 v)
	{
		return (x() * v.x()) + (y() * v.y());
	}
	
	
	@Override
	public synchronized Vector2 turnToNew(final double angle)
	{
		final double len = getLength2();
		final double yn = AngleMath.sin(angle) * len;
		final double xn = AngleMath.cos(angle) * len;
		return Vector2.fromXY(xn, yn);
	}
	
	
	@Override
	public synchronized Vector2 normalizeNew()
	{
		if (isZeroVector())
		{
			return Vector2.copy(this);
		}
		final double length = getLength2();
		return Vector2.fromXY(x() / length, y() / length);
	}
	
	
	@Override
	public synchronized Vector2 absNew()
	{
		return applyNew(Math::abs);
	}
	
	
	@Override
	public synchronized Vector2 getNormalVector()
	{
		return Vector2.fromXY(y(), -x());
	}
	
	
	@Override
	public synchronized boolean isVertical()
	{
		return SumatraMath.isZero(x()) && !SumatraMath.isZero(y());
	}
	
	
	@Override
	public synchronized boolean isHorizontal()
	{
		return SumatraMath.isZero(y()) && !SumatraMath.isZero(x());
	}
	
	
	@Override
	public Vector3 getXYZVector()
	{
		return Vector3.from2d(this, 0);
	}
	
	
	@Override
	public double distanceTo(final IVector2 point)
	{
		return VectorMath.distancePP(this, point);
	}
	
	
	@Override
	public double distanceToSqr(IVector2 point)
	{
		return VectorMath.distancePPSqr(this, point);
	}
	
	
	@Override
	public boolean isParallelTo(final IVector2 vector)
	{
		return SumatraMath.isZero((x() * vector.y()) - (vector.x() * y()));
	}
	
	
	@Override
	public Optional<Double> angleTo(IVector2 toVector)
	{
		return VectorMath.angleDifference(this, toVector);
	}
	
	
	@Override
	public Optional<Double> angleToAbs(IVector2 toVector)
	{
		return angleTo(toVector).map(Math::abs);
	}
	
	
	@Override
	public IVector2 nearestTo(Collection<IVector2> points)
	{
		return VectorMath.nearestTo(this, points);
	}
	
	
	@Override
	public Optional<IVector2> nearestToOpt(Collection<IVector2> points)
	{
		if (points.isEmpty())
		{
			return Optional.empty();
		}
		return Optional.of(nearestTo(points));
	}
	
	
	@Override
	public IVector2 nearestTo(IVector2... points)
	{
		return VectorMath.nearestTo(this, Arrays.asList(points));
	}
	
	
	@Override
	public IVector2 farthestTo(Collection<IVector2> points)
	{
		return VectorMath.farthestTo(this, points);
	}
	
	
	@Override
	public Optional<IVector2> farthestToOpt(Collection<IVector2> points)
	{
		if (points.isEmpty())
		{
			return Optional.empty();
		}
		return Optional.of(farthestTo(points));
	}
	
	
	@Override
	public IVector2 farthestTo(IVector2... points)
	{
		return VectorMath.farthestTo(this, Arrays.asList(points));
	}
}
