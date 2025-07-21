/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.clock;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.locks.LockSupport;


/**
 * A few supporting function for threading
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ThreadUtil
{
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

		while (stillSleep > 1)
		{
			// On some OS, the threads sleep too long, so we apply an exponential strategy
			LockSupport.parkNanos(stillSleep / 2);
			long timeSinceStart = System.nanoTime() - sleepStart;
			stillSleep = sleepTotal - timeSinceStart;
		}
	}
}
