/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.08.2012
 * Author(s): Gero, AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.util.function.Function;

import com.sleepycat.persist.model.Persistent;


/**
 * Simple data holder for position data
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see Vector2
 * @author Gero, AndreR
 */
@Persistent
public class Vector3 extends AVector3
{
	private double	x;
	private double	y;
	private double	z;
						
						
	/** */
	public Vector3()
	{
		x = 0;
		y = 0;
		z = 0;
	}
	
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3(final double x, final double y, final double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param xy
	 * @param z
	 */
	public Vector3(final IVector2 xy, final double z)
	{
		set(xy, z);
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param original
	 */
	public Vector3(final IVector original)
	{
		set(original);
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param arr
	 */
	public Vector3(final double[] arr)
	{
		if (arr.length != 3)
		{
			throw new IllegalArgumentException("Invalid input size");
		}
		x = arr[0];
		y = arr[1];
		z = arr[2];
	}
	
	
	/**
	 * @param value
	 * @return
	 */
	public static IVector3 valueOf(final String value)
	{
		return AVector.valueOf(value).getXYZVector();
	}
	
	
	/**
	 * @param original
	 * @param z
	 */
	public void set(final IVector2 original, final double z)
	{
		x = original.x();
		y = original.y();
		this.z = z;
	}
	
	
	/**
	 * @param original
	 */
	public void set(final IVector original)
	{
		x = original.x();
		y = original.y();
		z = original.z();
	}
	
	
	/**
	 * @param i
	 * @param value
	 */
	public void set(final int i, final double value)
	{
		switch (i)
		{
			case 0:
				x = value;
				break;
			case 1:
				y = value;
				break;
			case 2:
				z = value;
				break;
			default:
				throw new IllegalArgumentException("Invalid index: " + i);
		}
	}
	
	
	/**
	 * Adds the given 'vector' to 'this'
	 * 
	 * @param vector
	 * @return Added vector.
	 */
	public Vector3 add(final IVector vector)
	{
		x += vector.x();
		y += vector.y();
		z += vector.z();
		return this;
	}
	
	
	/**
	 * Subtracts the given 'vector' from 'this'
	 * 
	 * @param vector
	 * @return Subtracted vector.
	 */
	public Vector3 subtract(final IVector vector)
	{
		x -= vector.x();
		y -= vector.y();
		z -= vector.z();
		return this;
	}
	
	
	/**
	 * Multiply this vector with f
	 * 
	 * @param f factor
	 * @return this
	 */
	public Vector3 multiply(final double f)
	{
		x *= f;
		y *= f;
		z *= f;
		
		return this;
	}
	
	
	/**
	 * Multiply this vector with f
	 * 
	 * @param vec
	 * @return this
	 */
	public Vector3 mutiply(final IVector vec)
	{
		x *= vec.x();
		y *= vec.y();
		z *= vec.z();
		
		return this;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param function
	 * @return
	 */
	public Vector3 apply(final Function<Double, Double> function)
	{
		x = function.apply(x);
		y = function.apply(y);
		z = function.apply(z);
		return this;
	}
	
	
	@Override
	public Vector3 getXYZVector()
	{
		return this;
	}
	
	
	@Override
	public double x()
	{
		return x;
	}
	
	
	@Override
	public double y()
	{
		return y;
	}
	
	
	@Override
	public double z()
	{
		return z;
	}
}
