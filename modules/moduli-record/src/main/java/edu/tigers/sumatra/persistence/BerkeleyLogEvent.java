/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistence;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


/**
 * Data object for persisting log events from log4j
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Entity(version = 0)
public class BerkeleyLogEvent
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@PrimaryKey(sequence = "ID")
	private int				id;
	
	private final Long	timeStamp;
	private final String	level;
	private final String	thread;
	private final String	clazz;
	private final String	message;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Used for BerkeleyDB
	 */
	@SuppressWarnings("unused")
	private BerkeleyLogEvent()
	{
		timeStamp = Long.valueOf(0);
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
		timeStamp = event.getTimeStamp();
		level = event.getLevel().toString();
		thread = event.getThreadName();
		clazz = event.getClass().getName();
		message = event.getMessage().toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public final LoggingEvent getLoggingEvent()
	{
		LoggingEvent event = new LoggingEvent(clazz, Logger.getRootLogger(), timeStamp, getLevel(), message, thread,
				null, null, null, null);
		return event;
	}
	
	
	/**
	 * @return the timeStamp
	 */
	public final Long getTimeStamp()
	{
		return timeStamp;
	}
	
	
	/**
	 * @return the level
	 */
	public final Level getLevel()
	{
		return Level.toLevel(level);
	}
	
	
	/**
	 * @return the thread
	 */
	public final String getThread()
	{
		return thread;
	}
	
	
	/**
	 * @return the clazz
	 */
	public final String getClazz()
	{
		return clazz;
	}
	
	
	/**
	 * @return the message
	 */
	public final String getMessage()
	{
		return message;
	}
	
}
