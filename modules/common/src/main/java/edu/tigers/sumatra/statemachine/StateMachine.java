/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.statemachine;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.Map;


/**
 * Generic state machine.
 *
 * @param <T>
 */
@Log4j2
public class StateMachine<T extends IState> implements IStateMachine<T>
{
	private static final int MAX_STATE_CHANGES_PER_UPDATE = 15;

	@Getter
	private final Map<IEvent, Map<IState, T>> transitions = new HashMap<>();

	@Setter
	private String name;

	@Setter
	private boolean enableTransitions = true;

	private T currentState = null;
	private boolean initialStateInitialized = false;
	private int stateChangesSinceUpdate = 0;


	public StateMachine(String name)
	{
		this.name = name;
	}


	private T getNextState(IEvent event)
	{
		Validate.notNull(currentState, "StateMachine not initialized");
		Map<IState, T> subTransitions = transitions.computeIfAbsent(event, k -> new HashMap<>());
		return subTransitions.getOrDefault(currentState, subTransitions.get(null));
	}


	@Override
	public void update()
	{
		if (!initialStateInitialized && currentState != null)
		{
			// initialize the very first state
			currentState.doEntryActions();
			initialStateInitialized = true;
		}
		stateChangesSinceUpdate = 0;
		if (currentState != null)
		{
			currentState.doUpdate();
		}
	}


	@Override
	public void changeState(final T newState)
	{
		if (!enableTransitions || newState.equals(currentState))
		{
			return;
		}

		log.trace("{}: Switch state from {} to {}", name, currentState, newState);

		if (stateChangesSinceUpdate > MAX_STATE_CHANGES_PER_UPDATE)
		{
			log.warn("Number of allowed state changes exceeded. Possible state loop! Last change from {} to {}.",
					currentState, newState, new Exception());
			return;
		}
		stateChangesSinceUpdate++;

		currentState.doExitActions();

		// the order of the following statements matters:
		// in both methods, one could trigger another state change, resulting in a recursive call of this method.
		// by setting the currentState first, the last call to changeState will have priority
		// by calling the methods on `newState` instead of `currentState`, it is guaranteed, that the new state
		// is called, not a future currentState.
		currentState = newState;
		newState.doEntryActions();
		newState.doUpdate();
	}


	@Override
	public void stop()
	{
		if (currentState != null)
		{
			currentState.doExitActions();
		}
		currentState = null;
	}


	@Override
	public void triggerEvent(final IEvent event)
	{
		Validate.notNull(event);
		if (!enableTransitions)
		{
			return;
		}

		log.trace("{}: New event: {}", name, event);

		T newState = getNextState(event);
		if (newState != null)
		{
			changeState(newState);
		}
	}


	@Override
	public final T getCurrentState()
	{
		return currentState;
	}


	@Override
	public final void addTransition(final IState currentState, final IEvent event, final T state)
	{
		Validate.notNull(event);
		Validate.notNull(state);
		Map<IState, T> subTransitions = transitions.computeIfAbsent(event, k -> new HashMap<>());
		T preState = subTransitions.put(currentState, state);
		if (preState != null)
		{
			log.warn("{}: Overwriting transition for event: {} and state {}. Change state from {} to {}", name, event,
					currentState, preState, state);
		}
	}


	@Override
	public final void setInitialState(final T currentState)
	{
		if (this.currentState != null && initialStateInitialized)
		{
			throw new IllegalStateException("Already initialized");
		}
		this.currentState = currentState;
	}
}
