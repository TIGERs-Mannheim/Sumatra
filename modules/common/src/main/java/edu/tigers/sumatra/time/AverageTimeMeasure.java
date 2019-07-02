/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.time;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.lang.Validate;


/**
 * Measure times and build an average over a defined duration (averagingTime=1s)
 */
public class AverageTimeMeasure
{
	private final Deque<Long> measurements = new ConcurrentLinkedDeque<>();
	private final Deque<Long> measureTimes = new ConcurrentLinkedDeque<>();
	private double averagingTime = 1;
	private Long tStart = null;
	
	
	/**
	 * Make sure no measurement is running
	 */
	public void resetMeasure()
	{
		tStart = null;
	}
	
	
	/**
	 * Start a new measurement
	 */
	public void startMeasure()
	{
		Validate.isTrue(tStart == null, "Measurement already started!");
		tStart = System.nanoTime();
	}
	
	
	/**
	 * Stop the current measurement
	 */
	public void stopMeasure()
	{
		Validate.notNull(tStart, "Measurement not started!");
		long now = System.nanoTime();
		Long measurement = now - tStart;
		tStart = null;
		measurements.add(measurement);
		measureTimes.add(now);
		while ((now - measureTimes.peekFirst()) / 1e9 > averagingTime)
		{
			measurements.poll();
			measureTimes.poll();
		}
	}
	
	
	/**
	 * @return the latest measurement time
	 */
	public double getLatestMeasureTime()
	{
		final Long latest = measurements.peekLast();
		if (latest == null)
		{
			return 0.0;
		}
		return latest / 1e9;
	}
	
	
	/**
	 * @return the average measure time
	 */
	public double getAverageTime()
	{
		return measurements.stream().mapToDouble(d -> (double) d).average().orElse(0) / 1e9;
	}
	
	
	/**
	 * @return the max measure time
	 */
	public double getMaxTime()
	{
		return measurements.stream().mapToDouble(d -> (double) d).max().orElse(0) / 1e9;
	}
	
	
	/**
	 * @param averagingTime the time window in which the measurements will be averaged
	 */
	public void setAveragingTime(final double averagingTime)
	{
		this.averagingTime = averagingTime;
	}
}
