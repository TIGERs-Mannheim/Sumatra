/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.model.ballspeed;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.BallKickFitState;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author "Lukas Magel"
 */
public class BallSpeedModel
{
	private double lastBallSpeed = 0.0d;
	private double lastEstimatedBallSpeed = 0.0d;
	
	private GameState lastState = GameState.HALT;
	private boolean gameStateChanged = false;
	
	
	/**
	 * @param wFrameWrapper
	 */
	public void update(final WorldFrameWrapper wFrameWrapper)
	{
		GameState curState = wFrameWrapper.getGameState();
		
		if (!curState.equals(lastState))
		{
			gameStateChanged = true;
		}
		
		lastBallSpeed = wFrameWrapper.getSimpleWorldFrame().getBall().getVel3().getLength();
		lastEstimatedBallSpeed = wFrameWrapper.getSimpleWorldFrame().getKickFitState()
				.map(BallKickFitState::getKickVel)
				.map(IVector3::getLength).orElse(0.0);
		
		lastState = curState;
		
	}
	
	
	/**
	 * 
	 */
	public void reset()
	{
		gameStateChanged = false;
	}
	
	
	/**
	 * @return
	 */
	public double getLastBallSpeed()
	{
		return lastBallSpeed;
	}
	
	
	/**
	 * @return
	 */
	public GameState getLastState()
	{
		return lastState;
	}
	
	
	/**
	 * @return
	 */
	public boolean hasGameStateChanged()
	{
		return gameStateChanged;
	}
	
	
	public double getLastEstimatedBallSpeed()
	{
		return lastEstimatedBallSpeed;
	}
}
