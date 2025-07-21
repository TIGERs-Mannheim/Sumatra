/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;


import edu.tigers.sumatra.math.SumatraMath;
import org.apache.commons.math3.linear.RealVector;


/**
 * Mutable 2-dimensional vector
 *
 * @see Vector3
 * @see Vector2f
 * @see Vector3f
 * @see IVector2
 * @see IVector3
 * @author Gero
 */
public class Vector2 extends AVector2
{
	/** */
	private double x;
	/** */
	private double y;
	
	
	/**
	 * Creates a new vector with (0, 0)
	 */
	protected Vector2()
	{
		x = 0;
		y = 0;
	}
	
	
	/**
	 * Creates a new vector with given (x, y)
	 * 
	 * @param x
	 * @param y
	 */
	protected Vector2(final double x, final double y)
	{
		this.x = x;
		this.y = y;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param original
	 */
	protected Vector2(final IVector original)
	{
		x = original.x();
		y = original.y();
	}
	
	
	/**
	 * @return a new zero vector
	 */
	public static Vector2 zero()
	{
		return new Vector2();
	}
	
	
	/**
	 * @param x value for x
	 * @return new Vector2(x,0)
	 */
	public static Vector2 fromX(final double x)
	{
		return fromXY(x, 0);
	}
	
	
	/**
	 * @param y value for y
	 * @return new Vector2(0,y)
	 */
	public static Vector2 fromY(final double y)
	{
		return fromXY(0, y);
	}
	
	
	/**
	 * @param x value for x
	 * @param y value fro y
	 * @return new Vector2(x,y)
	 */
	public static Vector2 fromXY(final double x, final double y)
	{
		return new Vector2(x, y);
	}
	
	
	/**
	 * @param angle an angle
	 * @return new vector with given angle and length==1
	 */
	public static Vector2 fromAngle(final double angle)
	{
		final double yn = SumatraMath.sin(angle);
		final double xn = SumatraMath.cos(angle);
		return Vector2.fromXY(xn, yn);
	}
	
	
	/**
	 * @param angle an angle
	 * @param length the length
	 * @return new vector with given angle and given length
	 */
	public static Vector2 fromAngleLength(final double angle, final double length)
	{
		final double yn = SumatraMath.sin(angle) * length;
		final double xn = SumatraMath.cos(angle) * length;
		return Vector2.fromXY(xn, yn);
	}
	
	
	/**
	 * Create a direction vector from two points
	 *
	 * @param start first point
	 * @param end second point
	 * @return direction vector from start to end
	 */
	public static Vector2 fromPoints(final IVector2 start, final IVector2 end)
	{
		return end.subtractNew(start);
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 *
	 * @param original a vector to copy
	 * @return a new copy of the original
	 */
	public static Vector2 copy(final IVector original)
	{
		return new Vector2(original);
	}
	
	
	/**
	 * Create a vector from a {@link RealVector} (Apache Commons Math)
	 *
	 * @param vector RealVector
	 * @return a new vector based on the {@link RealVector}
	 */
	public static Vector2 fromReal(final RealVector vector)
	{
		return fromXY(vector.getEntry(0), vector.getEntry(1));
	}
	
	
	@Override
	public Vector2 copy()
	{
		return Vector2.copy(this);
	}
	
	
	/**
	 * s * Set x and y of this vector to the values of original
	 * 
	 * @param original
	 * @return
	 */
	public Vector2 set(final IVector original)
	{
		setX(original.x());
		setY(original.y());
		return this;
	}
	
	
	/**
	 * @param x
	 * @return
	 */
	public Vector2 setX(final double x)
	{
		this.x = x;
		return this;
	}
	
	
	/**
	 * @param y
	 * @return
	 */
	public Vector2 setY(final double y)
	{
		this.y = y;
		return this;
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
				setX(value);
				break;
			case 1:
				setY(value);
				break;
			default:
				throw new IllegalArgumentException("Invalid index: " + i);
		}
	}
	
	
	/**
	 * Adds the given 'vector' to 'this'
	 * 
	 * @param vector
	 * @return
	 */
	public Vector2 add(final IVector vector)
	{
		setX(x() + vector.x());
		setY(y() + vector.y());
		return this;
	}
	
	
	/**
	 * Subtracts the given 'vector' from 'this'
	 * 
	 * @param vector
	 * @return
	 */
	public Vector2 subtract(final IVector vector)
	{
		setX(x() - vector.x());
		setY(y() - vector.y());
		return this;
	}
	
	
	/**
	 * Multiplies 'this' by the given 'factor'
	 * 
	 * @param factor
	 * @return
	 */
	public Vector2 multiply(final double factor)
	{
		setX(x() * factor);
		setY(y() * factor);
		return this;
	}
	
	
	/**
	 * Sets the length of 'this' to the given 'length' <br>
	 * If 'this' is a zero vector, it stays always unaffected.
	 * 
	 * <pre>
	 * l
	 * ---- * v
	 * | v |
	 * 
	 * <pre>
	 * 
	 * @param newLength
	 * @author Malte
	 * @return
	 */
	public Vector2 scaleTo(final double newLength)
	{
		final double oldLength = getLength2();
		if (!SumatraMath.isZero(oldLength))
		{
			return multiply(newLength / oldLength);
		}
		return this;
	}
	
	
	/**
	 * Turns 'this' <b>with</b> the given 'angle'. The length of the vector remains the same.
	 * The angle is added anti-clockwise.
	 * 
	 * @param angle
	 * @author Malte
	 * @return
	 */
	public Vector2 turn(final double angle)
	{
		final double cosA = SumatraMath.cos(angle);
		final double sinA = SumatraMath.sin(angle);
		
		final double x2 = (x() * cosA) - (y() * sinA);
		final double y2 = (y() * cosA) + (x() * sinA);
		
		setX(x2);
		setY(y2);
		
		return this;
	}
	
	
	/**
	 * Turns 'this' <b>to</b> the given 'angle'. Angle is calculated between 'this' and the x-Axis.
	 * The length of the vector remains the same.
	 * 
	 * @param angle
	 * @author Malte
	 * @return
	 */
	public Vector2 turnTo(final double angle)
	{
		final double len = getLength2();
		setY(SumatraMath.sin(angle) * len);
		setX(SumatraMath.cos(angle) * len);
		return this;
	}
	
	
	/**
	 * Normalizes this and returns it as well.
	 * 
	 * @author Malte
	 * @return
	 */
	public Vector2 normalize()
	{
		if (!isZeroVector())
		{
			final double length = getLength2();
			x /= length;
			y /= length;
		}
		return this;
	}
	
	
	@Override
	public Vector2 getXYVector()
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
}
