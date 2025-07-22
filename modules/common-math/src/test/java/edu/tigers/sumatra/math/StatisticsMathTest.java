/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
class StatisticsMathTest
{
	
	@Test
	void testMean()
	{
		Assertions.assertThat(StatisticsMath.mean(Arrays.asList(1))).isEqualTo(1);
		Assertions.assertThat(StatisticsMath.mean(Arrays.asList(1, 1))).isEqualTo(1);
		Assertions.assertThat(StatisticsMath.mean(Arrays.asList(1, 1, 1))).isEqualTo(1);
		Assertions.assertThat(StatisticsMath.mean(Arrays.asList(42, 42, 42))).isEqualTo(42);
		Assertions.assertThat(StatisticsMath.mean(Arrays.asList(42, 0))).isEqualTo(21);
		Assertions.assertThat(StatisticsMath.mean(Arrays.asList(-1, 1, 1, -1))).isEqualTo(0);
		Assertions.assertThat(StatisticsMath.mean(Arrays.asList(1, 2, 3, 4, 5, 6))).isCloseTo(21.0 / 6.0,
				Assertions.withinPercentage(0.01));
	}
	
	
	@Test
	void testVariance()
	{
		Assertions.assertThat(StatisticsMath.variance(Arrays.asList(42, 42, 42))).isEqualTo(0);
		Assertions.assertThat(StatisticsMath.variance(Arrays.asList(43, 42, 41))).isEqualTo(2.0 / 3.0);
	}
	
	
	@Test
	void testStd()
	{
		Assertions.assertThat(StatisticsMath.std(Arrays.asList(12, 19, 42, 1337))).isCloseTo(
				SumatraMath.sqrt(StatisticsMath.variance(Arrays.asList(12, 19, 42, 1337))),
				Assertions.withinPercentage(0.01));
	}
}
