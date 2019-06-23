/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.util.function.Function;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AVectorN extends AVector implements IVectorN
{
	
	@Override
	public synchronized Vector2 getXYVector()
	{
		return new Vector2(x(), y());
	}
	
	
	@Override
	public synchronized Vector3 getXYZVector()
	{
		return new Vector3(x(), y(), z());
	}
	
	
	@Override
	public synchronized VectorN addNew(final IVector vector)
	{
		assert getNumDimensions() >= vector.getNumDimensions();
		VectorN vec = new VectorN(getNumDimensions());
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			vec.set(i, get(i) + vector.get(i));
		}
		return vec;
	}
	
	
	@Override
	public synchronized VectorN subtractNew(final IVector vector)
	{
		assert getNumDimensions() >= vector.getNumDimensions();
		VectorN vec = new VectorN(getNumDimensions());
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			vec.set(i, get(i) - vector.get(i));
		}
		return vec;
	}
	
	
	@Override
	public synchronized VectorN multiplyNew(final IVector vector)
	{
		assert getNumDimensions() >= vector.getNumDimensions();
		VectorN vec = new VectorN(getNumDimensions());
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			vec.set(i, get(i) * vector.get(i));
		}
		return vec;
	}
	
	
	@Override
	public synchronized VectorN multiplyNew(final double f)
	{
		VectorN vec = new VectorN(getNumDimensions());
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
			return new VectorN(this);
		}
		final double length = getLength();
		return applyNew(f -> f / length);
	}
	
	
	@Override
	public synchronized VectorN absNew()
	{
		return applyNew(f -> Math.abs(f));
	}
	
	
	@Override
	public synchronized VectorN applyNew(final Function<Double, Double> function)
	{
		double[] data = new double[getNumDimensions()];
		for (int i = 0; i < getNumDimensions(); i++)
		{
			data[i] = function.apply(get(i));
		}
		return new VectorN(data);
	}
}
