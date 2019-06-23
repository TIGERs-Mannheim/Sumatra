/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.clock;

/**
 * Wrapper for time functions
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IClock
{
	/**
	 * @see System#currentTimeMillis()
	 * @return
	 */
	long currentTimeMillis();
	
	
	/**
	 * @see System#nanoTime()
	 * @return
	 */
	long nanoTime();
	
	
	/**
	 * @see Thread#sleep(long)
	 * @param millis
	 */
	void sleep(long millis);
}
