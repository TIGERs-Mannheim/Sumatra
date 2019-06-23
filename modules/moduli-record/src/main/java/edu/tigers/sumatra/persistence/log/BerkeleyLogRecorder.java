/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IBerkeleyRecorder;


/**
 * Storage for log events
 */
public class BerkeleyLogRecorder implements IBerkeleyRecorder
{
	private final Queue<LoggingEvent> buffer = new ConcurrentLinkedQueue<>();
	private final BerkeleyLogAppender logAppender;
	private final BerkeleyDb db;
	
	
	/**
	 * Create a berkeley log recorder
	 */
	public BerkeleyLogRecorder(BerkeleyDb db)
	{
		this.db = db;
		logAppender = new BerkeleyLogAppender();
		logAppender.setThreshold(Level.ALL);
	}
	
	
	@Override
	public void start()
	{
		Logger.getRootLogger().addAppender(logAppender);
	}
	
	
	@Override
	public void stop()
	{
		Logger.getRootLogger().removeAppender(logAppender);
	}
	
	
	@Override
	public void flush()
	{
		List<BerkeleyLogEvent> eventsToSave = new ArrayList<>();
		LoggingEvent event = buffer.poll();
		while (event != null)
		{
			eventsToSave.add(new BerkeleyLogEvent(event));
			event = buffer.poll();
		}
		
		db.write(BerkeleyLogEvent.class, eventsToSave);
	}
	
	
	private class BerkeleyLogAppender extends WriterAppender
	{
		@Override
		public void append(final LoggingEvent event)
		{
			buffer.add(event);
		}
	}
}
