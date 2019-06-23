/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
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
	 * @return an optional identifier for this state
	 */
	String getIdentifier();
}
