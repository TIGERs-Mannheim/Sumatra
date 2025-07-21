/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import org.apache.commons.math3.linear.RealVector;

import java.util.function.Function;


/**
 * A vector with N dimensions. This class is mutable, but not thread-safe.<br>
 * The vector can be initialized with a zero dimension.
 * In this case, it will be resized on first vector-operation (add,sub).
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorN extends AVectorN
{
	private double[] data;
	
	
	private VectorN(final int dim)
	{
		data = new double[dim];
	}
	
	
	private VectorN(final double... data)
	{
		this.data = data;
	}
	
	
	private VectorN(final IVector vector)
	{
		data = new double[vector.getNumDimensions()];
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			data[i] = vector.get(i);
		}
	}
	
	
	/**
	 * Create zero vector with given initial dimension
	 *
	 * @param dim
	 * @return new instance
	 */
	public static VectorN zero(final int dim)
	{
		return new VectorN(dim);
	}
	
	
	/**
	 * Create an empty, uninitialized vector. Dimension will be resized on first operation with another vector.
	 *
	 * @return new instance
	 */
	public static VectorN empty()
	{
		return new VectorN(0);
	}
	
	
	/**
	 * Create vector from data
	 *
	 * @param data
	 * @return new instance
	 */
	public static VectorN from(final double... data)
	{
		if (data == null)
		{
			return VectorN.empty();
		}
		return new VectorN(data);
	}
	
	
	/**
	 * Deep copy
	 *
	 * @param vector
	 * @return new instance
	 */
	public static VectorN copy(final IVector vector)
	{
		return new VectorN(vector);
	}
	
	
	/**
	 * @param rv a real vector
	 * @return new instance
	 */
	public static VectorN fromReal(final RealVector rv)
	{
		VectorN vector = new VectorN(rv.getDimension());
		for (int i = 0; i < rv.getDimension(); i++)
		{
			vector.data[i] = rv.getEntry(i);
		}
		return vector;
	}
	
	
	/**
	 * Set value of an element
	 * 
	 * @param i index
	 * @param value value at index
	 */
	public void set(final int i, final double value)
	{
		if (i >= getNumDimensions())
		{
			throw new IllegalArgumentException("index too large: " + i);
		}
		data[i] = value;
	}
	
	
	private void checkDimension(int dim)
	{
		if (data.length == 0)
		{
			data = new double[dim];
		} else if (data.length != dim)
		{
			throw new IllegalArgumentException("Invalid dimension: " + dim + " != " + data.length);
		}
	}
	
	
	/**
	 * @param vector to add
	 * @return this
	 */
	public VectorN add(final IVector vector)
	{
		checkDimension(vector.getNumDimensions());
		for (int i = 0; i < getNumDimensions(); i++)
		{
			data[i] += vector.get(i);
		}
		return this;
	}
	
	
	/**
	 * @param vector to subtract
	 * @return this
	 */
	public VectorN subtract(final IVector vector)
	{
		checkDimension(vector.getNumDimensions());
		for (int i = 0; i < getNumDimensions(); i++)
		{
			data[i] -= vector.get(i);
		}
		return this;
	}
	
	
	/**
	 * @param factor to multiply
	 * @return this
	 */
	public VectorN multiply(final double factor)
	{
		for (int i = 0; i < getNumDimensions(); i++)
		{
			data[i] *= factor;
		}
		return this;
	}
	
	
	/**
	 * @param function to apply to each element in this vector
	 * @return this
	 */
	public VectorN apply(final Function<Double, Double> function)
	{
		for (int i = 0; i < getNumDimensions(); i++)
		{
			data[i] = function.apply(get(i));
		}
		return this;
	}
	
	
	@Override
	public double x()
	{
		return data[0];
	}
	
	
	@Override
	public double y()
	{
		return data[1];
	}
	
	
	@Override
	public double get(final int i)
	{
		if (i >= getNumDimensions())
		{
			throw new IllegalArgumentException("index too large: " + i);
		}
		return data[i];
	}
	
	
	@Override
	public int getNumDimensions()
	{
		if (data == null)
		{
			return 0;
		}
		return data.length;
	}
}
