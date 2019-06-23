/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 31, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.statemachine.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */

/**
 * Default idle state
 */
public class DefaultIdleState implements IState
{
	private static enum EStateId
	{
		DEFAULT
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
	public Enum<?> getIdentifier()
	{
		return EStateId.DEFAULT;
	}
}
