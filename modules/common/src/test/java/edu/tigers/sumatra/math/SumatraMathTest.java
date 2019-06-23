/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.10.2010
 * Author(s): daniel
 * *********************************************************
 */
package edu.tigers.sumatra.math;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;


/**
 * Test for AIMath functions
 * you may extend it as appropriate
 * 
 * @author DanielW
 */
public class SumatraMathTest
{
	private static final double ACCURACY = 0.001;
	
	
	/**
	 * Test method for {@link edu.tigers.sumatra.math.SumatraMath#faculty(int)}
	 * 
	 * @author Malte
	 */
	@Test
	public void testFaculty()
	{
		try
		{
			assertEquals((120), SumatraMath.faculty(5));
			
		} catch (MathException e)
		{
			Assert.fail();
		}
		try
		{
			assertEquals((-1), SumatraMath.faculty(-5));
			Assert.fail();
		} catch (MathException e)
		{
		}
		try
		{
			// This may change if you raise max_faculty
			assertEquals((-1), SumatraMath.faculty(50));
			Assert.fail();
		} catch (MathException e)
		{
		}
	}
	
	
	/**
	 * Test method for {@link SumatraMath#sign(double)}
	 * 
	 * @author Malte
	 */
	@Test
	public void testGetSign()
	{
		assertEquals(SumatraMath.sign(5), 1, ACCURACY);
		assertEquals(SumatraMath.sign(-5), -1, ACCURACY);
		assertEquals(SumatraMath.sign(0), 1, ACCURACY);
	}
	
	
	/**
	 * test {@link SumatraMath#hasDigitsAfterDecimalPoint(double)}
	 */
	@Test
	public void testHasDigitsAfterDecimal()
	{
		assertFalse(SumatraMath.hasDigitsAfterDecimalPoint(1.0f));
		assertTrue(SumatraMath.hasDigitsAfterDecimalPoint(1.22f));
		assertTrue(SumatraMath.hasDigitsAfterDecimalPoint(-1.02f));
		assertFalse(SumatraMath.hasDigitsAfterDecimalPoint(-1.0f));
	}
	
	
	/**
	 */
	@Test
	public void testisbetween()
	{
		assertEquals(true, SumatraMath.isBetween(-2, -10, 0));
		assertEquals(true, SumatraMath.isBetween(5, -10, 10));
		assertEquals(false, SumatraMath.isBetween(-20, -10, 10));
		assertEquals(false, SumatraMath.isBetween(15, -10, 10));
	}
	
	
}