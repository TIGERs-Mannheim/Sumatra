/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.timer;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * This is the data structure for information the {@link edu.tigers.sumatra.timer.ATimer}
 * has to offer, durations and time measurements in
 * particular
 * 
 * @author Gero
 */
public class TimerInfo
{
	/** key -> (frameId -> duration) */
	private final Map<String, SortedMap<Long, Long>>	timings		= new ConcurrentHashMap<>();
	private static final int									BUFFER_SIZE	= 1000;
	
	private final Object											lock			= new Object();
	
	
	/**
	 * @param tId
	 * @param value
	 */
	public void putTiming(final TimerIdentifier tId, final long value)
	{
		synchronized (lock)
		{
			SortedMap<Long, Long> map = getOrCreateTimings(tId.getTimable());
			Long existingValue = map.get(tId.getId());
			if (existingValue != null)
			{
				map.put(tId.getId(), value + existingValue);
			} else
			{
				map.put(tId.getId(), value);
			}
			if (map.size() > BUFFER_SIZE)
			{
				map.remove(map.firstKey());
			}
		}
	}
	
	
	/**
	 * @return a set of all timeables keys
	 */
	public Set<String> getAllTimables()
	{
		return timings.keySet();
	}
	
	
	private SortedMap<Long, Long> getOrCreateTimings(final String timable)
	{
		return timings.computeIfAbsent(timable, k -> new ConcurrentSkipListMap<>());
	}
	
	
	/**
	 * @param timable
	 * @return the timings frameId -> duration
	 */
	public SortedMap<Long, Long> getTimings(final String timable)
	{
		SortedMap<Long, Long> copy;
		synchronized (lock)
		{
			SortedMap<Long, Long> map = getOrCreateTimings(timable);
			copy = new TreeMap<>(map);
		}
		return copy;
	}
	
	
	/**
	 * Clear all timings
	 */
	public void clear()
	{
		synchronized (lock)
		{
			timings.clear();
		}
	}
	
	
	/**
	 * Calculate average values over numFrames frames.
	 * 
	 * @param timable
	 * @param numFrames
	 * @param tStats
	 * @return
	 */
	public SortedMap<Long, Long> getCombinedTimings(final String timable, final int numFrames,
			final ETimerStatistic tStats)
	{
		SortedMap<Long, Long> sortedTimings = getTimings(timable);
		if (numFrames <= 1)
		{
			return sortedTimings;
		}
		SortedMap<Long, Long> avageres = new TreeMap<>();
		int counter = 0;
		long sum = 0;
		long max = 0;
		long min = Long.MAX_VALUE;
		for (Map.Entry<Long, Long> entry : sortedTimings.entrySet())
		{
			if (counter >= numFrames)
			{
				long value = 0;
				switch (tStats)
				{
					case AVG:
						value = sum / counter;
						break;
					case MAX:
						value = max;
						break;
					case MIN:
						value = min;
						break;
				}
				avageres.put(entry.getKey(), value);
				sum = 0;
				min = Long.MAX_VALUE;
				max = 0;
				counter = 0;
			}
			if (entry.getValue() > max)
			{
				max = entry.getValue();
			}
			if (entry.getValue() < min)
			{
				min = entry.getValue();
			}
			sum += entry.getValue();
			counter++;
		}
		// the last frames will not be included.
		return avageres;
	}
}
