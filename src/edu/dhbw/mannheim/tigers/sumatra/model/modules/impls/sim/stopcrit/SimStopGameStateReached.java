/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 9, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.stopcrit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;


/**
 * Stop simulation if game state reached
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimStopGameStateReached extends ASimStopCriterion
{
	private final EGameState	gameState;
	
	
	/**
	 * @param gameState
	 */
	public SimStopGameStateReached(final EGameState gameState)
	{
		this.gameState = gameState;
	}
	
	
	@Override
	protected boolean checkStopSimulation()
	{
		return getLatestFrame().getTacticalField().getGameState() == gameState;
	}
}
