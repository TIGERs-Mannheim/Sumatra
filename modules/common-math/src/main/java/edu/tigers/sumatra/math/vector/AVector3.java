/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.function.Function;


/**
 * This base class allows transparent implementation of immutable math functions for {@link Vector3} and
 * {@link Vector3f}
 *
 * @author AndreR
 * @see Vector3
 * @see Vector3f
 */
public abstract class AVector3 extends AVector implements IVector3
{
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
	public double getLengthSqr()
	{
		return x() * x() + y() * y() + z() * z();
	}


	@Override
	public Vector3 addNew(final IVector3 vector)
	{
		Vector3 result = Vector3.copy(this);
		return result.add(vector);
	}


	@Override
	public Vector3 subtractNew(final IVector3 vector)
	{
		Vector3 result = Vector3.copy(this);
		return result.subtract(vector);
	}


	@Override
	public Vector3 multiplyNew(final double f)
	{
		return Vector3.fromXYZ(x() * f, y() * f, z() * f);
	}


	@Override
	public Vector3 multiplyNew(final IVector3 vector)
	{
		return Vector3.fromXYZ(
				x() * vector.x(),
				y() * vector.y(),
				z() * vector.z());
	}


	@Override
	public Vector3 absNew()
	{
		return applyNew(Math::abs);
	}


	@Override
	public Vector3 normalizeNew()
	{
		if (isZeroVector())
		{
			return Vector3.copy(this);
		}
		final double length = getLength();
		return Vector3.fromXYZ(x() / length, y() / length, z() / length);
	}


	@Override
	public Vector3 applyNew(final Function<Double, Double> function)
	{
		return Vector3.fromXYZ(
				function.apply(x()),
				function.apply(y()),
				function.apply(z()));
	}


	@Override
	public Vector2 projectToGroundNew(final IVector3 origin)
	{
		double scale = origin.z() / (origin.z() - z());
		return Vector2.fromXY(((x() - origin.x()) * scale) + origin.x(), ((y() - origin.y()) * scale) + origin.y());
	}


	@Override
	public double dotNew(final IVector3 vector)
	{
		return (x() * vector.x()) + (y() * vector.y()) + (z() * vector.z());
	}


	@Override
	public IVector3 crossNew(final IVector3 vector)
	{
		double x = (y() * vector.z()) - (z() * vector.y());
		double y = (z() * vector.x()) - (x() * vector.z());
		double z = (x() * vector.y()) - (y() * vector.x());

		return Vector3.fromXYZ(x, y, z);
	}


	@Override
	public Vector2 getXYVector()
	{
		return Vector2.fromXY(x(), y());
	}
}
