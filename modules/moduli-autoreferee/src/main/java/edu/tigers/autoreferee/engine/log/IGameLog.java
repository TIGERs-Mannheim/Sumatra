/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 9, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.log;

import java.util.List;

import edu.tigers.autoreferee.engine.log.GameLog.IGameLogObserver;


/**
 * 
 * @author "Lukas Magel" 
 */
public interface IGameLog
{
	
	/**
	 * @return a read only view of the log entries
	 */
	public List<GameLogEntry> getEntries();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(IGameLogObserver observer);
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IGameLogObserver observer);
	
}