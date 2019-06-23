/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 4, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statemachine;


/**
 * Use this state to indicate that you are done
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DoneState implements IState, IRoleState
{
	/**
	 */
	public enum EStateId
	{
		/**  */
		DONE;
	}
	
	
	@Override
	public void doEntryActions()
	{
	}
	
	
	@Override
	public void doExitActions()
	{
	}
	
	
	@Override
	public void doUpdate()
	{
	}
	
	
	@Override
	public Enum<? extends Enum<?>> getIdentifier()
	{
		return EStateId.DONE;
	}
}
