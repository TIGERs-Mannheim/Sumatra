/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
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
	List<GameLogEntry> getEntries();
	
	
	/**
	 * Clear all entries
	 */
	void clearEntries();
	
	
	/**
	 * @param observer
	 */
	void addObserver(IGameLogObserver observer);
	
	
	/**
	 * @param observer
	 */
	void removeObserver(IGameLogObserver observer);
	
}