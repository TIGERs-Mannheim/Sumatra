/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence.log;

import edu.tigers.sumatra.persistence.PersistenceTable;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.JdkMapAdapterStringMap;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;

import java.util.HashMap;
import java.util.Map;


/**
 * Data object for persisting log events from log4j
 */
public class PersistenceLogEvent implements PersistenceTable.IEntry<PersistenceLogEvent>
{
	private static long lastId = 0L;

	@Getter
	private final long key;

	@Getter
	private final long timestamp;
	private final String level;
	private final String thread;
	private final String clazz;
	private final String message;
	private final Map<String, String> contextData;


	@SuppressWarnings("unused") // used by Fury
	private PersistenceLogEvent()
	{
		key = 0L;
		timestamp = 0L;
		level = "TRACE";
		thread = "";
		clazz = "";
		message = "";
		contextData = new HashMap<>();
	}


	public PersistenceLogEvent(final LogEvent event)
	{
		key = lastId++;
		timestamp = event.getTimeMillis();
		level = event.getLevel().toString();
		thread = event.getThreadName();
		clazz = event.getLoggerName();
		message = event.getMessage().getFormattedMessage();
		contextData = event.getContextData().toMap();
	}


	public final LogEvent getLogEvent()
	{
		return Log4jLogEvent.newBuilder()
				.setLoggerName(clazz == null ? "Unknown" : clazz)
				.setLevel(Level.toLevel(level))
				.setMessage(new SimpleMessage(message))
				.setThreadName(thread)
				.setTimeMillis(timestamp)
				.setContextData(new JdkMapAdapterStringMap(contextData, true))
				.build();
	}
}
