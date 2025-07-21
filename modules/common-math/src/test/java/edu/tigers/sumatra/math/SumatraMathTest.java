/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * Test for AIMath functions
 * you may extend it as appropriate
 */
class SumatraMathTest
{
	@Test
	void testHasDigitsAfterDecimal()
	{
		assertThat(SumatraMath.hasDigitsAfterDecimalPoint(1.0f)).isFalse();
		assertThat(SumatraMath.hasDigitsAfterDecimalPoint(1.22f)).isTrue();
		assertThat(SumatraMath.hasDigitsAfterDecimalPoint(-1.02f)).isTrue();
		assertThat(SumatraMath.hasDigitsAfterDecimalPoint(-1.0f)).isFalse();
	}


	@Test
	void testIsBetween()
	{
		assertThat(SumatraMath.isBetween(-2, -10, 0)).isTrue();
		assertThat(SumatraMath.isBetween(5, -10, 10)).isTrue();
		assertThat(SumatraMath.isBetween(-20, -10, 10)).isFalse();
		assertThat(SumatraMath.isBetween(15, -10, 10)).isFalse();
		assertThat(SumatraMath.isBetween(10, -10, 10)).isTrue();
		assertThat(SumatraMath.isBetween(0, 0, 0)).isTrue();
		assertThat(SumatraMath.isBetween(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)).isTrue();
	}


	@Test
	void testSqrt()
	{
		assertThat(SumatraMath.sqrt(0)).isCloseTo(Math.sqrt(0), Assertions.withinPercentage(0.1));
		assertThat(SumatraMath.sqrt(1)).isCloseTo(Math.sqrt(1), Assertions.withinPercentage(0.1));
		assertThat(SumatraMath.sqrt(7)).isCloseTo(Math.sqrt(7), Assertions.withinPercentage(0.1));
		assertThat(SumatraMath.sqrt(65165161465.0)).isCloseTo(Math.sqrt(65165161465.0),
				Assertions.withinPercentage(0.1));
		assertThat(SumatraMath.sqrt(1e-10)).isCloseTo(Math.sqrt(1e-10), Assertions.withinPercentage(0.1));
	}


	@Test
	void testMin()
	{
		assertThat(SumatraMath.min(1)).isEqualTo(1.0);
		assertThat(SumatraMath.min(1, 2)).isEqualTo(1.0);
		assertThat(SumatraMath.min(1, 2, 3)).isEqualTo(1.0);
		assertThat(SumatraMath.min(3, 2, 1)).isEqualTo(1.0);
		assertThat(SumatraMath.min(3, 1, 2)).isEqualTo(1.0);
		assertThat(SumatraMath.min(-1, 2, 3)).isEqualTo(-1.0);
		assertThat(SumatraMath.min(1, 2, -1)).isEqualTo(-1.0);
	}


	@Test
	void testMax()
	{
		assertThat(SumatraMath.max(1)).isEqualTo(1.0);
		assertThat(SumatraMath.max(1, 2)).isEqualTo(2.0);
		assertThat(SumatraMath.max(1, 2, 3)).isEqualTo(3.0);
		assertThat(SumatraMath.max(3, 2, 1)).isEqualTo(3.0);
		assertThat(SumatraMath.max(1, 3, 2)).isEqualTo(3.0);
		assertThat(SumatraMath.max(-1, 2, 3)).isEqualTo(3.0);
		assertThat(SumatraMath.max(1, 2, -1)).isEqualTo(2.0);
	}


	@Test
	void testIsEqual()
	{
		assertThat(SumatraMath.isEqual(1, 1)).isTrue();
		assertThat(SumatraMath.isEqual(1, 2)).isFalse();

		assertThat(SumatraMath.isEqual(1, 1, 0)).isTrue();
		assertThat(SumatraMath.isEqual(1, 2, 0)).isFalse();
		assertThat(SumatraMath.isEqual(1, 1.0001, 0)).isFalse();
		assertThat(SumatraMath.isEqual(1, 1.0001, 0.0001)).isTrue();
		assertThat(SumatraMath.isEqual(-1, -1, 0)).isTrue();
		assertThat(SumatraMath.isEqual(-1, -1.2, 1)).isTrue();
		assertThat(SumatraMath.isEqual(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0)).isFalse();
	}


	@Test
	void testIsZero()
	{
		assertThat(SumatraMath.isZero(0.0)).isTrue();
		assertThat(SumatraMath.isZero(1e-10)).isTrue();
		assertThat(SumatraMath.isZero(1)).isFalse();
		assertThat(SumatraMath.isZero(-1)).isFalse();
		assertThat(SumatraMath.isZero(0.1)).isFalse();
		assertThat(SumatraMath.isZero(0.001)).isFalse();
	}


	@Test
	void testRelative()
	{
		assertThat(SumatraMath.relative(0.5, 0, 1)).isCloseTo(0.5, within(1e-10));
		assertThat(SumatraMath.relative(0.0, 0, 1)).isCloseTo(0.0, within(1e-10));
		assertThat(SumatraMath.relative(1.0, 0, 1)).isCloseTo(1.0, within(1e-10));
		assertThat(SumatraMath.relative(0.0, 1, 0)).isCloseTo(1.0, within(1e-10));
		assertThat(SumatraMath.relative(5, 0, 10)).isCloseTo(0.5, within(1e-10));
		assertThat(SumatraMath.relative(2, 0, 10)).isCloseTo(0.2, within(1e-10));
		assertThat(SumatraMath.relative(6, 10, 5)).isCloseTo(0.8, within(1e-10));
		assertThat(SumatraMath.relative(-1, 0, 5)).isCloseTo(0, within(1e-10));
		assertThat(SumatraMath.relative(6, 0, 5)).isCloseTo(1, within(1e-10));
	}
}