/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;


import java.io.Serializable;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;


/**
 * Simple data holder for position data. Containing a X- and a Y-Coordinate.
 * 
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see Vector3
 * @author Gero
 * 
 */
public class Vector2 extends AVector2 implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -8795210822775094406L;
	

	public float					x;
	public float					y;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Creates a new vector with (0, 0)
	 * 
	 * @see Vector2
	 */
	public Vector2()
	{
		this.x = 0;
		this.y = 0;
	}
	

	/**
	 * Creates a new vector with given (x, y)
	 * 
	 * @param x
	 * @param y
	 * @see Vector2
	 */
	public Vector2(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Creates a normalized vector that has the given angle, referring to the x-axis.
	  * @param angle
	  */
	public Vector2(float angle)
	{
		this.x = 1;
		this.y = 0;
		this.turnTo(angle);
	}
	

	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public Vector2(IVector2 original)
	{
		set(original);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public Vector2 set(IVector2 original)
	{
		this.x = original.x();
		this.y = original.y();
		return this;
	}
	

	public Vector2 setX(float x)
	{
		this.x = x;
		return this;
	}
	

	public Vector2 setY(float y)
	{
		this.y = y;
		return this;
	}
	

	@Override
	public float x()
	{
		return x;
	}
	

	@Override
	public float y()
	{
		return y;
	}
	

	/**
	 * Adds a given value to the x-value of the current vector.
	 * Returns the manipulated vector!
	 * 
	 * @param x
	 */
	public Vector2 addX(float x)
	{
		this.x += x;
		return this;
	}
	

	/**
	 * Adds a given value to the y-value of the current vector.
	 * Returns the manipulated vector!
	 * @param y
	 */
	public Vector2 addY(float y)
	{
		this.y += y;
		return this;
	}
	

	@Override
	public String toString()
	{
		return "Vector2 (" + x + "," + y + ")";
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Adds the given 'vector' to 'this'
	 * 
	 * @param vector
	 */
	public Vector2 add(IVector2 vector)
	{
		this.x += vector.x();
		this.y += vector.y();
		return this;
	}
	

	/**
	 * Subtracts the given 'vector' from 'this'
	 * 
	 * @param vector
	 */
	public Vector2 subtract(IVector2 vector)
	{
		this.x -= vector.x();
		this.y -= vector.y();
		return this;
	}
	

	/**
	 * Multiplies 'this' by the given 'factor'
	 * 
	 * @param factor
	 */
	public Vector2 multiply(float factor)
	{
		this.x *= factor;
		this.y *= factor;
		return this;
	}
	

	/**
	 * Sets the length of 'this' to the given 'length' <br>
	 * If 'this' is a zero vector, it stays always unaffected.
	 * 
	 * <pre>
	 *   l      
	 * ----  *   v
	 * | v |
	 * 
	 * <pre>
	 * 
	 * @param newLength
	 * @author Malte
	 */
	public Vector2 scaleTo(float newLength)
	{
		float oldLength = getLength2();
		if (oldLength != 0)
		{
			return multiply(newLength / oldLength);
		} else
		{
			return this;
			// log.warn("You tried to scale a null-vector to a non-zero length! Vector stays unaffected.");
		}
	}
	

	/**
	 * Turns 'this' <b>with</b> the given 'angle'. The length of the vector remains the same.
	 * The angle is added anti-clockwise.
	 * 
	 * @param angle
	 * @author Malte
	 */
	public Vector2 turn(float angle)
	{
		final float cosA = AIMath.cos(angle);
		final float sinA = AIMath.sin(angle);
		
		final float x2 = this.x * cosA - this.y * sinA;
		final float y2 = this.y * cosA + this.x * sinA;
		
		this.x = x2;
		this.y = y2;
		
		return this;
	}
	

	/**
	 * Turns 'this' <b>to</b> the given 'angle'. Angle is calculated between 'this' and the x-Axis.
	 * The length of the vector remains the same.
	 * 
	 * @param angle
	 * @author Malte
	 */
	public Vector2 turnTo(float angle)
	{
		float len = this.getLength2();
		this.y = AIMath.sin(angle) * len;
		this.x = AIMath.cos(angle) * len;
		return this;
	}
	
	/**
	 * Normalizes this and returns it as well.
	 * 
	 * @author Malte 
	 */
	public Vector2 normalize()
	{
		set(normalizeNew());
		return this;		
	}
	
	/**
	 * @param digits Number of digits to round at (negative values will be ignored!)
	 * @return This (rounded by the number of digits)
	 * @see IVector2#roundNew(int)
	 * @author Gero
	 */
	public Vector2 round(int digits)
	{
		if (digits == 0)
		{
			x = Math.round(x());
			y = Math.round(y());
		} else if (digits > 0)
		{
			final int f = 10 * digits;
			x = Math.round(x() * f) / f;
			y = Math.round(y() * f) / f;
		}
		
		return this;
	}
	
	public void setSavedString(String savedString)
	{
		String splitChar = ";";
		
		if(savedString.contains(","))
			splitChar = ",";
		
		String floats[] = savedString.split(splitChar);
		if(floats.length < 2)
		{
			throw new IllegalArgumentException("Not enough floats in: " + savedString);
		}
		
		x = Float.parseFloat(floats[0]);
		y = Float.parseFloat(floats[1]);
	}
}
