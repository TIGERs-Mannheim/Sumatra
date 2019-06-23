/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;


/**
 * Helper class for providing Angle math problems.
 * 
 * @author stei_ol
 */
public final class AngleMath
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final float		PI								= (float) Math.PI;
	/** */
	public static final float		PI_TWO						= (float) (Math.PI * 2);
	/** */
	public static final float		PI_HALF						= (float) (Math.PI / 2);
	/** */
	public static final float		PI_QUART						= (float) (Math.PI / 4);
	/** */
	public static final float		PI_SQR						= (float) (Math.PI * Math.PI);
	/** */
	public static final float		PI_INV						= (float) (1 / Math.PI);
	/** */
	public static final float		PI_TWO_INV					= (float) (1 / (Math.PI * 2));
	
	private static final float		DEG_RAD_FACTOR				= 180f;
	// private static final int TRIG_MAX = 360001;
	// private static final int FULL_CIRCLE_DEG = 360;
	// private static final float TRIG_ARRAY_CORRECTION = 1000f;
	private static final double	HACKY_MAGIC_NUMBER		= 0.000001;
	/** */
	public static final float		DEG_TO_RAD					= PI / DEG_RAD_FACTOR;
	/** */
	public static final float		RAD_TO_DEG					= DEG_RAD_FACTOR / PI;
	
	private static final float		ANGLE_EQUAL_TOLERANCE	= 0.01f;
	
	
	// /** */
	// private static final float[] SINUS = new float[TRIG_MAX];
	// /** */
	// private static final float[] COSINUS = new float[TRIG_MAX];
	// /** */
	// private static final float[] TANGENS = new float[TRIG_MAX];
	
	
	// Static initialization of the trigonometric functions
	// static
	// {
	// for (int i = 0; i < TRIG_MAX; i++)
	// {
	// SINUS[i] = (float) Math.sin((i / ((1000 * FULL_CIRCLE_DEG) / (PI_TWO))) - PI);
	// COSINUS[i] = (float) Math.cos((i / ((1000 * FULL_CIRCLE_DEG) / (PI_TWO))) - PI);
	// TANGENS[i] = (float) Math.tan((i / ((1000 * FULL_CIRCLE_DEG) / (PI_TWO))) - PI);
	// }
	// }
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private AngleMath()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Normalize angle, to make sure angle is in (-pi/pi] interval.<br>
	 * New angle is returned, parameter stay unaffected.
	 * 
	 * @param angle
	 * @author Malte
	 * @return
	 */
	public static float normalizeAngle(final float angle)
	{
		// Don't call this a hack! It's numeric!
		return (angle - (Math.round((angle / (PI_TWO)) - HACKY_MAGIC_NUMBER) * PI_TWO));
	}
	
	
	/**
	 * Get the smallest difference between a1 and a2.
	 * All values will be normalised.
	 * 
	 * @param a1 Angle 1
	 * @param a2 Angle 2
	 * @return Smallest difference.
	 */
	public static float difference(final float a1, final float a2)
	{
		return normalizeAngle(normalizeAngle(a1) - normalizeAngle(a2));
	}
	
	
	/**
	 * @param a1
	 * @param a2
	 * @param tolerance
	 * @return
	 */
	public static boolean isEqual(final float a1, final float a2, final float tolerance)
	{
		return Math.abs(difference(a1, a2)) < tolerance;
	}
	
	
	/**
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static boolean isEqual(final float a1, final float a2)
	{
		return Math.abs(difference(a1, a2)) < ANGLE_EQUAL_TOLERANCE;
	}
	
	
	/**
	 * Returns the shortest rotation for turning to target angle.
	 * 
	 * @param angle1
	 * @param angle2
	 * @return shortest rotation. [-pi;pi]
	 */
	public static float getShortestRotation(final float angle1, final float angle2)
	{
		float rotateDist = 0;
		
		rotateDist = angle2 - angle1;
		if (rotateDist < -AngleMath.PI)
		{
			rotateDist = AngleMath.PI_TWO + rotateDist;
		}
		if (rotateDist > AngleMath.PI)
		{
			rotateDist -= AngleMath.PI_TWO;
		}
		return rotateDist;
	}
	
	
	/**
	 * SINUS!
	 * 
	 * @author Malte
	 * @param number
	 * @return
	 */
	public static float sin(final float number)
	{
		// return SINUS[(int) ((normalizeAngle(number) + PI) * (FULL_CIRCLE_DEG / PI_TWO) * TRIG_ARRAY_CORRECTION)];
		return (float) Math.sin(number);
	}
	
	
	/**
	 * COSINUS!
	 * 
	 * @author Malte
	 * @param number
	 * @return
	 */
	public static float cos(final float number)
	{
		// return COSINUS[(int) ((normalizeAngle(number) + PI) * (FULL_CIRCLE_DEG / PI_TWO) * TRIG_ARRAY_CORRECTION)];
		return (float) Math.cos(number);
	}
	
	
	/**
	 * TANGENS!
	 * 
	 * @author Malte
	 * @param number
	 * @return
	 */
	public static float tan(final float number)
	{
		// return TANGENS[(int) ((normalizeAngle(number) + PI) * (FULL_CIRCLE_DEG / PI_TWO) * TRIG_ARRAY_CORRECTION)];
		return (float) Math.tan(number);
	}
	
	
	/**
	 * @param number
	 * @return (float) {@link Math#acos(double)}
	 * @author Gero
	 */
	public static float acos(final float number)
	{
		return (float) Math.acos(number);
	}
	
	
	/**
	 * @param number
	 * @return (float) {@link Math#asin(double)}
	 * @author Gero
	 */
	public static float asin(final float number)
	{
		return (float) Math.asin(number);
	}
	
	
	/**
	 * @param deg The angle in degree that should be converted to radiant
	 * @return The given angle in radiant
	 * @author Gero
	 */
	public static float deg2rad(final float deg)
	{
		return DEG_TO_RAD * deg;
	}
	
	
	/**
	 * @param rad The angle in radiant that should be converted to degree
	 * @return The given angle in degree
	 * @author Gero
	 */
	public static float rad2deg(final float rad)
	{
		return RAD_TO_DEG * rad;
	}
	
	
	/**
	 * Tangens hyperbolicus
	 * 
	 * @param rad
	 * @return Tangens hyperbolicus converted to float
	 * @author AndreR
	 */
	public static float tanh(final float rad)
	{
		return (float) Math.tanh(rad);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
