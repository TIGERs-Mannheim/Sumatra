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
