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

import org.apache.commons.math3.linear.RealVector;


/**
 * A vector with dynamic dimension. The {@link VectorN#set(int, double)} method resizes the vector if necessary!
 * This implementation is mutable and thread-safe
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorN extends AVectorN
{
	private final double[] data;
	
	
	/**
	 * Create zero vector with given initial dimension
	 * 
	 * @param dim
	 */
	public VectorN(final int dim)
	{
		data = new double[dim];
	}
	
	
	/**
	 * Create vector from data
	 * Note: data is not copied! Changes to the array will be reflected in this Vector!
	 * 
	 * @param data
	 */
	public VectorN(final double[] data)
	{
		assert data != null;
		this.data = data;
	}
	
	
	/**
	 * Deep copy
	 * 
	 * @param vector
	 */
	public VectorN(final IVector vector)
	{
		data = new double[vector.getNumDimensions()];
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			data[i] = vector.get(i);
		}
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param rv
	 */
	public VectorN(final RealVector rv)
	{
		data = new double[rv.getDimension()];
		for (int i = 0; i < rv.getDimension(); i++)
		{
			data[i] = rv.getEntry(i);
		}
	}
	
	
	/**
	 * Set value of an element
	 * 
	 * @param i
	 * @param value
	 */
	public void set(final int i, final double value)
	{
		data[i] = value;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param vec
	 * @return
	 */
	public VectorN add(final IVector vec)
	{
		for (int i = 0; i < getNumDimensions(); i++)
		{
			data[i] += vec.get(i);
		}
		return this;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param vec
	 * @return
	 */
	public VectorN sub(final IVector vec)
	{
		for (int i = 0; i < getNumDimensions(); i++)
		{
			data[i] -= vec.get(i);
		}
		return this;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param vec
	 * @return
	 */
	public VectorN multiply(final IVector vec)
	{
		for (int i = 0; i < getNumDimensions(); i++)
		{
			data[i] *= vec.get(i);
		}
		return this;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param factor
	 * @return
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
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param function
	 * @return
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
		return data[i];
	}
	
	
	@Override
	public int getNumDimensions()
	{
		return data.length;
	}
}
