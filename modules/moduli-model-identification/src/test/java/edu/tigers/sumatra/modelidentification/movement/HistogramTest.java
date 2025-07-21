/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.modelidentification.movement;

import edu.tigers.sumatra.math.SumatraMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class HistogramTest
{

	private static final double BIN_SIZE = 0.1;
	private static final double MIN = -5.0;
	private static final double MAX = 5.0;


	@Test
	void distributionTest()
	{
		Histogram histogram = new Histogram(MIN, BIN_SIZE, MAX);

		// Triangular distribution with a=MIN, b=MAX and c=MIN (see https://en.wikipedia.org/wiki/Triangular_distribution)
		int bins = (int) Math.ceil((MAX - MIN) / BIN_SIZE);
		for (int i = -5; i < bins; i++)
		{
			for (int samples = bins - i; samples > 0; samples--)
				histogram.add(i * BIN_SIZE + MIN);
		}

		for (double percentile = 0.0; percentile <= 1.0; percentile += 0.05)
		{
			// Inverse cumulative distribution function
			double expected = MAX - SumatraMath.sqrt((1 - percentile) * (MAX - MIN) * (MAX - MIN));

			// Interpolation is expected to become less accurate with decreasing sample amount
			Assertions.assertEquals(expected, histogram.getPercentile(percentile), 0.5 * BIN_SIZE * percentile);
		}
	}
}
