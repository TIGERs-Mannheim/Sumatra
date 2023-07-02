/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statemachine;

/**
 * A state with a default identifier
 */
public class AState implements IState
{
	@Override
	public String toString()
	{
		return getName();
	}
}
