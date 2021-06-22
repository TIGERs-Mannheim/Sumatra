/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A buffer for log events.
 */
public class LogEventBuffer
{
	private static final int NUM_EVENTS = 6;
	private static final int PER_LEVEL_CAPACITY = 300;

	private final List<LogEvent> eventStorage = new ArrayList<>(NUM_EVENTS * PER_LEVEL_CAPACITY);
	private final Map<Level, List<LogEvent>> eventsPerLevel = new HashMap<>();

	private long start = 0;
	private long end = 0;
	private long offset = 0;


	public synchronized LogEventBuffer copy()
	{
		LogEventBuffer copy = new LogEventBuffer();
		copy.eventStorage.addAll(eventStorage);
		eventsPerLevel.forEach((level, events) -> copy.eventsPerLevel.put(level, new ArrayList<>(events)));
		return copy;
	}


	public synchronized void append(final LogEvent ev)
	{
		List<LogEvent> levelEventStorage = eventsPerLevel.computeIfAbsent(ev.getLevel(),
				k -> new ArrayList<>(PER_LEVEL_CAPACITY));
		if (levelEventStorage.size() >= PER_LEVEL_CAPACITY)
		{
			LogEvent oldEv = levelEventStorage.remove(0);
			boolean removed = eventStorage.remove(oldEv);
			assert removed;
			offset++;
		}
		levelEventStorage.add(ev);
		eventStorage.add(ev);
		end++;
	}


	public synchronized void clear()
	{
		eventStorage.clear();
		eventsPerLevel.values().forEach(List::clear);
		start = 0;
		end = 0;
		offset = 0;
	}


	public synchronized List<LogEvent> getNewEvents()
	{
		List<LogEvent> subList = new ArrayList<>(
				eventStorage.subList((int) Math.max(0, start - offset), (int) (end - offset)));
		start = end;
		return subList;
	}


	public synchronized void reset()
	{
		start = 0;
		end = eventStorage.size();
		offset = 0;
	}
}
