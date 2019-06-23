/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statemachine;


/**
 * Interface for updating a state machine
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <T> like {@link IState}
 */
public interface IStateMachine<T extends IState>
{
	/**
	 * Perform an update step.
	 * This processes all events that were enqueued.
	 */
	void update();
	
	
	/**
	 * Enqueue an event and let the state machine
	 * transit to the next state on next update
	 *
	 * @param event to be triggered
	 */
	void triggerEvent(IEvent event);
	
	
	/**
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
	 * Check the current state for completeness
	 * 
	 * @return true, if the state machine is valid
	 */
	boolean valid();
	
	
	/**
	 * Restart the state machine with the initial state
	 */
	void restart();
	
	
	/**
	 * Stop the state machine. If there is an active state, it will be stopped.
	 */
	void stop();
	
	
	/**
	 * @param extendedLogging if true, log some more events as warning
	 */
	void setExtendedLogging(boolean extendedLogging);
	
	
	/**
	 * @param queueSize number of events to queue and process after each other per update step
	 */
	void setQueueSize(int queueSize);
}
