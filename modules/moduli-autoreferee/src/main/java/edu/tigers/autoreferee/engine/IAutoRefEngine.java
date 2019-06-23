/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import java.util.Set;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.log.IGameLog;


/**
 * @author "Lukas Magel"
 */
public interface IAutoRefEngine
{
	/**
	 * @author "Lukas Magel"
	 */
	enum AutoRefMode
	{
		/**  */
		ACTIVE,
		/**  */
		PASSIVE
	}
	
	
	/**
	 * 
	 */
	void pause();
	
	
	/**
	 * 
	 */
	void resume();
	
	
	/**
	 * 
	 */
	void stop();
	
	
	/**
	 * 
	 */
	void reset();
	
	
	/**
	 * @return
	 */
	AutoRefMode getMode();
	
	
	/**
	 * @return
	 */
	IGameLog getGameLog();
	
	
	/**
	 * @param types
	 */
	void setActiveGameEventDetectors(Set<EGameEventDetectorType> types);
	
	
	void setActiveGameEvents(Set<EGameEvent> activeGameEvents);
	
	
	/**
	 * @param frame
	 */
	void process(IAutoRefFrame frame);
}
