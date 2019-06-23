/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.util.Arrays;
import java.util.function.Function;


/**
 * A vector with dynamic dimension. The {@link VectorNd#set(int, double)} method resizes the vector if necessary!
 * This implementation is mutable and thread-safe
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorNd extends AVectorN
{
	private double[] data;
	
	
	/**
	 * Create an empty vector
	 */
	public VectorNd()
	{
		this(0);
	}
	
	
	/**
	 * Create zero vector with given initial dimension
	 * 
	 * @param dim
	 */
	public VectorNd(final int dim)
	{
		data = new double[dim];
	}
	
	
	/**
	 * Create vector from data
	 * Note: data is not copied! Changes to the array will be reflected in this Vector!
	 * 
	 * @param data
	 */
	public VectorNd(final double... data)
	{
		this.data = data;
	}
	
	
	/**
	 * Deep copy
	 * 
	 * @param vector
	 */
	public VectorNd(final IVector vector)
	{
		data = new double[vector.getNumDimensions()];
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			data[i] = vector.get(i);
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
		if (i >= getNumDimensions())
		{
			data = Arrays.copyOf(data, i + 1);
		}
		data[i] = value;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param vec
	 * @return
	 */
	public VectorNd add(final IVector vec)
	{
		for (int i = 0; i < Math.max(getNumDimensions(), vec.getNumDimensions()); i++)
		{
			set(i, get(i) + vec.get(i));
		}
		return this;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param vec
	 * @return
	 */
	public VectorNd sub(final IVector vec)
	{
		for (int i = 0; i < Math.max(getNumDimensions(), vec.getNumDimensions()); i++)
		{
			set(i, get(i) - vec.get(i));
		}
		return this;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param vec
	 * @return
	 */
	public VectorNd multiply(final IVector vec)
	{
		for (int i = 0; i < Math.max(getNumDimensions(), vec.getNumDimensions()); i++)
		{
			set(i, get(i) * vec.get(i));
		}
		return this;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param factor
	 * @return
	 */
	public VectorNd multiply(final double factor)
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
	public VectorNd apply(final Function<Double, Double> function)
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
		if (i < getNumDimensions())
		{
			return data[i];
		}
		return 0;
	}
	
	
	@Override
	public int getNumDimensions()
	{
		return data.length;
	}
}
