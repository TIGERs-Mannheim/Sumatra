/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LogEventWatcher extends WriterAppender
{
	private Set<Level> watchedLevels = new HashSet<>();
	private Map<Level, Integer> numEvents = new HashMap<>();
	
	
	/**
	 * Create a log appender that is looking for certain log events
	 * 
	 * @param levels to watch
	 */
	public LogEventWatcher(Level... levels)
	{
		watchedLevels.addAll(Arrays.asList(levels));
	}
	
	
	@Override
	public void append(final LoggingEvent event)
	{
		if (watchedLevels.contains(event.getLevel()))
		{
			numEvents.put(event.getLevel(), numEvents.getOrDefault(event.getLevel(), 0) + 1);
		}
	}
	
	
	/**
	 * @param level the level to get the number of events for
	 * @return the number of log events for the given log level that happened
	 */
	public int getNumEvents(final Level level)
	{
		return numEvents.getOrDefault(level, 0);
	}
	
	
	/**
	 * Clear all events
	 */
	public void clear()
	{
		numEvents.clear();
	}
}
