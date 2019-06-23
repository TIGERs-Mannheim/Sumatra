/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import com.sleepycat.persist.model.Persistent;


/**
 * Immutable 2-dimensional vector
 *
 * @see Vector2
 * @see Vector2f
 * @see Vector3f
 * @see IVector2
 * @see IVector3
 * @author Gero
 */
@Persistent
public class Vector2f extends AVector2
{
	private final double	x;
	private final double	y;
	
	
	private Vector2f()
	{
		x = 0;
		y = 0;
	}
	
	
	/**
	 * @param x value
	 * @param y value
	 */
	private Vector2f(final double x, final double y)
	{
		this.x = x;
		this.y = y;
	}
	
	
	/**
	 * @param x value
	 * @param y value
	 * @return new instance
	 */
	public static Vector2f fromXY(final double x, final double y)
	{
		return new Vector2f(x, y);
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 *
	 * @param xy original vector
	 * @return new instance
	 */
	public static Vector2f copy(final IVector2 xy)
	{
		return new Vector2f(xy.x(), xy.y());
	}
	
	
	/**
	 * Creates a final vector with (0, 0)
	 * 
	 * @return new instance
	 */
	public static Vector2f zero()
	{
		return new Vector2f();
	}
	
	
	@Override
	public IVector2 copy()
	{
		return Vector2f.copy(this);
	}
	
	
	@Override
	public Vector2 getXYVector()
	{
		return Vector2.copy(this);
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
}
