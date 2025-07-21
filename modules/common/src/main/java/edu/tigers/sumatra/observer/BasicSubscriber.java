/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

/**
 * Allow subscribing to something.
 *
 * @param <T> the type of the consumer
 */
public interface BasicSubscriber<T>
{
	/**
	 * Subscribe to something.
	 *
	 * @param id       the id of the subscription needed for unsubscribing
	 * @param consumer the consumer to be notified
	 */
	void subscribe(String id, T consumer);

	/**
	 * Unsubscribe from something.
	 *
	 * @param id the id of the subscription used during subscription
	 */
	void unsubscribe(String id);
}
