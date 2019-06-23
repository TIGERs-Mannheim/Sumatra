/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.referee.source.refbox.time;

/**
 * @author AndreR <andre@ryll.cc>
 */
@FunctionalInterface
public interface ITimeProvider
{
	/**
	 * Get current time in microseconds
	 * 
	 * @return
	 */
	long getTimeInMicroseconds();
}
