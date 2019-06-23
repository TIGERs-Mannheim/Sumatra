/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.util.ArrayList;
import java.util.List;


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
	/**  */
	public static final double		EQUAL_TOL		= 0.000001;
																
	/** PI as a double */
	public static final double		PI					= Math.PI;
																
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
	 * This is not inaccurate or fast. It just converts the result to double!
	 * 
	 * @author Malte
	 * @param number
	 * @return
	 */
	public static double sqrt(final double number)
	{
		return Math.sqrt(number);
	}
	
	
	/**
	 * Get absolute of number
	 * 
	 * @param number
	 * @return
	 */
	public static double abs(final double number)
	{
		return Math.abs(number);
	}
	
	
	/**
	 * This is not inaccurate or fast. It just converts the result to double!
	 * 
	 * @author AndreR
	 * @param number
	 * @return
	 */
	public static double cos(final double number)
	{
		return Math.cos(number);
	}
	
	
	/**
	 * This is not inaccurate or fast. It just converts the result to double!
	 * 
	 * @author AndreR
	 * @param number
	 * @return
	 */
	public static double sin(final double number)
	{
		return Math.sin(number);
	}
	
	
	/**
	 * @param exponent The exponent
	 * @return (double) {@link SumatraMath#exp(double)}
	 * @author Gero
	 */
	public static double exp(final double exponent)
	{
		return Math.exp(exponent);
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
	public static boolean isZero(final double x, final double epsilon)
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
	 * EPSILON = 0.00001;
	 * 
	 * @author GuntherB
	 * @param x
	 * @return
	 */
	public static boolean isZero(final double x)
	{
		return isZero(x, EQUAL_TOL);
	}
	
	
	/**
	 * this method returns the faculty of an int.
	 * 
	 * @param n
	 * @author Malte, Gero
	 * @return
	 * @throws MathException
	 */
	public static long faculty(final int n) throws MathException
	{
		if (n > FACTORIAL_MAX)
		{
			throw new MathException("AIMath.faculty is limited to FACTORIAL_MAX; if you need more, change it! ;-)");
		} else if (n < 0)
		{
			throw new MathException("AIMath.faculty: Can't calculate faculty of a negative number!");
		} else
		{
			return FACTORIALS[n];
		}
	}
	
	
	/**
	 * gets the sign of a double
	 * 
	 * @param f
	 * @return -1 if f is negative; 1 else
	 * @author DanielW
	 */
	public static double sign(final double f)
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
	public static boolean isPositive(final double f)
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
	public static double square(final double x)
	{
		return x * x;
	}
	
	
	/**
	 * @param x
	 * @return x^3 (cubic of x)
	 * @author DanielW
	 */
	public static double cubic(final double x)
	{
		return x * x * x;
	}
	
	
	/**
	 * Returns the minimum double-value
	 * 
	 * @return minimum value
	 * @author DionH
	 * @param values
	 */
	public static double min(final double... values)
	{
		if (values.length == 0)
		{
			throw new IllegalArgumentException("No values");
		}
		
		double minimum = values[0];
		
		for (final double f : values)
		{
			if (f < minimum)
			{
				minimum = f;
			}
		}
		
		return minimum;
	}
	
	
	/**
	 * Returns the minimum double-value
	 * 
	 * @return maximum value
	 * @author Frieder Berthold
	 * @param values
	 */
	public static double max(final double... values)
	{
		if (values.length == 0)
		{
			throw new IllegalArgumentException("No values");
		}
		
		double maximum = values[0];
		
		for (final double f : values)
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
	public static boolean hasDigitsAfterDecimalPoint(final double number)
	{
		final double numberInt = Math.ceil(number);
		
		if (isEqual(number, numberInt))
		{
			return false;
		}
		return true;
	}
	
	
	/**
	 * Checks two double values for equality with a small tolerance value
	 * 
	 * @param a
	 * @param b
	 * @param tolerance
	 * @return
	 */
	public static boolean isEqual(final double a, final double b, final double tolerance)
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
	public static double map(double x, final double in_min, final double in_max, final double out_min,
			final double out_max)
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
	
	
	/**
	 * Calculate mean value
	 * 
	 * @param values
	 * @return
	 */
	public static double mean(final List<Double> values)
	{
		double sum = 0;
		for (Double f : values)
		{
			sum += f;
		}
		return sum / values.size();
	}
	
	
	/**
	 * Calculate mean value
	 * 
	 * @param values
	 * @return
	 */
	public static double meanInt(final List<Integer> values)
	{
		double sum = 0;
		for (Integer f : values)
		{
			sum += f;
		}
		return sum / values.size();
	}
	
	
	/**
	 * Calculate variance
	 * 
	 * @param values
	 * @return
	 */
	public static double variance(final List<Double> values)
	{
		double mu = mean(values);
		List<Double> val2 = new ArrayList<Double>(values.size());
		for (Double f : values)
		{
			double diff = f - mu;
			val2.add(diff * diff);
		}
		return mean(val2);
	}
	
	
	/**
	 * Calculate standard deviation
	 * 
	 * @param values
	 * @return
	 */
	public static double std(final List<Double> values)
	{
		return sqrt(variance(values));
	}
}