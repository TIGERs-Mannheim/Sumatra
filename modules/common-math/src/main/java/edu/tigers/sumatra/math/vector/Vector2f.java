/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.SumatraMath;


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
public final class Vector2f extends AVector2
{
	/** Vector2f(1,0) */
	public static final Vector2f X_AXIS = fromXY(1, 0);
	/** Vector2f(0,1) */
	public static final Vector2f Y_AXIS = fromXY(0, 1);
	/** Vector2f(0,0) */
	public static final Vector2f ZERO_VECTOR = fromXY(0, 0);
	
	private final double x;
	private final double y;
	
	
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
	 * @param x value for x
	 * @return new Vector2(x,0)
	 */
	public static Vector2f fromX(final double x)
	{
		return fromXY(x, 0);
	}
	
	
	/**
	 * @param y value for y
	 * @return new Vector2(0,y)
	 */
	public static Vector2f fromY(final double y)
	{
		return fromXY(0, y);
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
	 * @param angle an angle
	 * @return new vector with given angle and length==1
	 */
	public static Vector2f fromAngle(final double angle)
	{
		final double yn = SumatraMath.sin(angle);
		final double xn = SumatraMath.cos(angle);
		return Vector2f.fromXY(xn, yn);
	}
	
	
	/**
	 * @param angle an angle
	 * @param length the length
	 * @return new vector with given angle and given length
	 */
	public static Vector2f fromAngleLength(final double angle, final double length)
	{
		final double yn = SumatraMath.sin(angle) * length;
		final double xn = SumatraMath.cos(angle) * length;
		return Vector2f.fromXY(xn, yn);
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
