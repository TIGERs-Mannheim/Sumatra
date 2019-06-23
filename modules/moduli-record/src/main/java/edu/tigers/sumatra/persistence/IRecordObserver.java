/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence;

/**
 * Interface for recordManager
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IRecordObserver
{
	
	/**
	 * @param recording
	 */
	void onStartStopRecord(boolean recording);
	
	
	/**
	 * @param persistence the open persistence reference
	 * @param startTime the initial time after opening the replay (timestamp in ns)
	 */
	default void onViewReplay(ABerkeleyPersistence persistence, long startTime)
	{
	}
	
}
