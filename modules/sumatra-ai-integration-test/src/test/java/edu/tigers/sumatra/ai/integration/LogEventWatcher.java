/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	private Map<Level, List<LoggingEvent>> events = new HashMap<>();
	
	
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
			final List<LoggingEvent> loggingEvents = events.computeIfAbsent(event.getLevel(), a -> new ArrayList<>());
			loggingEvents.add(event);
		}
	}
	
	
	/**
	 * @param level the level to get the number of events for
	 * @return the log events for the given log level that happened
	 */
	public List<LoggingEvent> getEvents(final Level level)
	{
		return events.getOrDefault(level, Collections.emptyList());
	}
	
	
	/**
	 * Clear all events
	 */
	public void clear()
	{
		events.clear();
	}
}
