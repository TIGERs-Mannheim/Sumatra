/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import edu.tigers.sumatra.math.AngleMath;


/**
 * @author nicolai.ommer
 */
public class VectorMathTest
{
	
	
	@Test
	public void testGetAngle()
	{
		for (double angle = -2 * AngleMath.PI_TWO; angle < 2 * AngleMath.PI_TWO; angle += 0.1)
		{
			double normalizeAngle = AngleMath.normalizeAngle(angle);
			IVector2 vector = Vector2.fromAngle(angle);
			double resultAngle = VectorMath.getAngle(vector);
			assertThat(normalizeAngle).isCloseTo(resultAngle, within(1e-6));
			assertThat(resultAngle).isBetween(-AngleMath.PI, AngleMath.PI);
		}
	}
	
	
	@Test
	public void testAngleDifference()
	{
		final double maxAbsAngle = 2 * AngleMath.PI_TWO;
		for (double angle1 = -maxAbsAngle; angle1 < maxAbsAngle; angle1 += 0.1)
		{
			for (double angle2 = -maxAbsAngle; angle2 < maxAbsAngle; angle2 += 0.1)
			{
				IVector2 vector1 = Vector2.fromAngle(angle1);
				IVector2 vector2 = Vector2.fromAngle(angle2);
				
				double diff = AngleMath.difference(angle2, angle1);
				double diff1 = VectorMath.angleDifference(vector1, vector2).orElse(0.0);
				assertThat(diff).isCloseTo(diff1, within(1e-6));
				assertThat(diff).isBetween(-AngleMath.PI, AngleMath.PI);
			}
		}
	}
	
	
	@Test
	public void testNearestPointInList()
	{
		Random rnd = new Random(239827);
		for (int times = 0; times < 1e5; ++times)
		{
			List<IVector2> c = new ArrayList<>();
			for (int i = rnd.nextInt(50) + 1; i > 0; --i)
			{
				double length = rnd.nextDouble() * 1000;
				IVector2 vector = Vector2.fromAngle(rnd.nextDouble() * AngleMath.PI_TWO).scaleToNew(length);
				c.add(vector);
			}
			final IVector2 expected = Collections.min(c, new VectorLengthComparator());
			final IVector2 result = VectorMath.nearestTo(AVector2.ZERO_VECTOR, c);
			assertTrue(result.isCloseTo(expected, 1e-6));
		}
	}
	
	
	@Test
	public void testFarthestPointInList()
	{
		Random rnd = new Random(239827);
		for (int times = 0; times < 1e5; ++times)
		{
			List<IVector2> c = new ArrayList<>();
			for (int i = rnd.nextInt(50) + 1; i > 0; --i)
			{
				double length = rnd.nextDouble() * 1000;
				IVector2 vector = Vector2.fromAngle(rnd.nextDouble() * AngleMath.PI_TWO).scaleToNew(length);
				c.add(vector);
			}
			final IVector2 expected = Collections.max(c, new VectorLengthComparator());
			final IVector2 result = VectorMath.farthestTo(AVector2.ZERO_VECTOR, c);
			assertTrue(result.isCloseTo(expected, 1e-6));
		}
	}
}
