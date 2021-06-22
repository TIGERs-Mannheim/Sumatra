/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence.log;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;


/**
 * Data object for persisting log events from log4j
 */
@Entity
public class BerkeleyLogEvent
{
	@SuppressWarnings("unused")
	@PrimaryKey(sequence = "ID")
	private long id;

	private final Long timestamp;
	private final String level;
	private final String thread;
	private final String clazz;
	private final String message;


	@SuppressWarnings("unused") // used by BerkeleyDB
	private BerkeleyLogEvent()
	{
		timestamp = 0L;
		level = "TRACE";
		thread = "";
		clazz = "";
		message = "";
	}


	public BerkeleyLogEvent(final LogEvent event)
	{
		timestamp = event.getTimeMillis();
		level = event.getLevel().toString();
		thread = event.getThreadName();
		clazz = event.getLoggerName();
		message = event.getMessage().getFormattedMessage();
	}


	public final LogEvent getLogEvent()
	{
		return Log4jLogEvent.newBuilder()
				.setLoggerName(clazz == null ? "Unknown" : clazz)
				.setLevel(Level.toLevel(level))
				.setMessage(new SimpleMessage(message))
				.setThreadName(thread)
				.setTimeMillis(timestamp)
				.build();
	}


	public long getTimestamp()
	{
		return timestamp;
	}
}
