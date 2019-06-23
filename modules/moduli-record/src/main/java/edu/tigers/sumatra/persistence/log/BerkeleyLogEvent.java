/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


/**
 * Data object for persisting log events from log4j
 */
@Entity
public class BerkeleyLogEvent
{
	private final Long timestamp;
	private final String level;
	private final String thread;
	private final String clazz;
	private final String message;
	@SuppressWarnings("unused")
	@PrimaryKey(sequence = "ID")
	private long id;
	
	
	/**
	 * Used for BerkeleyDB
	 */
	@SuppressWarnings("unused")
	private BerkeleyLogEvent()
	{
		timestamp = 0L;
		level = "TRACE";
		thread = "";
		clazz = "";
		message = "";
	}
	
	
	/**
	 * @param event
	 */
	public BerkeleyLogEvent(final LoggingEvent event)
	{
		timestamp = event.getTimeStamp();
		level = event.getLevel().toString();
		thread = event.getThreadName();
		clazz = event.getClass().getName();
		message = event.getMessage().toString();
	}
	
	
	/**
	 * @return
	 */
	public final LoggingEvent getLoggingEvent()
	{
		return new LoggingEvent(clazz, Logger.getRootLogger(), timestamp, Level.toLevel(level), message, thread,
				null, null, null, null);
	}
	
	
	public long getTimestamp()
	{
		return timestamp;
	}
}
