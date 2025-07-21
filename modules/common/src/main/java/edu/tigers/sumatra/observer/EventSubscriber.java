/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import java.util.function.Consumer;


/**
 * Allow subscribing to continuously incoming events.
 *
 * @param <T> the type of the event
 */
public interface EventSubscriber<T>
{
	/**
	 * Subscribe to new events.
	 *
	 * @param id
	 * @param consumer the consumer to be notified
	 */
	void subscribe(String id, Consumer<T> consumer);

	/**
	 * Unsubscribe from new events.
	 *
	 * @param id
	 */
	void unsubscribe(String id);
}
