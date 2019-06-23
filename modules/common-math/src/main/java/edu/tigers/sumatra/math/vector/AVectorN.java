/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.function.Function;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
abstract class AVectorN extends AVector implements IVectorN
{
	
	@Override
	public Vector2 getXYVector()
	{
		return Vector2.fromXY(x(), y());
	}
	
	
	@Override
	public Vector3 getXYZVector()
	{
		return Vector3.fromXYZ(x(), y(), get(2));
	}
	
	
	@Override
	public VectorN addNew(final IVectorN vector)
	{
		VectorN vec = VectorN.zero(getNumDimensions());
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			vec.set(i, get(i) + vector.get(i));
		}
		return vec;
	}
	
	
	@Override
	public VectorN subtractNew(final IVectorN vector)
	{
		VectorN vec = VectorN.zero(getNumDimensions());
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			vec.set(i, get(i) - vector.get(i));
		}
		return vec;
	}
	
	
	@Override
	public VectorN multiplyNew(final IVectorN vector)
	{
		VectorN vec = VectorN.zero(getNumDimensions());
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			vec.set(i, get(i) * vector.get(i));
		}
		return vec;
	}
	
	
	@Override
	public VectorN multiplyNew(final double f)
	{
		VectorN vec = VectorN.zero(getNumDimensions());
		for (int i = 0; i < getNumDimensions(); i++)
		{
			vec.set(i, get(i) * f);
		}
		return vec;
	}
	
	
	@Override
	public VectorN normalizeNew()
	{
		if (isZeroVector())
		{
			return VectorN.copy(this);
		}
		final double length = getLength();
		return applyNew(f -> f / length);
	}
	
	
	@Override
	public VectorN absNew()
	{
		return applyNew(Math::abs);
	}
	
	
	@Override
	public VectorN applyNew(final Function<Double, Double> function)
	{
		double[] data = new double[getNumDimensions()];
		for (int i = 0; i < getNumDimensions(); i++)
		{
			data[i] = function.apply(get(i));
		}
		return VectorN.from(data);
	}
}
