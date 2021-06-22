/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statemachine;


import java.util.Map;


/**
 * Interface for a state machine
 *
 * @param <T> like {@link IState}
 */
public interface IStateMachine<T extends IState>
{
	/**
	 * Perform an update step. This will update the current state.
	 */
	void update();


	/**
	 * Trigger an event. If a state is found for the event, the state machine will change to the new state
	 * immediately.
	 *
	 * @param event to be triggered
	 */
	void triggerEvent(IEvent event);


	/**
	 * Set the initial state (can not be called after the state machine has been started.
	 *
	 * @param currentState the currentState to set
	 */
	void setInitialState(T currentState);


	/**
	 * @return the current state
	 */
	T getCurrentState();


	/**
	 * @param currentState the state for which the transition should be triggered, can be null for wildcard
	 * @param event the event that triggers the transition
	 * @param state the resulting state
	 */
	void addTransition(final IState currentState, final IEvent event, final T state);


	/**
	 * Immediately change to given state
	 *
	 * @param newState
	 */
	void changeState(T newState);


	/**
	 * Stop the state machine. If there is an active state, it will be stopped.
	 */
	void stop();


	/**
	 * @param extendedLogging if true, log a bit more, like events and state changes.
	 */
	void setExtendedLogging(boolean extendedLogging);


	/**
	 * Set a name to be used in logging.
	 *
	 * @param name
	 */
	void setName(String name);

	/**
	 *
	 * @return statemachine graph
	 */
	Map<IEvent, Map<IState, T>> getTransitions();
}
