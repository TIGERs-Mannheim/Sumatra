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


}
