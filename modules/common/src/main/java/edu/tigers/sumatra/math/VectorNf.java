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


/**
 * Vector with fixed dimension
 * This implementation is immutable and thread-safe.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorNf extends AVectorN
{
	private final double[] data;
	
	
	/**
	 * Create zero vector with given initial dimension
	 * 
	 * @param dim
	 */
	public VectorNf(final int dim)
	{
		data = new double[dim];
	}
	
	
	/**
	 * Create vector and initialize with data
	 * 
	 * @param data
	 */
	public VectorNf(final double... data)
	{
		this.data = Arrays.copyOf(data, data.length);
	}
	
	
	/**
	 * Deep copy
	 * 
	 * @param vector
	 */
	public VectorNf(final IVector vector)
	{
		data = new double[vector.getNumDimensions()];
		for (int i = 0; i < vector.getNumDimensions(); i++)
		{
			data[i] = vector.get(i);
		}
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
