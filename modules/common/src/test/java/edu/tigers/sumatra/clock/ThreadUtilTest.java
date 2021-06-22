/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.clock;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests methods in {@link ThreadUtil}
 *
 * @author Gero
 */
public class ThreadUtilTest
{
	/**
	 * Test method for {@link edu.tigers.sumatra.clock.ThreadUtil#parkNanosSafe(long)}.
	 */
	@Test
	public void testParkNanosSafeLong()
	{
		final long sleepFor = 50;

		for (int i = 0; i < 10; i++)
		{
			final long start = System.nanoTime();
			ThreadUtil.parkNanosSafe(sleepFor);
			final long stop = System.nanoTime();

			final long duration = stop - start;
			assertTrue("Not slept enough: " + duration + "ns < " + sleepFor + "ns!!!", duration > sleepFor);
		}
	}
}
