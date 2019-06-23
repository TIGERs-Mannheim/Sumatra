/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistance;

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
