/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;


/**
 * This appender can persist log events
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BerkeleyLogAppender extends WriterAppender
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private List<BerkeleyLogEvent>	events	= new LinkedList<BerkeleyLogEvent>();
	private Object							sync		= new Object();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public BerkeleyLogAppender()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void append(LoggingEvent event)
	{
		synchronized (sync)
		{
			events.add(new BerkeleyLogEvent(event));
		}
	}
	
	
	/**
	 * Securely flush all events.
	 * 
	 * @return
	 */
	public List<BerkeleyLogEvent> flush()
	{
		List<BerkeleyLogEvent> list;
		synchronized (sync)
		{
			list = events;
			events = new LinkedList<BerkeleyLogEvent>();
		}
		return list;
	}
	
	
	/**
	 * @return the events
	 */
	public final List<BerkeleyLogEvent> getEvents()
	{
		synchronized (sync)
		{
			return new ArrayList<BerkeleyLogEvent>(events);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
