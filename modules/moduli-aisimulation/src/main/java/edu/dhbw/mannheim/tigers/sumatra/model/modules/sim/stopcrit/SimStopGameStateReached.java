/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 9, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.stopcrit;

import edu.tigers.sumatra.ai.data.EGameStateTeam;


/**
 * Stop simulation if game state reached
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimStopGameStateReached extends ASimStopCriterion
{
	private final EGameStateTeam	gameState;
	
	
	/**
	 * @param gameState
	 */
	public SimStopGameStateReached(final EGameStateTeam gameState)
	{
		this.gameState = gameState;
	}
	
	
	@Override
	protected boolean checkStopSimulation()
	{
		return getLatestFrame().getTacticalField().getGameState() == gameState;
	}
}
