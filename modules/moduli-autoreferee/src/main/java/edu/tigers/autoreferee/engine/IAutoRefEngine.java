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
	public enum AutoRefMode
	{
		/**  */
		ACTIVE,
		/**  */
		PASSIVE
	}
	
	
	/**
	 * 
	 */
	public void pause();
	
	
	/**
	 * 
	 */
	public void resume();
	
	
	/**
	 * 
	 */
	public void stop();
	
	
	/**
	 * 
	 */
	public void reset();
	
	
	/**
	 * @return
	 */
	public AutoRefMode getMode();
	
	
	/**
	 * @return
	 */
	public IGameLog getGameLog();
	
	
	/**
	 * @param types
	 */
	public void setActiveGameEvents(Set<EGameEventDetectorType> types);
	
	
	/**
	 * @param frame
	 */
	public void process(IAutoRefFrame frame);
}
