/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.10.2010
 * Author(s): daniel
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.math;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;


/**
 * Test for AIMath functions
 * you may extend it as appropriate
 * 
 * @author DanielW
 */
public class SumatraMathTest
{
	private static final float	ACCURACY	= 0.001f;
	
	
	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath#faculty(int)}
	 * 
	 * @author Malte
	 */
	@Test
	public void testFaculty()
	{
		assertEquals((120), SumatraMath.faculty(5));
		assertEquals((-1), SumatraMath.faculty(-5));
		// This may change if you raise max_faculty
		assertEquals((-1), SumatraMath.faculty(50));
	}
	
	
	/**
	 * Test method for {@link SumatraMath#sign(float)}
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
	 * test {@link SumatraMath#hasDigitsAfterDecimalPoint(float)}
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