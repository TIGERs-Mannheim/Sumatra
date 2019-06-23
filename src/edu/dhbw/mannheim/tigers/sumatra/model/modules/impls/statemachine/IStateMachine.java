/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine;

import java.util.Map;


/**
 * Interface for updating a state machine
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <StateType> like {@link IState} or {@link IRoleState}
 * 
 */
public interface IStateMachine<StateType>
{
	/**
	 */
	void update();
	
	
	/**
	 * Enqueue an event and let the state machine
	 * transite to the next state on next update
	 * 
	 * @param event
	 */
	void nextState(Enum<? extends Enum<?>> event);
	
	
	/**
	 * @param currentState the currentState to set
	 */
	void setInitialState(StateType currentState);
	
	
	/**
	 * @return
	 */
	StateType getCurrentState();
	
	
	/**
	 * @return
	 */
	Map<EventStatePair, StateType> getTransititions();
	
	
	/**
	 * Check the current state for completeness
	 * 
	 * @return
	 */
	boolean valid();
}
