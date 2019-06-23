/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.function.Function;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
abstract class AVectorN extends AVector implements IVectorN
{
	
	@Override
	public synchronized Vector2 getXYVector()
	{
		return Vector2.fromXY(x(), y());
	}
	
	
	@Override
	public synchronized Vector3 getXYZVector()
	{
		return Vector3.fromXYZ(x(), y(), z());
	}
	
	
	@Override
	public synchronized VectorN addNew(final IVector vector)
	{
		VectorN vec = VectorN.zero(getNumDimensions());
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			vec.set(i, get(i) + vector.get(i));
		}
		return vec;
	}
	
	
	@Override
	public synchronized VectorN subtractNew(final IVector vector)
	{
		VectorN vec = VectorN.zero(getNumDimensions());
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			vec.set(i, get(i) - vector.get(i));
		}
		return vec;
	}
	
	
	@Override
	public synchronized VectorN multiplyNew(final IVector vector)
	{
		VectorN vec = VectorN.zero(getNumDimensions());
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			vec.set(i, get(i) * vector.get(i));
		}
		return vec;
	}
	
	
	@Override
	public synchronized VectorN multiplyNew(final double f)
	{
		VectorN vec = VectorN.zero(getNumDimensions());
		for (int i = 0; i < getNumDimensions(); i++)
		{
			vec.set(i, get(i) * f);
		}
		return vec;
	}
	
	
	@Override
	public synchronized VectorN normalizeNew()
	{
		if (isZeroVector())
		{
			return VectorN.copy(this);
		}
		final double length = getLength();
		return applyNew(f -> f / length);
	}
	
	
	@Override
	public synchronized VectorN absNew()
	{
		return applyNew(Math::abs);
	}
	
	
	@Override
	public synchronized VectorN applyNew(final Function<Double, Double> function)
	{
		double[] data = new double[getNumDimensions()];
		for (int i = 0; i < getNumDimensions(); i++)
		{
			data[i] = function.apply(get(i));
		}
		return VectorN.from(data);
	}
}
