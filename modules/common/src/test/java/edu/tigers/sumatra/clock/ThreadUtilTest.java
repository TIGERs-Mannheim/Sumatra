/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
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
	
	
	/**
	 * Test method for
	 * {@link edu.tigers.sumatra.clock.ThreadUtil#parkNanosSafe(long, java.util.concurrent.atomic.AtomicBoolean)}
	 * .
	 */
	@Test
	@SuppressWarnings("squid:S2925") // Thread.sleep is used intentionally here
	public void testParkNanosSafeLongAtomicBoolean()
	{
		// ### Check result if canceled
		final long awakeAfter = 50; // [ms]

		for (int i = 0; i < 5; i++)
		{
			// Send to sleep
			Sleeper s = new Sleeper();
			s.start();
			
			// Wait a bit
			try
			{
				Thread.sleep(awakeAfter);
			} catch (InterruptedException err)
			{
				err.printStackTrace();
				fail(err.getMessage());
			}
			
			// Interrupt sleeper
			s.cancel.set(true);
			LockSupport.unpark(s);
			try
			{
				s.join();
			} catch (InterruptedException err)
			{
				err.printStackTrace();
				fail(err.getMessage());
			}
			
			// Check!
			assertTrue("Sleep not interrupted!", !s.result.get());
		}
		
		
		// ### Check result if not canceled
		for (int i = 0; i < 5; i++)
		{
			// Send to sleep
			Sleeper s = new Sleeper();
			s.start();
			
			// Wait for sleeper
			try
			{
				s.join();
			} catch (InterruptedException err)
			{
				err.printStackTrace();
				fail(err.getMessage());
			}
			
			// Check!
			assertTrue("Sleep interrupted!", s.result.get());
		}
	}
	
	
	private static class Sleeper extends Thread
	{
		private static final long		SLEEP_FOR	= TimeUnit.MILLISECONDS.toNanos(100);
		private final AtomicBoolean	cancel		= new AtomicBoolean(false);
		private final AtomicBoolean	result		= new AtomicBoolean();
		
		
		@Override
		public void run()
		{
			result.set(ThreadUtil.parkNanosSafe(SLEEP_FOR, cancel));
		}
	}
}
