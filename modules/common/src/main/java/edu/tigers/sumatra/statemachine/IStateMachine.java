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
 * @param <StateType> like {@link IState} or {@link IRoleState}
 */
public interface IStateMachine<StateType>
{
	/**
	 */
	void update();
	
	
	/**
	 * Enqueue an event and let the state machine
	 * transit to the next state on next update
	 * 
	 * @param event
	 */
	void triggerEvent(Enum<? extends Enum<?>> event);
	
	
	/**
	 * @param currentState the currentState to set
	 */
	void setInitialState(StateType currentState);
	
	
	/**
	 * @return
	 */
	StateType getCurrentStateId();
	
	
	/**
	 * @param esp
	 * @param state
	 */
	void addTransition(final EventStatePair esp, final StateType state);
	
	
	/**
	 * Check the current state for completeness
	 * 
	 * @return
	 */
	boolean valid();
	
	
	/**
	 * Restart the state machine with the initial state
	 */
	void restart();
	
	
	/**
	 */
	void stop();
}
