/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

/**
 * Allow subscribing to state changes. The old state and the new state are passed to the observer.
 *
 * @param <T> the type of the state
 */
public interface StateSubscriber<T>
{
	/**
	 * Subscribe to state changes.
	 *
	 * @param id       a unique identifier for the consumer required for unsubscribing
	 * @param consumer the consumer to be notified
	 */
	void subscribe(String id, StateChangeObserver<T> consumer);

	/**
	 * Unsubscribe from state changes.
	 *
	 * @param id the unique identifier of the consumer used for subscribing
	 */
	void unsubscribe(String id);

	@FunctionalInterface
	interface StateChangeObserver<T>
	{
		/**
		 * Called when the state changes.
		 *
		 * @param oldState the old state
		 * @param newState the new state
		 */
		void onStateChange(T oldState, T newState);
	}
}
