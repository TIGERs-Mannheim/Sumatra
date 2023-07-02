/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.log;

import edu.tigers.sumatra.log.ILogEventConsumer;
import edu.tigers.sumatra.log.SumatraAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Listen for certain log events and collect them.
 */
public class LogEventWatcher implements ILogEventConsumer
{
	private static final String INTEGRATION_TEST_APPENDER_NAME = "integrationTest";
	private final Set<Level> watchedLevels = new HashSet<>();
	private Map<Level, List<LogEvent>> events = new HashMap<>();


	/**
	 * Create a log appender that is looking for certain log events
	 *
	 * @param levels to watch
	 */
	public LogEventWatcher(Level... levels)
	{
		watchedLevels.addAll(Arrays.asList(levels));
	}


	public void start()
	{
		LoggerContext lc = (LoggerContext) LogManager.getContext(false);
		final SumatraAppender appender = lc.getConfiguration().getAppender(INTEGRATION_TEST_APPENDER_NAME);
		appender.addConsumer(this);
	}


	public void stop()
	{
		LoggerContext lc = (LoggerContext) LogManager.getContext(false);
		final SumatraAppender appender = lc.getConfiguration().getAppender(INTEGRATION_TEST_APPENDER_NAME);
		appender.removeConsumer(this);
	}


	@Override
	public void onNewLogEvent(final LogEvent logEvent)
	{
		if (watchedLevels.contains(logEvent.getLevel()))
		{
			final List<LogEvent> loggingEvents = events.computeIfAbsent(logEvent.getLevel(), a -> new ArrayList<>());
			loggingEvents.add(logEvent.toImmutable());
		}
	}


	/**
	 * @param level the level to get the number of events for
	 * @return the log events for the given log level that happened
	 */
	public List<LogEvent> getEvents(final Level level)
	{
		return events.getOrDefault(level, Collections.emptyList());
	}


	/**
	 * Clear all events
	 */
	public void clear()
	{
		LoggerContext lc = (LoggerContext) LogManager.getContext(false);
		final SumatraAppender appender = lc.getConfiguration().getAppender(INTEGRATION_TEST_APPENDER_NAME);
		appender.clear();
		events.clear();
	}
}
