/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

/**
 * Allow subscribing to continuously incoming frames.
 * It is ensured that the observer is called with the latest frame and each new frame.
 * The observer is also notified when the frame is cleared.
 *
 * @param <T> the type of the frame
 */
public interface FrameSubscriber<T> extends EventSubscriber<T>
{
	/**
	 * Subscribe to clear frame.
	 *
	 * @param id
	 * @param runnable the runnable to be notified
	 */
	void subscribeClear(String id, Runnable runnable);

	/**
	 * Unsubscribe from clear frames.
	 *
	 * @param id
	 */
	void unsubscribeClear(String id);
}
