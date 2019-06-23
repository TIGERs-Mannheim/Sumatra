/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.04.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.clock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;


/**
 * A few supporting function for threading
 * 
 * @author Gero
 */
public final class ThreadUtil
{
	
	private ThreadUtil()
	{
	
	}
	
	
	/**
	 * Uses {@link LockSupport#parkNanos(long)} to send this thread to sleep <i>safely</i>. That is, this thread is
	 * guaranteed to really sleep the given time.
	 * <p>
	 * <b>NOTE: Calling {@link LockSupport#unpark(Thread)} on this thread has no effect!!! </b>
	 * </p>
	 * 
	 * @param sleepTotal The time this thread should sleep [ns]
	 */
	public static void parkNanosSafe(final long sleepTotal)
	{
		final long sleepStart = System.nanoTime();
		long stillSleep = sleepTotal;
		
		do
		{
			LockSupport.parkNanos(stillSleep);
			long timeSinceStart = System.nanoTime() - sleepStart;
			stillSleep = sleepTotal - timeSinceStart;
		} while (stillSleep > 0);
	}
	
	
	/**
	 * Uses {@link LockSupport#parkNanos(long)} to send this thread to sleep <i>safely</i>. That is, this thread is
	 * guaranteed to really sleep the given time. Sleep may be interrupted by setting the cancelSwitch to
	 * <code>true</code> and then calling {@link LockSupport#unpark(Thread)}.
	 * 
	 * @param sleepTotal The time this thread should sleep [ns]
	 * @param cancelSwitch This can be used to cancel the sleep by setting it to <code>true</code>
	 * @return Whether the thread really slept enough or it has been canceled
	 */
	public static boolean parkNanosSafe(final long sleepTotal, final AtomicBoolean cancelSwitch)
	{
		final long sleepStart = System.nanoTime();
		long stillSleep = sleepTotal;
		
		boolean sleptEnough = false;
		
		do
		{
			// Sleep
			LockSupport.parkNanos(stillSleep);
			
			// Check:
			if (cancelSwitch.get())
			{
				return sleptEnough;
			}
			
			// Check if sleptEnough
			long timeSinceStart = System.nanoTime() - sleepStart;
			stillSleep = sleepTotal - timeSinceStart;
			sleptEnough = stillSleep <= 0;
			
		} while (!sleptEnough);
		
		return sleptEnough;
	}
}
