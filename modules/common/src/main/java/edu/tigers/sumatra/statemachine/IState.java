/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.statemachine;


/**
 * State of a state machine.
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
	 * @return the name of this state
	 */
	default String getName()
	{
		return this.getClass().getSimpleName();
	}
}
