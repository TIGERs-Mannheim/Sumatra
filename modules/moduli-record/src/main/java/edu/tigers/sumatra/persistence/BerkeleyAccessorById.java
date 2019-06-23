/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 9, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistence;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <T>
 */
public class BerkeleyAccessorById<T>
{
	@SuppressWarnings("unused")
	private static final Logger					log	= Logger.getLogger(BerkeleyAccessorById.class.getName());
	
	private final PrimaryIndex<Integer, T>	elementById;
	
	
	/**
	 * @param store
	 * @param clazz
	 */
	public BerkeleyAccessorById(final EntityStore store, final Class<T> clazz)
	{
		elementById = store.getPrimaryIndex(Integer.class, clazz);
	}
	
	
	/**
	 * @return
	 */
	public long size()
	{
		return elementById.count();
	}
	
	
	/**
	 * @param logEvents
	 */
	public synchronized void save(final List<T> logEvents)
	{
		if (elementById == null)
		{
			log.error("You are trying to save an logEvent to an old version of a database. How did you do this?");
			return;
		}
		for (T event : logEvents)
		{
			elementById.put(event);
		}
	}
	
	
	/**
	 * @return
	 */
	public synchronized List<T> load()
	{
		if (elementById == null)
		{
			// for compatibility
			log.info("Could not load log events");
			return new ArrayList<>(0);
		}
		List<T> events = new ArrayList<>((int) elementById.count());
		EntityCursor<T> cursor = elementById.entities();
		try
		{
			for (T event : cursor)
			{
				events.add(event);
			}
		} finally
		{
			cursor.close();
		}
		return events;
	}
}
