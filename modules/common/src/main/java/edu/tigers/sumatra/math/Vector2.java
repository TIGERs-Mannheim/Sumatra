/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.math;


import org.apache.commons.math3.linear.RealVector;

import com.sleepycat.persist.model.Persistent;


/**
 * Simple data holder for position data. Containing a X- and a Y-Coordinate.
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see Vector3
 * @author Gero
 */
@Persistent
public class Vector2 extends AVector2
{
	/** */
	private double	x;
	/** */
	private double	y;
						
						
	/**
	 * Creates a new vector with (0, 0)
	 * 
	 * @see Vector2
	 */
	public Vector2()
	{
		setX(0);
		setY(0);
	}
	
	
	/**
	 * Creates a new vector with given (x, y)
	 * 
	 * @param x
	 * @param y
	 * @see Vector2
	 */
	public Vector2(final double x, final double y)
	{
		setX(x);
		setY(y);
	}
	
	
	/**
	 * Creates a normalized vector that has the given angle, referring to the x-axis.
	 * 
	 * @param angle
	 */
	public Vector2(final double angle)
	{
		setX(1);
		setY(0);
		turnTo(angle);
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param original
	 */
	public Vector2(final IVector2 original)
	{
		set(original);
	}
	
	
	/**
	 * Create a vector from a RealVector (Apache Commons Math)
	 * 
	 * @author AndreR
	 * @param vector RealVector
	 * @note Passed double vector is casted to double!
	 */
	public Vector2(final RealVector vector)
	{
		setX(vector.getEntry(0));
		setY(vector.getEntry(1));
	}
	
	
	/**
	 * @param value
	 * @return
	 */
	public static IVector2 valueOf(final String value)
	{
		return AVector.valueOf(value).getXYVector();
	}
	
	
	/**
	 * s * Set x and y of this vector to the values of original
	 * 
	 * @param original
	 * @return
	 */
	public Vector2 set(final IVector2 original)
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
	 * Adds a given value to the x-value of the current vector.
	 * Returns the manipulated vector!
	 * 
	 * @param x
	 * @return
	 */
	public Vector2 addX(final double x)
	{
		setX(x() + x);
		return this;
	}
	
	
	/**
	 * Adds a given value to the y-value of the current vector.
	 * Returns the manipulated vector!
	 * 
	 * @param y
	 * @return
	 */
	public Vector2 addY(final double y)
	{
		setY(y() + y);
		return this;
	}
	
	
	/**
	 * Subtracts the given value from the x-value of the current vector.
	 * Returns the manipulated vector!
	 * 
	 * @param x
	 * @return
	 */
	public Vector2 subX(final double x)
	{
		setX(x() - x);
		return this;
	}
	
	
	/**
	 * Subtracts the given value from the y-value of the current vector.
	 * Returns the manipulated vector!
	 * 
	 * @param y
	 * @return
	 */
	public Vector2 subY(final double y)
	{
		setY(y() - y);
		return this;
	}
	
	
	/**
	 * Multiplys the given value with the x-value of the current vector.
	 * Returns the manipulated vector!
	 * 
	 * @param xFactor
	 * @return
	 */
	public Vector2 multX(final double xFactor)
	{
		setX(x() * xFactor);
		return this;
	}
	
	
	/**
	 * Multiplys the given value with the y-value of the current vector.
	 * Returns the manipulated vector!
	 * 
	 * @param yFactor
	 * @return
	 */
	public Vector2 multY(final double yFactor)
	{
		setY(y() * yFactor);
		return this;
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
		if (oldLength != 0)
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
		final double cosA = AngleMath.cos(angle);
		final double sinA = AngleMath.sin(angle);
		
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
		setY(AngleMath.sin(angle) * len);
		setX(AngleMath.cos(angle) * len);
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
