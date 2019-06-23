/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.timer;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DummyTimer implements ITimer
{
	@Override
	public void stop(final ETimable timable, final long id)
	{
	}
	
	
	@Override
	public void stop(final ETimable timable, final long id, final int customId)
	{
	}
	
	
	@Override
	public void start(final ETimable timable, final long id)
	{
	}
	
	
	@Override
	public void start(final ETimable timable, final long id, final int customId)
	{
	}
}
