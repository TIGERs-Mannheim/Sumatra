/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;

import edu.tigers.sumatra.log.ILogEventConsumer;
import edu.tigers.sumatra.log.SumatraAppender;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IBerkeleyRecorder;


/**
 * Storage for log events
 */
public class BerkeleyLogRecorder implements IBerkeleyRecorder, ILogEventConsumer
{
	private static final String BERKELEY_APPENDER_NAME = "berkeley";
	private final Queue<BerkeleyLogEvent> buffer = new ConcurrentLinkedQueue<>();
	private final BerkeleyDb db;


	/**
	 * Create a berkeley log recorder
	 */
	public BerkeleyLogRecorder(BerkeleyDb db)
	{
		this.db = db;
	}


	@Override
	public void onNewLogEvent(final LogEvent logEvent)
	{
		buffer.add(new BerkeleyLogEvent(logEvent));
	}


	@Override
	public void start()
	{
		LoggerContext lc = (LoggerContext) LogManager.getContext(false);
		final SumatraAppender sumatraAppender = lc.getConfiguration().getAppender(BERKELEY_APPENDER_NAME);
		sumatraAppender.addConsumer(this);
	}


	@Override
	public void stop()
	{
		LoggerContext lc = (LoggerContext) LogManager.getContext(false);
		final SumatraAppender sumatraAppender = lc.getConfiguration().getAppender(BERKELEY_APPENDER_NAME);
		sumatraAppender.removeConsumer(this);
	}


	@Override
	public void flush()
	{
		List<BerkeleyLogEvent> eventsToSave = new ArrayList<>();
		BerkeleyLogEvent event = buffer.poll();
		while (event != null)
		{
			eventsToSave.add(event);
			event = buffer.poll();
		}

		db.write(BerkeleyLogEvent.class, eventsToSave);
	}
}
