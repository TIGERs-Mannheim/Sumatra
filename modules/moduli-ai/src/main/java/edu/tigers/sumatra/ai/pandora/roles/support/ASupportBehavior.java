/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.statemachine.AState;


/**
 * This is the base class for all supportive behaviors.
 */
public abstract class ASupportBehavior extends AState
{
	private final ARole role;


	public ASupportBehavior(final ARole role)
	{
		super();
		this.role = role;
	}


	/**
	 * If the behavior is active in the current GameState,
	 * call calculateViability, else return 0
	 *
	 * @return calculateViability or 0.0
	 */
	public double getViability()
	{
		if (!isEnabledInCurrentGameState())
		{
			return 0.0;
		}
		return calculateViability();
	}



	/**
	 * Calculates the viability [0,1], whether this behavior could be appropriate for the given bot in this situation.
	 */
	protected abstract double calculateViability();

	/**
	 * Normally you want to return the static isActive configurable of the class
	 * @return true if the behaviour is currently enabled
	 */
	public abstract boolean getIsActive();

	/**
	 * This method checks whenever a behavior is enabled in a
	 * given game state. If a behavior should be used outside
	 * of the regular game, this method needs to be overridden.
	 *
	 * @return true if enabled, false otherwise
	 */
	protected boolean isEnabledInCurrentGameState()
	{
		GameState gameState = getRole().getAiFrame().getGameState();
		return gameState.isGameRunning()
				|| gameState.isStop()
				|| gameState.isBallPlacement();
	}

	protected ARole getRole()
	{
		return role;
	}
	
}

