/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * Testing the AngleMath module.
 * 
 * @author stei_ol
 */
public class AngleMathTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final double	ACCURACY	= 0.001;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Test method for {@link edu.tigers.sumatra.math.GeoMath#angleBetweenXAxisAndLine}
	 */
	@Test
	public void testAngleBetweenXAxisAndLine()
	{
		assertEquals(AngleMath.PI / 4, GeoMath.angleBetweenXAxisAndLine(new Vector2(0f, 0), new Vector2(1f, 1)), 0.01); // falsch!?
		assertEquals(-AngleMath.PI / 4, GeoMath.angleBetweenXAxisAndLine(new Vector2(0f, 0), new Vector2(1f, -1f)), 0.01); // falsch!?
		assertEquals(0, GeoMath.angleBetweenXAxisAndLine(new Vector2(0f, 0), new Vector2(1f, 0)), 0.01);
		assertEquals(AngleMath.PI, GeoMath.angleBetweenXAxisAndLine(new Vector2(0f, 0), new Vector2(-1f, 0)), 0.01);
	}
	
	
	/**
	 * Test method for {@link AngleMath#normalizeAngle}
	 * 
	 * @author Malte
	 */
	@Test
	public void testNormalizeAngle()
	{
		assertEquals(AngleMath.normalizeAngle(4.6f * AngleMath.PI), 0.6 * AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(-4.6f * AngleMath.PI), -0.6f * AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(3.6f * AngleMath.PI), -0.4f * AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(-3.6f * AngleMath.PI), 0.4 * AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(5.001f * AngleMath.PI), -0.999f * AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(5f * AngleMath.PI), AngleMath.PI, ACCURACY);
		assertEquals(AngleMath.normalizeAngle(4f * AngleMath.PI), 0, ACCURACY);
	}
	
	
	/**
	 * Tests trigonometric functions of AIMath
	 */
	@Test
	public void testTrigonometry()
	{
		// sin
		assertEquals(Math.sin(AngleMath.PI), 0, ACCURACY);
		assertEquals(Math.sin(4.5f), -0.9775301, ACCURACY);
		assertEquals(Math.sin(-34), -0.529, ACCURACY);
		
		// cos
		assertEquals(Math.cos(5), 0.28366, ACCURACY);
		assertEquals(Math.cos(-0.1f), 0.9950, ACCURACY);
		
		// tan
		assertEquals(AngleMath.tan(3), -0.1425, ACCURACY);
		assertEquals(AngleMath.tan(-2), 2.185, ACCURACY);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
