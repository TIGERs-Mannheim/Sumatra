/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.11.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;


/**
 * Class for testing several functions provided by {@link AVector2}, {@link Vector2}, {@link AVector2} and
 * {@link Vector3}
 * 
 * @author Malte
 */
public class VectorTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final double	ACCURACY	= 0.001;
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Test method for {@link AVector2#turnNew(double)}
	 */
	@Test
	public void testTurnNew()
	{
		final Vector2 input = new Vector2(1, 0);
		Vector2 expected = new Vector2(0, 1);
		Vector2 output;
		
		output = input.turnNew(AngleMath.PI / 2.0);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
		output = input.turnNew(-AngleMath.PI / 2.0);
		expected = new Vector2(0, -1);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
		output = input.turnNew(4 * AngleMath.PI);
		expected = new Vector2(input);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
		
	}
	
	
	/**
	 * Test method for {@link AVector2#scaleToNew(double)}
	 */
	@Test
	public void TestScaleToNew()
	{
		Vector2 input = new Vector2(1, 0);
		Vector2 expected = new Vector2(5, 0);
		Vector2 output;
		
		output = input.scaleToNew(5);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
		input = new Vector2(0, -1);
		expected = new Vector2(0, 0.5);
		output = input.scaleToNew(-0.5f);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
		input = new Vector2(0, 0);
		expected = new Vector2(0, 0);
		output = input.scaleToNew(-8);
		assertEquals(output.x(), expected.x(), ACCURACY);
		assertEquals(output.y(), expected.y(), ACCURACY);
		
	}
	
	
	/**
	 * Testmethod for Vector2#add.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testAdd()
	{
		Vector2 vec1 = new Vector2(2, 1);
		Vector2 vec2 = new Vector2(3, 4);
		
		vec1.add(vec2);
		assertEquals(new Vector2(5, 5), vec1);
		
		vec1 = new Vector2(-2, 0);
		vec1.add(vec2);
		assertEquals(new Vector2(1, 4), vec1);
		
		vec1 = new Vector2(-2, -3);
		vec2 = new Vector2(-1, 5);
		vec1.add(vec2);
		assertEquals(new Vector2(-3, 2), vec1);
	}
	
	
	/**
	 * Testmethod for Vector2#addNew.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testAddNew()
	{
		Vector2 vec1 = new Vector2(5, 7);
		Vector2 vec2 = new Vector2(1, 2);
		Vector2 result = vec1.addNew(vec2);
		assertEquals(new Vector2(6, 9), result);
		
		vec1 = new Vector2(-3, 0);
		vec2 = new Vector2(4, 2);
		result = vec1.addNew(vec2);
		assertEquals(new Vector2(1, 2), result);
		
		vec1 = new Vector2(-3, 5);
		vec2 = new Vector2(-4, -2);
		result = vec1.addNew(vec2);
		assertEquals(new Vector2(-7, 3), result);
		assertEquals(new Vector2(-3, 5), vec1);
		
		
	}
	
	
	/**
	 * Testmethod for Vector2#multiply.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testMultiply()
	{
		Vector2 vec1 = new Vector2(4, 7);
		double factor = 4.5;
		vec1.multiply(factor);
		assertEquals(new Vector2(18, 31.5), vec1);
		
		vec1 = new Vector2(1, 1);
		factor = AngleMath.PI;
		vec1.multiply(factor);
		assertEquals(vec1.x(), factor, ACCURACY);
		assertEquals(vec1.y(), factor, ACCURACY);
		
		vec1 = new Vector2(3, -5);
		factor = -2.1f;
		vec1.multiply(factor);
		assertEquals(vec1.x(), -6.3f, ACCURACY);
		assertEquals(vec1.y(), 10.5, ACCURACY);
		
		factor = 0;
		vec1.multiply(factor);
		assertEquals(new Vector2(0, 0), vec1);
		
	}
	
	
	/**
	 * Testmethod for Vector2#multiplyNew.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testMultiplyNew()
	{
		Vector2 vec1 = new Vector2(4, 7);
		double factor = 4.5;
		Vector2 result = vec1.multiplyNew(factor);
		assertEquals(new Vector2(18, 31.5), result);
		
		vec1 = new Vector2(1, 1);
		factor = AngleMath.PI;
		result = vec1.multiplyNew(factor);
		assertEquals(result.x(), factor, ACCURACY);
		assertEquals(result.y(), factor, ACCURACY);
		assertEquals(new Vector2(1, 1), vec1);
		
		vec1 = new Vector2(3, -5);
		factor = -2.1f;
		result = vec1.multiplyNew(factor);
		assertEquals(result.x(), -6.3f, ACCURACY);
		assertEquals(result.y(), 10.5, ACCURACY);
		
		factor = 0;
		result = vec1.multiplyNew(factor);
		assertEquals(new Vector2(0, 0), result);
		
	}
	
	
	/**
	 * Testmethod for Vector2#subtract.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testSubtract()
	{
		Vector2 vec1 = new Vector2(4, 2);
		Vector2 vec2 = new Vector2(3, 5);
		vec1.subtract(vec2);
		assertEquals(new Vector2(1, -3), vec1);
		
		vec1 = new Vector2(1, 5);
		vec2 = new Vector2(-2, 5);
		vec1.subtract(vec2);
		assertEquals(new Vector2(3, 0), vec1);
		
		vec2 = new Vector2(2.4f, -3.1f);
		vec1.subtract(vec2);
		assertEquals(vec1.x(), 0.6, ACCURACY);
		assertEquals(vec1.y(), 3.1, ACCURACY);
		
	}
	
	
	/**
	 * Testmethod for Vector2#subtractNew.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testSubtractNew()
	{
		Vector2 vec1 = new Vector2(5, 7);
		Vector2 vec2 = new Vector2(1, 2);
		Vector2 result = vec1.subtractNew(vec2);
		assertEquals(new Vector2(4, 5), result);
		
		vec1 = new Vector2(-3, 0);
		vec2 = new Vector2(4, 2);
		result = vec1.subtractNew(vec2);
		assertEquals(new Vector2(-7, -2), result);
		
		vec1 = new Vector2(-3, 5);
		vec2 = new Vector2(-4, -2);
		result = vec1.subtractNew(vec2);
		assertEquals(new Vector2(1, 7), result);
		assertEquals(new Vector2(-3, 5), vec1);
		
		
	}
	
	
	/**
	 * Testmethod for Vector2#scaleTo.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testScaleTo()
	{
		Vector2 vec1 = new Vector2(3, -4);
		vec1.scaleTo(15.0f);
		assertEquals(new Vector2(9, -12), vec1);
		
		vec1 = new Vector2(2.5f, 2.5);
		vec1.scaleTo(12.5f);
		assertEquals(vec1.x(), 8.8388, ACCURACY);
		assertEquals(vec1.y(), 8.8388, ACCURACY);
		
		vec1 = new Vector2(0, 0);
		vec1.scaleTo(8);
		assertEquals(new Vector2(0, 0), vec1);
		
	}
	
	
	/**
	 * Testmethod for Vector2#turn.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testTurn()
	{
		Vector2 vec1 = new Vector2(1, 0);
		vec1.turn(AngleMath.PI);
		assertEquals(vec1.x(), -1, ACCURACY);
		assertEquals(vec1.y(), 0, ACCURACY);
		
		vec1 = new Vector2(1, 0);
		vec1.turn(AngleMath.PI * (1.5f));
		assertEquals(vec1.x(), 0, ACCURACY);
		assertEquals(vec1.y(), -1, ACCURACY);
		
		vec1 = new Vector2(1, 0);
		vec1.turn(AngleMath.PI * (-0.5f));
		assertEquals(vec1.x(), 0, ACCURACY);
		assertEquals(vec1.y(), -1, ACCURACY);
		
		vec1 = new Vector2(0, 0);
		vec1.turn(AngleMath.PI * (1.5f));
		assertEquals(vec1.x(), 0, ACCURACY);
		assertEquals(vec1.y(), 0, ACCURACY);
	}
	
	
	/**
	 * Testmethod for Vector2#turnTo.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testTurnTo()
	{
		Vector2 vec1 = new Vector2(1, 0);
		vec1.turnTo(AngleMath.PI);
		assertEquals(vec1.x(), -1, ACCURACY);
		assertEquals(vec1.y(), 0, ACCURACY);
		
		vec1 = new Vector2(1, 0);
		vec1.turnTo(AngleMath.PI * (-0.5f));
		assertEquals(vec1.x(), 0, ACCURACY);
		assertEquals(vec1.y(), -1, ACCURACY);
		
		vec1 = new Vector2(0, 0);
		vec1.turnTo(AngleMath.PI);
		assertEquals(vec1.x(), 0, ACCURACY);
		assertEquals(vec1.y(), 0, ACCURACY);
	}
	
	
	/**
	 * Testmethod for Vector2#scalarProduct.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testScalarProduct()
	{
		Vector2 vec1 = new Vector2(1, 3);
		Vector2 vec2 = new Vector2(-4, 2);
		double result = vec1.scalarProduct(vec2);
		assertEquals(2f, result, ACCURACY);
		
		vec1 = new Vector2(1, 3);
		vec2 = new Vector2(0, 0);
		result = vec1.scalarProduct(vec2);
		assertEquals(0f, result, ACCURACY);
		
		vec1 = new Vector2(2.1f, 3);
		vec2 = new Vector2(10f, 1.5);
		result = vec1.scalarProduct(vec2);
		assertEquals(25.5f, result, ACCURACY);
	}
	
	
	/**
	 * Testmethod for Vector2#equals.
	 * 
	 * @author Timo, Frieder
	 */
	@Test
	public void testEquals()
	{
		Vector2 vec1 = new Vector2(5, 0);
		Vector2 vec2 = new Vector2(5, 0);
		Boolean result = vec1.equals(vec2);
		assertTrue(result);
		
		vec1 = new Vector2(3.1f, 1.4);
		vec2 = new Vector2(3.1f, 1.4);
		result = vec1.equals(vec2);
		assertTrue(result);
		
		vec1 = new Vector2(3.1f, 1.4);
		vec2 = new Vector2(3.1f, 1.3);
		result = vec1.equals(vec2);
		assertTrue(!result);
		
	}
}
