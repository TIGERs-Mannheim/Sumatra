/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math;

import edu.tigers.sumatra.ai.metis.targetrater.HorizonCubicReductionCalculator;
import org.assertj.core.data.Offset;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;


public class HorizonCubicReductionCalculatorTest
{
	@Test
	public void testStartSlope()
	{
		HorizonCubicReductionCalculator calc = new HorizonCubicReductionCalculator();
		double x1 = calc.reduceHorizon(0);
		double x2 = calc.reduceHorizon(0.05);
		double slope = (x2 - x1) / 0.05;
		assertThat(slope).isBetween(0.9, 1.0);
	}

	@Test
	public void testStart()
	{
		HorizonCubicReductionCalculator calc = new HorizonCubicReductionCalculator();
		assertThat(calc.reduceHorizon(0)).isCloseTo(0, Offset.offset(0.0001));
	}

	@Test
	public void testNegtiveValues()
	{
		HorizonCubicReductionCalculator calc = new HorizonCubicReductionCalculator();
		assertThat(calc.reduceHorizon(-1)).isCloseTo(0, Offset.offset(0.0001));
		assertThat(calc.reduceHorizon(-0.025)).isCloseTo(0, Offset.offset(0.0001));
		assertThat(calc.reduceHorizon(-3)).isCloseTo(0, Offset.offset(0.0001));
		assertThat(calc.reduceHorizon(-6)).isCloseTo(0, Offset.offset(0.0001));
	}

	@Test
	public void testSteadyIncrease()
	{
		HorizonCubicReductionCalculator calc = new HorizonCubicReductionCalculator();
		var values = IntStream.range(0, 100).mapToDouble(x -> calc.reduceHorizon(x * 0.1)).toArray();
		for (int i = 0; i < values.length - 1; i++)
		{
			assertThat(values[i]).isLessThan(values[i+1]);
		}
	}
}
