/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence;

import java.util.List;

import org.apache.log4j.Logger;

import com.sleepycat.persist.EntityStore;


/**
 * Persistance class for dealing with a berkeley database
 * 
 * @author geforce
 */
public class LogBerkeleyPersistence extends ABerkeleyPersistence
{
	@SuppressWarnings("unused")
	private static final Logger							log	= Logger
			.getLogger(LogBerkeleyPersistence.class.getName());
	
	private BerkeleyAccessorById<BerkeleyLogEvent>	logEventAccessor;
	
	
	/**
	 * @param dbPath absolute path to database folder or zip file
	 */
	public LogBerkeleyPersistence(final String dbPath)
	{
		super(dbPath);
	}
	
	
	@Override
	public void open()
	{
		super.open();
		EntityStore store = getEnv().getEntityStore();
		logEventAccessor = new BerkeleyAccessorById<>(store, BerkeleyLogEvent.class);
	}
	
	
	/**
	 * @return total number of log events
	 */
	public long getNumberOfLogEvents()
	{
		return logEventAccessor.size();
	}
	
	
	/**
	 * Save all log events to database
	 *
	 * @param logEvents to be saved
	 */
	public synchronized void saveLogEvent(final List<BerkeleyLogEvent> logEvents)
	{
		logEventAccessor.save(logEvents);
	}
	
	
	/**
	 * @return all log events stored in the database
	 */
	public synchronized List<BerkeleyLogEvent> loadLogEvents()
	{
		return logEventAccessor.load();
	}
	
	
	@Override
	public Long getFirstKey()
	{
		return null;
	}
	
	
	@Override
	public Long getLastKey()
	{
		return null;
	}
	
	
	@Override
	public Long getKey(long tCur)
	{
		return null;
	}
	
	
	@Override
	public Long getNextKey(long key)
	{
		return null;
	}
	
	
	@Override
	public Long getPreviousKey(long key)
	{
		return null;
	}
}
