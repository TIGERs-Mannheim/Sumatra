/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Dominik Engelhardt
 */
public class OffensiveRedirectorMathTest
{
	
	private OffensiveRedirectorMath math = new OffensiveRedirectorMath();
	
	
	@Test
	public void testGetPassDurationShouldBeZero() throws Exception
	{
		IVector2 source = Vector2.zero();
		IVector2 target = Vector2.zero();
		double duration = math.getPassDuration(source, target);
		assertEquals(0, duration, 0.01);
	}
	
	
	@Test
	public void testGetPassDurationShouldBeOne() throws Exception
	{
		IVector2 source = Vector2.zero();
		IVector2 target = Vector2.fromX(-2000);
		double duration = math.getPassDuration(source, target);
		assertEquals(1, duration, 0.1);
	}
	
	
	@Test
	public void testGetPassDurationShouldBeHuge() throws Exception
	{
		IVector2 source = Vector2.zero();
		IVector2 target = Vector2.fromX(1e10);
		double duration = math.getPassDuration(source, target);
		assertEquals(6950, duration, 50);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetPassDurationShouldThrowIllegalArgumentException() throws Exception
	{
		IVector2 source = Vector2.zero();
		IVector2 target = Vector2.fromX(Double.NaN);
		math.getPassDuration(source, target);
	}
	
}