/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.referee.source.refbox.time;

/**
 * @author AndreR <andre@ryll.cc>
 */
public class SystemTimeProvider implements ITimeProvider
{
	@Override
	public long getTimeInMicroseconds()
	{
		return System.nanoTime() / 1000;
	}
}
