/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import org.apache.log4j.Logger;


/**
 * This class holds math-functions often used in the AI Module.<br>
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
	public static float sqrt(final float number)
	{
		return (float) Math.sqrt(number);
	}
	
	
	/**
	 * @param exponent The exponent
	 * @return (float) {@link SumatraMath#exp(float)}
	 * @author Gero
	 */
	public static float exp(final float exponent)
	{
		return (float) Math.exp(exponent);
	}
	
	
	/**
	 * Checks, if x is almost 0 (within the epsilon environment).<br>
	 * -epsilon < x < epsilon
	 * 
	 * @param x
	 * @param epsilon
	 * @author GuntherB
	 * @return
	 */
	public static boolean isZero(final float x, final float epsilon)
	{
		if ((x > -epsilon) && (x < epsilon))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * Checks, if x is almost 0 (within the epsilon environment).<br>
	 * -EPSILON < x < EPSILON <br>
	 * EPSILON = 0.00001f;
	 * 
	 * @author GuntherB
	 * @param x
	 * @return
	 */
	public static boolean isZero(final float x)
	{
		return isZero(x, EPS);
	}
	
	
	/**
	 * this method returns the faculty of an int.
	 * 
	 * @param n
	 * @author Malte, Gero
	 * @return
	 */
	public static long faculty(final int n)
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
	 * 
	 * @param f
	 * @return -1 if f is negative; 1 else
	 * @author DanielW
	 */
	public static float sign(final float f)
	{
		return f < 0 ? -1 : 1;
	}
	
	
	/**
	 * Returns true if the given number is positive or zero. Else: false
	 * 
	 * @param f
	 * @author Malte
	 * @return
	 */
	public static boolean isPositive(final float f)
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
	public static boolean allTheSame(final boolean... values)
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
	public static float square(final float x)
	{
		return x * x;
	}
	
	
	/**
	 * @param x
	 * @return x^3 (cubic of x)
	 * @author DanielW
	 */
	public static float cubic(final float x)
	{
		return x * x * x;
	}
	
	
	/**
	 * Returns the minimum float-value
	 * 
	 * @return minimum value
	 * @author DionH
	 * @param values
	 */
	public static float min(final float... values)
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
	 * 
	 * @return maximum value
	 * @author Frieder Berthold
	 * @param values
	 */
	public static float max(final float... values)
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
	public static boolean hasDigitsAfterDecimalPoint(final float number)
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
	public static boolean isEqual(final float a, final float b)
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
	public static boolean isEqual(final float a, final float b, final float tolerance)
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
	public static boolean isEqual(final double a, final double b)
	{
		return Math.abs(a - b) < EQUAL_TOL;
	}
	
	
	/**
	 * Checks if is a Number between to values
	 * 
	 * @param x
	 * @param min
	 * @param max
	 * @return
	 */
	public static boolean isBetween(final double x, final double min, final double max)
	{
		boolean result;
		if (max > min)
		{
			result = (x >= min) && (x <= max);
		} else
		{
			result = (x >= max) && (x <= min);
		}
		
		return result;
	}
	
	
	/**
	 * Maps a value between in_min and in_max to a scale between out_min and out_max
	 * 
	 * @param x
	 * @param in_min
	 * @param in_max
	 * @param out_min
	 * @param out_max
	 * @return
	 */
	public static float map(float x, final float in_min, final float in_max, final float out_min, final float out_max)
	{
		if (x < in_min)
		{
			x = in_min;
		}
		
		if (x < in_min)
		{
			x = in_max;
		}
		
		return (((x - in_min) * (out_max - out_min)) / (in_max - in_min)) + out_min;
	}
	
}