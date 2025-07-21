/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

/**
 * An interface for all persistence stores
 */
public interface IPersistenceRecorder
{
	/**
	 * Start recorder
	 */
	void start();
	
	
	/**
	 * Stop recorder
	 */
	void stop();
	
	
	/**
	 * Flush all buffered data
	 */
	void flush();
}
