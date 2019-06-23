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
 * Fake the time with a simulated dt
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationClock implements IClock
{
	private long			offsetMillis;
	private long			offsetNanos;
	private final double	dt;
	
	
	/**
	 * @param dt the artificial dt
	 */
	public SimulationClock(final double dt)
	{
		this.dt = dt;
		syncWithRealTime();
	}
	
	
	/**
	 * Reset time offsets. Be careful as application could misbehave because of unreasonable time changes
	 */
	public final void syncWithRealTime()
	{
		offsetMillis = System.currentTimeMillis();
		offsetNanos = System.nanoTime();
	}
	
	
	@Override
	public long currentTimeMillis()
	{
		long elapsed = (long) ((System.currentTimeMillis() - offsetMillis) * dt);
		return offsetMillis + elapsed;
	}
	
	
	@Override
	public long nanoTime()
	{
		long elapsed = (long) ((System.nanoTime() - offsetNanos) * dt);
		return offsetNanos + elapsed;
	}
	
	
	@Override
	public void sleep(final long millis)
	{
		ThreadUtil.parkNanosSafe((long) (millis * dt * 1e6));
	}
}
