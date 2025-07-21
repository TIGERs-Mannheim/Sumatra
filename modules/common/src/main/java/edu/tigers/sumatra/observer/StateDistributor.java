/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import edu.tigers.sumatra.observer.StateSubscriber.StateChangeObserver;
import lombok.NoArgsConstructor;


/**
 * State distributor that notifies all registered observers when the state changes.
 *
 * @param <T> the type of the state
 */
@NoArgsConstructor
public class StateDistributor<T> extends BasicDistributor<StateChangeObserver<T>> implements StateSubscriber<T>
{
	private T oldState;
	private T state;


	public StateDistributor(T state)
	{
		this.state = state;
	}


	@Override
	public synchronized void subscribe(String id, StateChangeObserver<T> consumer)
	{
		super.subscribe(id, consumer);
		if (state != null)
		{
			// Notify the new consumer with the current state
			consumer.onStateChange(oldState, state);
		}
	}


	/**
	 * Notify all registered observers with the given state.
	 *
	 * @param newState the state
	 */
	public synchronized void set(T newState)
	{
		oldState = state;
		state = newState;

		getConsumers().values().forEach(consumer -> consumer.onStateChange(oldState, state));
	}


	public synchronized T get()
	{
		return state;
	}


	@Override
	public synchronized void clear()
	{
		super.clear();
		state = null;
		oldState = null;
	}
}
