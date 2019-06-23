/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.timer;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DummyTimer implements ITimer
{
	@Override
	public void stop(final String timable, final long id)
	{
		// nothing to do
	}
	
	
	@Override
	public void stop(final String timable, final long id, final int customId)
	{
		// nothing to do
	}
	
	
	@Override
	public void start(final String timable, final long id)
	{
		// nothing to do
	}
	
	
	@Override
	public void start(final String timable, final long id, final int customId)
	{
		// nothing to do
	}
}
