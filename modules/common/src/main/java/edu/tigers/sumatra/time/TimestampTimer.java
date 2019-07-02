/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.time;

/**
 * A timestamp-based timer that can check if a predefined duration has running out
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TimestampTimer
{
	private long tStart = 0;
	private long duration = 0;
	
	
	/**
	 * Create a new timer
	 * 
	 * @param duration the duration of this timer [s]
	 */
	public TimestampTimer(final double duration)
	{
		setDuration(duration);
	}
	
	
	/**
	 * Reset the timer. Time is not running afterwards, until {@link TimestampTimer#start(long)} is called.
	 */
	public void reset()
	{
		tStart = 0;
	}
	
	
	/**
	 * Start the timer with given start time
	 * 
	 * @param tStart the starting timestamp [ns]
	 */
	public void start(long tStart)
	{
		this.tStart = tStart;
	}
	
	
	/**
	 * Start timer if not already started
	 * 
	 * @param tStart the start timestamp
	 */
	public void update(long tStart)
	{
		if (this.tStart == 0)
		{
			this.tStart = tStart;
		}
	}
	
	
	/**
	 * @param duration the duration in [s] after which time is up
	 */
	public void setDuration(double duration)
	{
		this.duration = (long) (duration * 1e9);
	}
	
	
	/**
	 * Check if time is up
	 * 
	 * @param curTimestamp the current time as timestamp [ns]
	 * @return true, if curTimestamp is newer than tStart + duration
	 */
	public boolean isTimeUp(long curTimestamp)
	{
		return tStart != 0 && (curTimestamp - tStart) > duration;
	}
	
	
	/**
	 * @param curTimestamp current timestamp
	 * @return the time from tStart to now in [s]
	 */
	public double getCurrentTime(long curTimestamp)
	{
		if (tStart == 0)
		{
			return 0;
		}
		return (curTimestamp - tStart) / 1e9;
	}
	
	
	/**
	 * @param curTimestamp current timestamp
	 * @return the remaining time of this timer
	 */
	public double getRemainingTime(long curTimestamp)
	{
		return getDuration() - getCurrentTime(curTimestamp);
	}
	
	
	public double getDuration()
	{
		return duration / 1e9;
	}
	
	
	/**
	 * @return true, if the timer is running (not reset)
	 */
	public boolean isRunning()
	{
		return tStart != 0;
	}
}
