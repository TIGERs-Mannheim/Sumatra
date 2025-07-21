/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.clock;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * Test class for FpsCounter
 */
public class FpsCounterTest
{
	@Test
	public void testSimple()
	{
		FpsCounter fpsCounter = new FpsCounter();
		for (int i = 0; i < 20; i++)
		{
			fpsCounter.newFrame(i * 100_000_000L);
		}
		assertThat(fpsCounter.getAvgFps()).isCloseTo(10, within(1e-10));
	}
}
