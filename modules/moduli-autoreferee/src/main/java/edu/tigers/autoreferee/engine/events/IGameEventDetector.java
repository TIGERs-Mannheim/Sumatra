/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

import java.util.Optional;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * @author "Lukas Magel"
 */
public interface IGameEventDetector
{
	/**
	 * @param state
	 * @return
	 */
	boolean isActiveIn(EGameState state);
	
	
	/**
	 * @return
	 */
	int getPriority();
	
	
	/**
	 * @param frame
	 * @return
	 */
	Optional<IGameEvent> update(IAutoRefFrame frame);
	
	
	/**
	 * Reset
	 */
	void reset();
	
	
	/**
	 * @return the type of the game event detector
	 */
	EGameEventDetectorType getType();
}
