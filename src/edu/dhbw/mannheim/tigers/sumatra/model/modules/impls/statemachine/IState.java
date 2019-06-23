/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 3, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine;


/**
 * State of a state machine
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IState
{
	/**
	 * Called once on state entrance
	 */
	void doEntryActions();
	
	
	/**
	 * Called once on state exit
	 */
	void doExitActions();
	
	
	/**
	 * Called continuously with each new aiFrame
	 */
	void doUpdate();
	
	
	/**
	 * return a unique identifier for this state
	 * 
	 * @return
	 */
	Enum<? extends Enum<?>> getIdentifier();
}
