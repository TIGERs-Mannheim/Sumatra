/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import com.sleepycat.persist.model.Persistent;


/**
 * Immutable 3-dimensional vector
 *
 * @see Vector2
 * @see Vector2f
 * @see Vector3f
 * @see IVector2
 * @see IVector3
 * @author Gero
 */
@Persistent
public class Vector3f extends AVector3
{
	private final double	x;
	private final double	y;
	private final double	z;
	
	
	private Vector3f()
	{
		x = 0;
		y = 0;
		z = 0;
	}
	
	
	private Vector3f(final double x, final double y, final double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	/**
	 * @param x value
	 * @param y value
	 * @param z value
	 * @return new instance
	 */
	public static Vector3f fromXYZ(final double x, final double y, final double z)
	{
		return new Vector3f(x, y, z);
	}
	
	
	/**
	 * @param xy vector
	 * @param z value
	 * @return new instance
	 */
	public static Vector3f from2d(final IVector2 xy, final double z)
	{
		return new Vector3f(xy.x(), xy.y(), z);
	}
	
	
	/**
	 * @param original vector to copy
	 * @return new instance
	 */
	public static Vector3f copy(final IVector original)
	{
		if (original.getNumDimensions() == 2)
		{
			return new Vector3f(original.x(), original.y(), 0);
		}
		return new Vector3f(original.x(), original.y(), original.z());
	}
	
	
	/**
	 * Create new zero vector
	 * 
	 * @return new instance
	 */
	public static Vector3f zero()
	{
		return new Vector3f();
	}
	
	
	@Override
	public IVector3 copy()
	{
		return Vector3.copy(this);
	}
	
	
	@Override
	public Vector3 getXYZVector()
	{
		return Vector3.copy(this);
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
