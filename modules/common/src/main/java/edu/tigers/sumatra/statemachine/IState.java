/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 3, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statemachine;


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
	default void doEntryActions()
	{
	}
	
	
	/**
	 * Called once on state exit
	 */
	default void doExitActions()
	{
	}
	
	
	/**
	 * Called continuously with each new aiFrame
	 */
	default void doUpdate()
	{
	}
	
	
	/**
	 * @return an optional identifier for this state, defaults to the class name
	 */
	default String getIdentifier()
	{
		return this.getClass().getSimpleName();
	}
}
