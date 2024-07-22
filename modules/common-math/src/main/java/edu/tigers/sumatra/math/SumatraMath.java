/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import net.jafama.FastMath;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;


/**
 * This class holds basic math-functions for every day use.
 *
 * @author Malte
 */
public final class SumatraMath
{
	private static final double EQUAL_TOL = 1e-3;


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
		return FastMath.sqrt(number);
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
	 * @param a         first value
	 * @param b         second value
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
	 * @param x   a value
	 * @param min smaller value
	 * @param max larger value
	 * @return true, if min <= x <= max
	 */
	public static boolean isBetween(final double x, final double min, final double max)
	{
		if (max > min)
		{
			return (x >= min) && (x <= max);
		}
		return (x >= max) && (x <= min);
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
	 * @param from  the value that corresponds to 0
	 * @param to    the value that corresponds to 1
	 * @return value in range [0..1]
	 */
	public static double relative(final double value, final double from, final double to)
	{
		double range = to - from;
		double cappedValue = cap(value, from, to);
		double rel = (cappedValue - from) / range;
		Validate.isTrue(rel >= 0);
		Validate.isTrue(rel <= 1);
		return rel;
	}


	/**
	 * Cap value if outside the range, else return value.
	 *
	 * @param value  the value
	 * @param bound1 the first bound value
	 * @param bound2 the second bound value
	 * @return value in range [bound1..bound2] or [bound2..bound1]
	 */
	public static double cap(final double value, final double bound1, final double bound2)
	{
		double min = Math.min(bound1, bound2);
		double max = Math.max(bound1, bound2);
		return Math.max(min, Math.min(max, value));
	}


	/**
	 * Cap value if outside the range, else return value.
	 *
	 * @param value  the value
	 * @param bound1 the first bound value
	 * @param bound2 the second bound value
	 * @return value in range [bound1..bound2] or [bound2..bound1]
	 */
	public static int cap(final int value, final int bound1, final int bound2)
	{
		int min = Math.min(bound1, bound2);
		int max = Math.max(bound1, bound2);
		return Math.max(min, Math.min(max, value));
	}


	/**
	 * Add a magnitude to the absolute of a value.
	 *
	 * @param baseValue the base value
	 * @param offset    the magnitude to add to the absolute of the base value
	 * @return the result
	 */
	public static double addMagnitude(double baseValue, double offset)
	{
		return Math.signum(baseValue) * (Math.abs(baseValue) + offset);
	}


	/**
	 * Solves for the real roots of a quadratic equation with real
	 * coefficients. The quadratic equation is of the form
	 * <p>
	 * <I>ax</I><SUP>2</SUP> + <I>bx</I> + <I>c</I> = 0
	 * <p>
	 *
	 * @param a Coefficient of <I>x</I><SUP>2</SUP>.
	 * @param b Coefficient of <I>x</I>.
	 * @param c Constant coefficient.
	 * @return A list of roots.
	 */

	public static List<Double> quadraticFunctionRoots(final double a, final double b, final double c)
	{
		if (isEqual(a, 0.0))
		{
			if (isEqual(b, 0.0))
			{
				// the function is of constant form 'c = 0' => no roots
				return List.of();
			}

			double x1 = -c / b;
			return List.of(x1);
		}

		// normalize coefficients
		double p = b / a;
		double q = c / a;

		// calculate discriminant
		double d = (p * p) - (4.0 * q);

		if (d < 0.0)
		{
			// no real solution
			return List.of();
		} else if (d > 0.0)
		{
			double pHalf = p * 0.5;
			double x1 = -pHalf + SumatraMath.sqrt((pHalf * pHalf) - q);
			double x2 = -pHalf - SumatraMath.sqrt((pHalf * pHalf) - q);
			return List.of(x1, x2);
		} else
		{
			double x1 = -p * 0.5;
			return List.of(x1);
		}
	}


	/**
	 * Solves for the real roots of a cubic equation with real
	 * coefficients. The cubic equation is of the form
	 * <p>
	 * <I>ax</I><SUP>3</SUP> + <I>bx</I><SUP>2</SUP> + <I>cx</I> + <I>d</I> = 0
	 * <p>
	 * Source taken from: <a href="https://github.com/davidzof/wattzap/blob/master/src/com/wattzap/model/power/Cubic.java">algorithm</a>
	 *
	 * @param a3 Coefficient of <I>x</I><SUP>3</SUP>.
	 * @param b2 Coefficient of <I>x</I><SUP>2</SUP>.
	 * @param c1 Coefficient of <I>x</I>.
	 * @param d0 Constant coefficient.
	 * @return A list of roots.
	 */
	public static List<Double> cubicFunctionRoots(final double a3, final double b2, final double c1, final double d0)
	{
		// Verify preconditions.
		if (isZero(a3))
		{
			return quadraticFunctionRoots(b2, c1, d0);
		}

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
			double theta = SumatraMath.acos(r / SumatraMath.sqrt(-qCube));
			double sqrtQ = SumatraMath.sqrt(-q);
			double x1 = (2.0 * sqrtQ * SumatraMath.cos(theta / 3.0)) - aOver3;
			double x2 = (2.0 * sqrtQ * SumatraMath.cos((theta + AngleMath.PI_TWO) / 3.0)) - aOver3;
			double x3 = (2.0 * sqrtQ * SumatraMath.cos((theta + (AngleMath.PI * 4.0)) / 3.0)) - aOver3;
			return List.of(x1, x2, x3);
		} else if (d > 0.0)
		{
			// One real root.
			double sqrtD = SumatraMath.sqrt(d);
			double s = Math.cbrt(r + sqrtD);
			double t = Math.cbrt(r - sqrtD);
			double x1 = (s + t) - aOver3;
			return List.of(x1);
		} else
		{
			// Three real roots, at least two equal.
			double cbrtR = Math.cbrt(r);
			double x1 = (2 * cbrtR) - aOver3;
			double x2 = -cbrtR - aOver3;
			return List.of(x1, x2);
		}
	}


	/**
	 * Solves for the real roots of a quartic equation with real
	 * coefficients. The quartic equation is of the form
	 * <p>
	 * z_4x^4 + z_3x^3 z_2x^2 + z_1x + z_0 = 0
	 * <p>
	 * <a href="https://quarticequations.com/Quartic.pdf">Improved ferrari's method</a>
	 *
	 * @param z4 Coefficient for x^4
	 * @param z3 Coefficient for x^3
	 * @param z2 Coefficient for x^2
	 * @param z1 Coefficient for x
	 * @param z0 Constant coefficient
	 * @return A list of roots.
	 */
	public static List<Double> quarticFunctionRoots(double z4, double z3, double z2, double z1, double z0)
	{
		// Verify preconditions
		if (isZero(z4))
		{
			return cubicFunctionRoots(z3, z2, z1, z0);
		}

		// Normalize coefficients
		double a3 = z3 / z4;
		double a2 = z2 / z4;
		double a1 = z1 / z4;
		double a0 = z0 / z4;

		// Commence solution
		double c = a3 / 4;
		double c2 = c * c;
		double c3 = c2 * c;
		double c4 = c3 * c;
		double b2 = a2 - 6 * c2;
		double b1 = a1 - 2 * a2 * c + 8 * c3;
		double b0 = a0 - a1 * c + a2 * c2 - 3 * c4;

		double tmp = b2 * b2 / 4 - b0;

		var cubeRoots = cubicFunctionRoots(1, b2, tmp, -b1 * b1 / 8);
		double m = cubeRoots.stream().filter(r -> r >= 0).findAny().orElse(0.0);
		int sign = b1 > 0 ? 1 : -1;
		double rRadicand = m * m + b2 * m + tmp;
		Validate.isTrue(rRadicand >= 0);
		double r = sign * SumatraMath.sqrt(rRadicand);
		double radicand1 = -m / 2 - b2 / 2 - r;
		double radicand2 = -m / 2 - b2 / 2 + r;
		double sqrtMHalf = SumatraMath.sqrt(m / 2);

		var roots = new ArrayList<Double>();

		if (SumatraMath.isZero(radicand1))
		{
			roots.add(sqrtMHalf);
		} else if (radicand1 > 0)
		{
			var root = SumatraMath.sqrt(radicand1);
			roots.add(sqrtMHalf - c + root);
			roots.add(sqrtMHalf - c - root);
		}

		if (SumatraMath.isZero(radicand2))
		{
			roots.add(-sqrtMHalf);
		} else if (radicand2 > 0)
		{
			var root = SumatraMath.sqrt(radicand2);
			roots.add(-sqrtMHalf - c + root);
			roots.add(-sqrtMHalf - c - root);
		}
		return roots;
	}


	public static double getEqualTol()
	{
		return EQUAL_TOL;
	}


	/**
	 * @param x
	 * @return
	 */
	public static double acos(double x)
	{
		return FastMath.acos(x);
	}


	/**
	 * @param x
	 * @return
	 */
	public static double asin(double x)
	{
		return FastMath.asin(x);
	}


	/**
	 * A replaceable implementation
	 *
	 * @param y
	 * @param x
	 * @return
	 */
	public static double atan2(double y, double x)
	{
		return FastMath.atan2(y, x);
	}


	/**
	 * A replaceable implementation
	 *
	 * @param angle
	 * @return
	 */
	public static double cos(double angle)
	{
		return FastMath.cos(angle);
	}


	/**
	 * A replaceable implementation
	 *
	 * @param angle
	 * @return
	 */
	public static double sin(double angle)
	{
		return FastMath.sin(angle);
	}


	/**
	 * A replaceable implementation
	 *
	 * @param angle
	 * @return
	 */
	public static double tan(double angle)
	{
		return FastMath.tan(angle);
	}


	public static List<Double> evenDistribution1D(double min, double max, int n)
	{
		double len = max - min;
		double step = len / n;
		List<Double> points = new ArrayList<>();
		for (int i = 0; i < n; i++)
		{
			points.add(min + step * i + 0.5 * step);
		}
		return points;
	}
}