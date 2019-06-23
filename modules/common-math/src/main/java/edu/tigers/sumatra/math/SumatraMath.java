/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;


/**
 * This class holds basic math-functions for every day use.
 *
 * @author Malte
 */
public final class SumatraMath
{
	private static final double EQUAL_TOL = 1e-6;
	
	
	private SumatraMath()
	{
	}
	
	
	/**
	 * Maps to {@link Math#sqrt}
	 * 
	 * @param number a value
	 * @return the sqrt of number
	 */
	public static double sqrt(final double number)
	{
		return Math.sqrt(number);
	}
	
	
	/**
	 * @param x a value
	 * @return x^2 (square of x)
	 */
	public static double square(final double x)
	{
		return x * x;
	}
	
	
	/**
	 * Returns the minimum double-value
	 *
	 * @param values all values to check
	 * @return minimum value
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
	 * Returns the maximum double-value
	 *
	 * @param values all values to check
	 * @return maximum value
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
		
		return !isEqual(number, numberInt);
	}
	
	
	/**
	 * Checks two double values for equality with a small tolerance value
	 * 
	 * @param a first value
	 * @param b second value
	 * @param tolerance to use for comparison
	 * @return true, if absolute difference between both values is smaller than or equal to tolerance
	 */
	public static boolean isEqual(final double a, final double b, final double tolerance)
	{
		return Math.abs(a - b) <= tolerance;
	}
	
	
	/**
	 * Checks two double values for equality with a small tolerance value
	 *
	 * @param a first value
	 * @param b second value
	 * @return true, if absolute difference between both values is smaller than a tolerance
	 */
	public static boolean isEqual(final double a, final double b)
	{
		return isEqual(a, b, EQUAL_TOL);
	}
	
	
	/**
	 * Checks if x is a Number between to values (inclusive)
	 * 
	 * @param x a value
	 * @param min smaller value
	 * @param max larger value
	 * @return true, if min <= x <= max
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
	 * @param v some value
	 * @return true, if the value is almost zero
	 */
	public static boolean isZero(final double v)
	{
		return Math.abs(v) < EQUAL_TOL;
	}
	
	
	/**
	 * Project value to a relative values between 0 and 1
	 * 
	 * @param value the value
	 * @param from smallest possible
	 * @param to largest possible
	 * @return value in range [0..1]
	 */
	public static double relative(final double value, final double from, final double to)
	{
		double min = Math.min(from, to);
		double max = Math.max(from, to);
		double range = Math.abs(max - min);
		Validate.isTrue(range > 0);
		double limitedValue = cap(value, min, max);
		double rel = (limitedValue - min) / range;
		Validate.isTrue(rel >= 0);
		Validate.isTrue(rel <= 1);
		return rel;
	}
	
	
	/**
	 * Cap value if outside of the range, else return value.
	 *
	 * @param value the value
	 * @param min the lower bound
	 * @param max the upper bound
	 * @return value in range [min..max]
	 */
	public static double cap(final double value, final double min, final double max)
	{
		Validate.isTrue(max >= min);
		return Math.max(min, Math.min(max, value));
	}
	
	
	/**
	 * Solves for the real roots of a quadratic equation with real
	 * coefficients. The quadratic equation is of the form
	 * <P>
	 * <I>ax</I><SUP>2</SUP> + <I>bx</I> + <I>c</I> = 0
	 * <P>
	 * 
	 * @author AndreR <andre@ryll.cc>
	 * @param a Coefficient of <I>x</I><SUP>2</SUP>.
	 * @param b Coefficient of <I>x</I>.
	 * @param c Constant coefficient.
	 * @return A list of roots.
	 */
	
	public static List<Double> quadraticFunctionRoots(final double a, final double b, final double c)
	{
		List<Double> roots = new ArrayList<>();
		
		if (isEqual(a, 0.0))
		{
			if (isEqual(b, 0.0))
			{
				// the function is of constant form 'c = 0' => no roots
				return roots;
			}
			
			double x1 = -c / b;
			roots.add(x1);
			return roots;
		}
		
		// normalize coefficients
		double p = b / a;
		double q = c / a;
		
		// calculate discriminant
		double d = (p * p) - (4.0 * q);
		
		if (d < 0.0)
		{
			// no real solution
		} else if (d > 0.0)
		{
			double pHalf = p * 0.5;
			double x1 = -pHalf + Math.sqrt((pHalf * pHalf) - q);
			double x2 = -pHalf - Math.sqrt((pHalf * pHalf) - q);
			roots.add(x1);
			roots.add(x2);
		} else
		{
			double x1 = -p * 0.5;
			roots.add(x1);
		}
		
		return roots;
	}
	
	
	/**
	 * Solves for the real roots of a cubic equation with real
	 * coefficients. The cubic equation is of the form
	 * <P>
	 * <I>ax</I><SUP>3</SUP> + <I>bx</I><SUP>2</SUP> + <I>cx</I> + <I>d</I> = 0
	 * <P>
	 * Source taken from: https://github.com/davidzof/wattzap/blob/master/src/com/wattzap/model/power/Cubic.java
	 * 
	 * @author AndreR <andre@ryll.cc>
	 * @param a3 Coefficient of <I>x</I><SUP>3</SUP>.
	 * @param b2 Coefficient of <I>x</I><SUP>2</SUP>.
	 * @param c1 Coefficient of <I>x</I>.
	 * @param d0 Constant coefficient.
	 * @return A list of roots.
	 */
	public static List<Double> cubicFunctionRoots(final double a3, final double b2, final double c1, final double d0)
	{
		// Verify preconditions.
		if (isEqual(a3, 0.0))
		{
			return quadraticFunctionRoots(b2, c1, d0);
		}
		
		List<Double> roots = new ArrayList<>();
		
		// normalize coefficients
		double a = b2 / a3;
		double b = c1 / a3;
		double c = d0 / a3;
		
		// commence solution
		double aOver3 = a / 3.0;
		double q = ((3 * b) - (a * a)) / 9.0;
		double qCube = q * q * q;
		double r = ((9 * a * b) - (27 * c) - (2 * a * a * a)) / 54.0;
		double rSqr = r * r;
		double d = qCube + rSqr;
		
		if (d < 0.0)
		{
			// Three unequal real roots.
			double theta = Math.acos(r / Math.sqrt(-qCube));
			double sqrtQ = Math.sqrt(-q);
			double x1 = (2.0 * sqrtQ * Math.cos(theta / 3.0)) - aOver3;
			double x2 = (2.0 * sqrtQ * Math.cos((theta + AngleMath.PI_TWO) / 3.0)) - aOver3;
			double x3 = (2.0 * sqrtQ * Math.cos((theta + (AngleMath.PI * 4.0)) / 3.0)) - aOver3;
			roots.add(x1);
			roots.add(x2);
			roots.add(x3);
		} else if (d > 0.0)
		{
			// One real root.
			double sqrtD = Math.sqrt(d);
			double s = Math.cbrt(r + sqrtD);
			double t = Math.cbrt(r - sqrtD);
			double x1 = (s + t) - aOver3;
			roots.add(x1);
		} else
		{
			// Three real roots, at least two equal.
			double cbrtR = Math.cbrt(r);
			double x1 = (2 * cbrtR) - aOver3;
			double x2 = -cbrtR - aOver3;
			roots.add(x1);
			roots.add(x2);
		}
		
		return roots;
	}
	
	
	public static double getEqualTol()
	{
		return EQUAL_TOL;
	}
}