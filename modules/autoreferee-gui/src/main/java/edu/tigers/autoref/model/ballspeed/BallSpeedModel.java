/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 31, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.model.ballspeed;

import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author "Lukas Magel"
 */
public class BallSpeedModel
{
	private double					lastBallSpeed		= 0.0d;
	
	private EGameStateNeutral	lastState			= EGameStateNeutral.UNKNOWN;
	private boolean				gameStateChanged	= false;
	
	
	/**
	 * 
	 */
	public BallSpeedModel()
	{
	}
	
	
	/**
	 * @param wFrameWrapper
	 */
	public void update(final WorldFrameWrapper wFrameWrapper)
	{
		EGameStateNeutral curState = wFrameWrapper.getGameState();
		
		if (curState != lastState)
		{
			gameStateChanged = true;
		}
		
		lastBallSpeed = wFrameWrapper.getSimpleWorldFrame().getBall().getVel().getLength();
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
	public EGameStateNeutral getLastState()
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
}
