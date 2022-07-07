/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * Helper class for providing Angle math problems.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AngleMath
{
	public static final double PI = Math.PI;
	public static final double PI_TWO = Math.PI * 2.0;
	public static final double PI_HALF = Math.PI / 2.0;
	public static final double PI_QUART = Math.PI / 4.0;

	public static final double DEG_180_IN_RAD = PI;
	public static final double DEG_090_IN_RAD = PI_HALF;
	public static final double DEG_045_IN_RAD = PI_QUART;

	private static final double DEG_RAD_FACTOR = 180;
	private static final double DEG_TO_RAD = PI / DEG_RAD_FACTOR;
	private static final double RAD_TO_DEG = DEG_RAD_FACTOR / PI;


	/**
	 * Normalize angle, to make sure angle is in (-pi/pi] interval.<br>
	 * New angle is returned, parameter stay unaffected.
	 *
	 * @param angle some unnormalized angle
	 * @return angle in [-pi..pi]
	 */
	public static double normalizeAngle(final double angle)
	{
		// Don't call this a hack! It's numeric!
		return angle - (Math.round((angle / (PI_TWO)) - 1e-6) * PI_TWO);
	}


	/**
	 * Get the smallest difference between angle1 and angle2.<br>
	 * <code>norm( angle1 - angle2 )</code>
	 * A positive difference represents a counter clockwise turn from angle2 to angle1
	 * A negative difference represents a clockwise turn from angle2 to angle1
	 *
	 * @param angle1 first angle
	 * @param angle2 second angle
	 * @return difference in [-pi..pi]
	 */
	public static double difference(final double angle1, final double angle2)
	{
		return normalizeAngle(angle1 - angle2);
	}


	/**
	 * Get the resulting angle after rotating angleStart in rotationDirection for angleAmount.<br>
	 * Rotating with a negative value of angleAmount is equivalent to rotating the opposite direction with the absolute amount
	 *
	 * @param angleStart
	 * @param angleAmount
	 * @param rotationDirection
	 * @return
	 */
	public static double rotateAngle(final double angleStart, final double angleAmount,
			final ERotationDirection rotationDirection)
	{
		if (rotationDirection == ERotationDirection.NONE)
		{
			return normalizeAngle(angleStart);
		}
		return normalizeAngle(rotationDirection == ERotationDirection.COUNTER_CLOCKWISE ?
				angleStart + angleAmount :
				angleStart - angleAmount
		);
	}


	public static ERotationDirection rotationDirection(final double angleStart, final double angleEnd)
	{
		final var difference = AngleMath.difference(angleEnd, angleStart);
		if (Math.abs(difference) < 1e-6)
		{
			return ERotationDirection.NONE;
		} else if (difference < 0)
		{
			return ERotationDirection.CLOCKWISE;
		} else
		{
			return ERotationDirection.COUNTER_CLOCKWISE;
		}
	}


	public static int compareAngle(final double angle1, final double angle2)
	{
		final var rotDirection = AngleMath.rotationDirection(angle1, angle2);
		return switch (rotDirection)
				{
					case NONE -> 0;
					case CLOCKWISE -> 1;
					case COUNTER_CLOCKWISE -> -1;
				};
	}


	/**
	 * Get the absolute smallest difference between angle1 and angle2.<br>
	 * <code>abs(norm(angle1-angle2))</code>
	 *
	 * @param angle1 first angle
	 * @param angle2 second angle
	 * @return absolute difference in [0..pi]
	 */
	public static double diffAbs(final double angle1, final double angle2)
	{
		return Math.abs(difference(angle1, angle2));
	}


	/**
	 * @param number a value
	 * @return cos of value
	 */
	public static double cos(final double number)
	{
		return SumatraMath.cos(number);
	}


	/**
	 * @param number a value
	 * @return sin of value
	 */
	public static double sin(final double number)
	{
		return SumatraMath.sin(number);
	}


	/**
	 * @param number a value
	 * @return tan of value
	 */
	public static double tan(final double number)
	{
		return SumatraMath.tan(number);
	}


	/**
	 * @param deg The angle in degree that should be converted to radiant
	 * @return The given angle in radiant
	 */
	public static double deg2rad(final double deg)
	{
		return DEG_TO_RAD * deg;
	}


	/**
	 * @param rad The angle in radiant that should be converted to degree
	 * @return The given angle in degree
	 */
	public static double rad2deg(final double rad)
	{
		return RAD_TO_DEG * rad;
	}


	/**
	 * @param angle input angle
	 * @return a mirrored angle
	 */
	public static double mirror(final double angle)
	{
		return normalizeAngle(angle + PI);
	}
}
