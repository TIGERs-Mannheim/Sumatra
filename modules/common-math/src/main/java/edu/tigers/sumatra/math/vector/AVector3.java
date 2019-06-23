/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.function.Function;

import com.sleepycat.persist.model.Persistent;


/**
 * This base class allows transparent implementation of immutable math functions for {@link Vector3} and
 * {@link Vector3f}
 * 
 * @see Vector3
 * @see Vector3f
 * @author AndreR
 */
@Persistent(version = 1)
public abstract class AVector3 extends AVector implements IVector3
{
	/** Vector3f(1,0,0) */
	public static final Vector3f	X_AXIS		= Vector3f.fromXYZ(1, 0, 0);
	/** Vector3f(0,1,0) */
	public static final Vector3f	Y_AXIS		= Vector3f.fromXYZ(0, 1, 0);
	/** Vector3f(0,0,1) */
	public static final Vector3f	Z_AXIS		= Vector3f.fromXYZ(0, 0, 1);
	/** Vector3f(0,0,0) */
	public static final Vector3f	ZERO_VECTOR	= Vector3f.fromXYZ(0, 0, 0);
	
	
	@Override
	public double get(final int i)
	{
		switch (i)
		{
			case 0:
				return x();
			case 1:
				return y();
			case 2:
				return z();
			default:
				throw new IllegalArgumentException("Invalid index: " + i);
		}
		
	}
	
	
	@Override
	public synchronized Vector3 addNew(final IVector vector)
	{
		Vector3 result = Vector3.copy(this);
		return result.add(vector);
	}
	
	
	@Override
	public synchronized Vector3 subtractNew(final IVector vector)
	{
		Vector3 result = Vector3.copy(this);
		return result.subtract(vector);
	}
	
	
	@Override
	public synchronized Vector3 multiplyNew(final double f)
	{
		return Vector3.fromXYZ(x() * f, y() * f, z() * f);
	}
	
	
	@Override
	public synchronized Vector3 multiplyNew(final IVector vector)
	{
		return Vector3.fromXYZ(
				x() * vector.x(),
				y() * vector.y(),
				z() * vector.z());
	}
	
	
	@Override
	public synchronized Vector3 absNew()
	{
		return applyNew(Math::abs);
	}
	
	
	@Override
	public synchronized Vector3 normalizeNew()
	{
		if (isZeroVector())
		{
			return Vector3.copy(this);
		}
		final double length = getLength();
		return Vector3.fromXYZ(x() / length, y() / length, z() / length);
	}
	
	
	@Override
	public synchronized Vector3 applyNew(final Function<Double, Double> function)
	{
		return Vector3.fromXYZ(
				function.apply(x()),
				function.apply(y()),
				function.apply(z()));
	}
	
	
	@Override
	public synchronized Vector2 projectToGroundNew(final IVector3 origin)
	{
		double scale = origin.z() / (origin.z() - z());
		return Vector2.fromXY(((x() - origin.x()) * scale) + origin.x(), ((y() - origin.y()) * scale) + origin.y());
	}
	
	
	@Override
	public Vector2 getXYVector()
	{
		return Vector2.fromXY(x(), y());
	}
}
