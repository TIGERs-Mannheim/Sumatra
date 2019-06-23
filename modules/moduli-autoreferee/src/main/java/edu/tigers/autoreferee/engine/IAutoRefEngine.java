/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.Set;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.IGameEventDetector.EGameEventDetectorType;
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
	void setActiveGameEvents(Set<EGameEventDetectorType> types);
	
	
	/**
	 * @param frame
	 */
	void process(IAutoRefFrame frame);
}
