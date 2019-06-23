/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.data;


public interface ITimestampBased
{
	/**
	 * @return the timestamp based on {@link System#nanoTime()}
	 */
	long getTimestamp();
}
