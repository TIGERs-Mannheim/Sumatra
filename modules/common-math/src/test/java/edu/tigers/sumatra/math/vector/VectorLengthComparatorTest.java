/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorLengthComparatorTest
{
	
	@Test
	public void testCompare()
	{
		List<IVector2> points = new ArrayList<>();
		Random rnd = new Random(43);
		for (int i = 0; i < 100; i++)
		{
			points.add(Vector2.fromXY(rnd.nextDouble() * 5000 - 2500, rnd.nextDouble() * 5000 - 2500));
		}
		points.add(Vector2.zero());
		points.add(Vector2.zero());
		
		points.sort(new VectorLengthComparator());
		double smallestValue = -1;
		for (IVector2 p : points)
		{
			double len = p.getLength2();
			assertThat(len).isGreaterThanOrEqualTo(smallestValue);
			smallestValue = len;
		}
	}
}
