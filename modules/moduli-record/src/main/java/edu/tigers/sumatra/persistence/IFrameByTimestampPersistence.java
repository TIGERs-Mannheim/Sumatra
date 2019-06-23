/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 9, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistence;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IFrameByTimestampPersistence
{
	
	/**
	 * @return
	 */
	Long getFirstKey();
	
	
	/**
	 * @return
	 */
	Long getLastKey();
	
	
	/**
	 * @param tCur
	 * @return
	 */
	Long getKey(final long tCur);
	
	
	/**
	 * @param key
	 * @return
	 */
	Long getNextKey(long key);
	
	
	/**
	 * @param lastKey
	 * @return
	 */
	Long getPreviousKey(long lastKey);
}
