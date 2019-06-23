/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.function.Function;

import com.sleepycat.persist.model.Persistent;


/**
 * Mutable 3-dimensional vector
 * 
 * @see Vector2
 * @see Vector2f
 * @see Vector3f
 * @see IVector2
 * @see IVector3
 * @author Gero
 * @author Andre
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class Vector3 extends AVector3
{
	private double	x;
	private double	y;
	private double	z;
	
	
	@SuppressWarnings("unused")
	private Vector3()
	{
		x = 0;
		y = 0;
		z = 0;
	}
	
	
	private Vector3(final double x, final double y, final double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	private Vector3(final IVector original)
	{
		set(original);
	}
	
	
	/**
	 * Parse a string to a vector. See {@link AVector#valueOf(String)} for more details.
	 *
	 * @param value a string with three comma separated values
	 * @return a new 3d vector based on the string
	 * @see AVector#valueOf(String)
	 */
	public static IVector3 valueOf(final String value)
	{
		return AVector.valueOf(value).getXYZVector();
	}
	
	
	/**
	 * Return a new zero vector instance
	 * 
	 * @return a new instance of a zero vector
	 */
	public static Vector3 zero()
	{
		return new Vector3();
	}
	
	
	/**
	 * @param x value
	 * @param y value
	 * @param z value
	 * @return new instance
	 */
	public static Vector3 fromXYZ(final double x, final double y, final double z)
	{
		return new Vector3(x, y, z);
	}
	
	
	/**
	 * @param x value
	 * @param y value
	 * @return new instance
	 */
	public static Vector3 fromXY(final double x, final double y)
	{
		return new Vector3(x, y, 0);
	}
	
	
	/**
	 * @param xy value
	 * @param z value
	 * @return new instance
	 */
	public static Vector3 from2d(final IVector2 xy, final double z)
	{
		return new Vector3(xy.x(), xy.y(), z);
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 *
	 * @param original hard copy of the original vector
	 * @return new instance
	 */
	public static Vector3 copy(final IVector original)
	{
		return new Vector3(original);
	}
	
	
	/**
	 * @param arr an array with 3 elements
	 * @return a new vector based on the values in the array
	 * @throws IllegalArgumentException if arr has wrong size
	 */
	public static Vector3 fromArray(final double[] arr)
	{
		if (arr.length != 3)
		{
			throw new IllegalArgumentException("Invalid input size");
		}
		return new Vector3(arr[0], arr[1], arr[2]);
	}
	
	
	/**
	 * Create a 3D pos from a projected ground pos.<br>
	 * This method basically traces back the ray from <code>ground</code> to <code>origin</code> until it reaches
	 * <code>height</code>.
	 * 
	 * @param origin 3D origin of the projection.
	 * @param ground 2D position on the ground.
	 * @param height Desired height.
	 * @return
	 */
	public static Vector3 fromProjection(final IVector3 origin, final IVector2 ground, final double height)
	{
		double scale = (origin.z() - height) / origin.z();
		return new Vector3(((ground.x() - origin.x()) * scale) + origin.x(),
				((ground.y() - origin.y()) * scale) + origin.y(), height);
	}
	
	
	@Override
	public IVector3 copy()
	{
		return Vector3.copy(this);
	}
	
	
	/**
	 * @param xy value
	 * @param z value
	 */
	public void set(final IVector2 xy, final double z)
	{
		x = xy.x();
		y = xy.y();
		this.z = z;
	}
	
	
	/**
	 * @param xy value
	 */
	public void set(final IVector xy)
	{
		x = xy.x();
		y = xy.y();
		z = xy.z();
	}
	
	
	/**
	 * @param xy value
	 */
	public void setXY(final IVector xy)
	{
		x = xy.x();
		y = xy.y();
	}
	
	
	/**
	 * @param i index
	 * @param value new value
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
	 * @param vector other vector
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
	 * @param vector other vector
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
	 * @param function a function to apply to each element of this vector
	 * @return this
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
