/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence.log;

import edu.tigers.sumatra.log.ILogEventConsumer;
import edu.tigers.sumatra.log.SumatraAppender;
import edu.tigers.sumatra.persistence.ABufferedPersistenceRecorder;
import edu.tigers.sumatra.persistence.PersistenceDb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;


/**
 * Storage for log events
 */
public class PersistenceLogRecorder extends ABufferedPersistenceRecorder<PersistenceLogEvent>
		implements ILogEventConsumer
{
	private static final String APPENDER_NAME = "persistence";


	/**
	 * Create a persistence log recorder
	 */
	public PersistenceLogRecorder(PersistenceDb db)
	{
		super(db, PersistenceLogEvent.class);
	}


	@Override
	public void onNewLogEvent(final LogEvent logEvent)
	{
		queue(new PersistenceLogEvent(logEvent));
	}


	@Override
	public void start()
	{
		LoggerContext lc = (LoggerContext) LogManager.getContext(false);
		final SumatraAppender sumatraAppender = lc.getConfiguration().getAppender(APPENDER_NAME);
		sumatraAppender.addConsumer(this);
	}


	@Override
	public void stop()
	{
		LoggerContext lc = (LoggerContext) LogManager.getContext(false);
		final SumatraAppender sumatraAppender = lc.getConfiguration().getAppender(APPENDER_NAME);
		sumatraAppender.removeConsumer(this);
	}
}
