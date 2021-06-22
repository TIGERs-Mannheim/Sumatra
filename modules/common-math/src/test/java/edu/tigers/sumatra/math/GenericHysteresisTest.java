/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class GenericHysteresisTest
{
	private boolean lower = false;
	private boolean upper = false;
	private double currentValue = 0;


	@Test
	public void testSimple()
	{
		lower = false;
		upper = false;
		GenericHysteresis hysteresis = new GenericHysteresis(() -> lower, () -> upper);

		assertThat(hysteresis.isLower())
				.as("Initially lower").isTrue();

		hysteresis.update();
		assertThat(hysteresis.isLower())
				.as("No change")
				.isTrue();

		upper = true;
		hysteresis.update();
		assertThat(hysteresis.isUpper()).isTrue();

		upper = false;
		hysteresis.update();
		assertThat(hysteresis.isUpper()).isTrue();

		lower = true;
		hysteresis.update();
		assertThat(hysteresis.isLower()).isTrue();

		lower = false;
		hysteresis.update();
		assertThat(hysteresis.isLower()).isTrue();
	}


	@Test
	public void testDouble()
	{
		GenericHysteresis hysteresis = new GenericHysteresis(() -> currentValue < -1, () -> currentValue > 1);
		assertThat(hysteresis.isUpper())
				.as("Initially lower")
				.isFalse();
		currentValue = 0;
		hysteresis.update();
		assertThat(hysteresis.isUpper())
				.as("currentValue (0) < 1")
				.isFalse();
		currentValue = 2;
		hysteresis.update();
		assertThat(hysteresis.isUpper())
				.as("currentValue (2) > 1")
				.isTrue();
		currentValue = 0;
		hysteresis.update();
		assertThat(hysteresis.isUpper())
				.as("currentValue (0) < 1")
				.isTrue();
		currentValue = -2;
		hysteresis.update();
		assertThat(hysteresis.isUpper())
				.as("currentValue (-2) < -1")
				.isFalse();

	}
}