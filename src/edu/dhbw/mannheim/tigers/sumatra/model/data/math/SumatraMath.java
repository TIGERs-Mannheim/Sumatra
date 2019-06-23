/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import org.apache.log4j.Logger;


/**
 * This class holds math-functions often used in the AI Module.<br>
 * 
 * If you change something here or have ideas for new implementations please contact the owner.
 * If you want to add more functionality, focus on clear documentation (like in the already existing methods) and leave
 * your name!
 * 
 * @author Malte
 */
public final class SumatraMath
{
	// Logger
	private static final Logger	log				= Logger.getLogger(SumatraMath.class.getName());
	
	private static final float		EPS				= 0.00001f;
	private static final float		EQUAL_TOL		= 0.000001f;
	
	// --------------------------------------------------------------------------
	// --- Factorial ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * The maximum number for which the function {@link #faculty(int)} can return a result via lookup-array (
	 * {@link #FACTORIALS})!
	 */
	private static final int		FACTORIAL_MAX	= 10;
	private static final long[]	FACTORIALS		= new long[FACTORIAL_MAX + 1];
	
	
	// Static initialization of the Lookup-array
	static
	{
		long n = 1;
		FACTORIALS[0] = n;
		for (int i = 1; i <= FACTORIAL_MAX; i++)
		{
			n *= i;
			FACTORIALS[i] = n;
		}
	}
	
	
	private SumatraMath()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- Basic conversion functions -------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This is not inaccurate or fast. It just converts the result to float!
	 * 
	 * @author Malte
	 * @param number
	 * @return
	 */
	public static float sqrt(float number)
	{
		return (float) Math.sqrt(number);
	}
	
	
	/**
	 * @param exponent The exponent
	 * @return (float) {@link SumatraMath#exp(float)}
	 * 
	 * @author Gero
	 */
	public static float exp(float exponent)
	{
		return (float) Math.exp(exponent);
	}
	
	
	/**
	 * Checks, if x is almost 0 (within the epsilon environment).<br>
	 * -epsilon < x < epsilon
	 * @param x
	 * @param epsilon
	 * 
	 * @author GuntherB
	 * @return
	 */
	public static boolean isZero(float x, float epsilon)
	{
		if ((x > -epsilon) && (x < epsilon))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * 
	 * Checks, if x is almost 0 (within the epsilon environment).<br>
	 * -EPSILON < x < EPSILON <br>
	 * EPSILON = 0.00001f;
	 * @author GuntherB
	 * @param x
	 * @return
	 */
	public static boolean isZero(float x)
	{
		return isZero(x, EPS);
	}
	
	
	/**
	 * this method returns the faculty of an int.
	 * @param n
	 * @author Malte, Gero
	 * @return
	 */
	public static long faculty(int n)
	{
		if (n > FACTORIAL_MAX)
		{
			log.error("AIMath.faculty is limited to FACTORIAL_MAX; if you need more, change it! ;-)");
			return -1;
		} else if (n < 0)
		{
			log.error("AIMath.faculty: Can't calculate faculty of a negative number!");
			return -1;
		} else
		{
			return FACTORIALS[n];
		}
	}
	
	
	/**
	 * gets the sign of a float
	 * @param f
	 * @return -1 if f is negative; 1 else
	 * @author DanielW
	 */
	public static float sign(float f)
	{
		return f < 0 ? -1 : 1;
	}
	
	
	/**
	 * Returns true if the given number is positive or zero. Else: false
	 * @param f
	 * @author Malte
	 * @return
	 */
	public static boolean isPositive(float f)
	{
		return f >= 0 ? true : false;
	}
	
	
	/**
	 * Checks if all given values are the same.
	 * If list is empty, it's true!
	 * 
	 * @param values
	 * @author Malte
	 * @return
	 */
	public static boolean allTheSame(boolean... values)
	{
		if (values.length == 0)
		{
			return true;
		}
		final boolean ref = values[0];
		for (final boolean b : values)
		{
			if (b != ref)
			{
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * @param x
	 * @return x^2 (square of x)
	 * @author DanielW
	 */
	public static float square(float x)
	{
		return x * x;
	}
	
	
	/**
	 * @param x
	 * @return x^3 (cubic of x)
	 * @author DanielW
	 */
	public static float cubic(float x)
	{
		return x * x * x;
	}
	
	
	/**
	 * Returns the minimum float-value
	 * @return minimum value
	 * @author DionH
	 * @param values
	 */
	public static float min(float... values)
	{
		if (values.length == 0)
		{
			throw new IllegalArgumentException("No values");
		}
		
		float minimum = values[0];
		
		for (final float f : values)
		{
			if (f < minimum)
			{
				minimum = f;
			}
		}
		
		return minimum;
	}
	
	
	/**
	 * Returns the minimum float-value
	 * @return maximum value
	 * @author Frieder Berthold
	 * @param values
	 */
	public static float max(float... values)
	{
		if (values.length == 0)
		{
			throw new IllegalArgumentException("No values");
		}
		
		float maximum = values[0];
		
		for (final float f : values)
		{
			if (f > maximum)
			{
				maximum = f;
			}
		}
		
		return maximum;
	}
	
	
	/**
	 * Check if number has digits after decimal point.
	 * 
	 * @param number to check
	 * @return true when number has digits after decimal point
	 */
	public static boolean hasDigitsAfterDecimalPoint(float number)
	{
		final float numberInt = (float) Math.ceil(number);
		
		if (isEqual(number, numberInt))
		{
			return false;
		}
		return true;
	}
	
	
	/**
	 * Checks two float values for equality with a small tolerance value
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean isEqual(float a, float b)
	{
		return isEqual(a, b, EQUAL_TOL);
	}
	
	
	/**
	 * Checks two float values for equality with a small tolerance value
	 * 
	 * @param a
	 * @param b
	 * @param tolerance
	 * @return
	 */
	public static boolean isEqual(float a, float b, float tolerance)
	{
		return Math.abs(a - b) < tolerance;
	}
	
	
	/**
	 * Checks two double values for equality with a small tolerance value
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean isEqual(double a, double b)
	{
		return Math.abs(a - b) < EQUAL_TOL;
	}
	
}