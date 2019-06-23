/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.11.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;


/**
 * Test method for {@link Line}.
 * 
 * @author Malte, Timo, Frieder
 */
public class LineTest
{
	private static final double	ACCURACY	= 0.001;
	
	
	/**
	 * Testmethod for Line#getYIntercept.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testGetYIntercept()
	{
		
		Vector2 vecS = new Vector2(1, 1);
		Vector2 vecD = new Vector2(1, 1);
		Line line1 = new Line(vecS, vecD);
		double result;
		try
		{
			result = line1.getYIntercept();
			assertEquals(0.0f, result, ACCURACY);
		} catch (MathException err)
		{
			fail("Exception: " + err.getMessage());
		}
		
		vecS = new Vector2(3, 4);
		vecD = new Vector2(0, 1);
		line1 = new Line(vecS, vecD);
		try
		{
			result = line1.getYIntercept();
			fail("fail");
		} catch (Exception err)
		{
			
		}
		
		vecS = new Vector2(-11, 1);
		vecD = new Vector2(-3, 0);
		line1 = new Line(vecS, vecD);
		try
		{
			result = line1.getYIntercept();
			assertEquals(1f, result, ACCURACY);
		} catch (MathException err)
		{
			fail("Exception: " + err.getMessage());
		}
		
		vecS = new Vector2(-9, 1);
		vecD = new Vector2(-3, 0.5);
		line1 = new Line(vecS, vecD);
		try
		{
			result = line1.getYIntercept();
			assertEquals(-0.5f, result, ACCURACY);
		} catch (MathException err)
		{
			fail("Exception: " + err.getMessage());
		}
	}
	
	
	/**
	 * Testmethod for Line#getSlope.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void textGetSlope()
	{
		Vector2 vecS = new Vector2(1, 1);
		Vector2 vecD = new Vector2(1, 1);
		Line line1 = new Line(vecS, vecD);
		double result;
		try
		{
			result = line1.getSlope();
			assertEquals(1f, result, ACCURACY);
		} catch (MathException err)
		{
			fail("Exception: " + err.getMessage());
		}
		
		vecS = new Vector2(3, 4);
		vecD = new Vector2(0, 1);
		line1 = new Line(vecS, vecD);
		try
		{
			result = line1.getSlope();
			fail("fail");
		} catch (Exception err)
		{
			
		}
		
		vecS = new Vector2(-11, 1);
		vecD = new Vector2(-3, 0);
		line1 = new Line(vecS, vecD);
		try
		{
			result = line1.getSlope();
			assertEquals(0f, result, ACCURACY);
		} catch (MathException err)
		{
			fail("Exception: " + err.getMessage());
		}
		
		vecS = new Vector2(-9, 1);
		vecD = new Vector2(-4, 0.5);
		line1 = new Line(vecS, vecD);
		try
		{
			result = line1.getSlope();
			assertEquals(-0.125f, result, ACCURACY);
		} catch (MathException err)
		{
			fail("Exception: " + err.getMessage());
		}
		
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
