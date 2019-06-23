/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;


/**
 * An accessor for timestamp-indexed data
 * 
 * @param <T>
 */
public class BerkeleyAccessor<T> implements IBerkeleyAccessor<T>
{
	private static final Logger log = Logger.getLogger(BerkeleyAccessor.class.getName());
	private static final long EXPECTED_FRAME_RATE = 16;
	private final Class<T> clazz;
	private final boolean sumatraTimestampBased;
	private PrimaryIndex<Long, T> frameByTimestamp;
	
	
	/**
	 * @param clazz
	 */
	public BerkeleyAccessor(final Class<T> clazz, boolean sumatraTimestampBased)
	{
		this.clazz = clazz;
		this.sumatraTimestampBased = sumatraTimestampBased;
	}
	
	
	@Override
	public void open(final EntityStore entityStore)
	{
		frameByTimestamp = entityStore.getPrimaryIndex(Long.class, clazz);
	}
	
	
	@Override
	public void write(final Collection<T> elements)
	{
		elements.forEach(this::write);
	}
	
	
	@Override
	public void write(T element)
	{
		if (element == null)
		{
			log.error("null element! sth is wrong...");
			return;
		}
		try
		{
			frameByTimestamp.put(element);
		} catch (Exception err)
		{
			log.error("Could not write element: " + element, err);
		}
	}
	
	
	@Override
	public synchronized T get(final long tCur)
	{
		Long key = getNearestKey(tCur);
		if (key == null)
		{
			return null;
		}
		return frameByTimestamp.get(key);
	}
	
	
	@Override
	public synchronized Long getNearestKey(final long key)
	{
		long t = key - (EXPECTED_FRAME_RATE / 2);
		try (EntityCursor<Long> keys = frameByTimestamp.keys(null, t, true, null, true,
				CursorConfig.READ_UNCOMMITTED))
		{
			Long first = keys.first();
			if (first == null)
			{
				EntityCursor<Long> cursor = frameByTimestamp.keys(null, CursorConfig.READ_UNCOMMITTED);
				first = cursor.first();
				cursor.close();
			}
			return first;
		}
	}
	
	
	@Override
	public synchronized Long getNextKey(final long key)
	{
		try (EntityCursor<Long> keys = frameByTimestamp.keys(null, key, false, null, true,
				CursorConfig.READ_UNCOMMITTED))
		{
			return keys.first();
		}
	}
	
	
	@Override
	public synchronized Long getPreviousKey(final long key)
	{
		try (EntityCursor<Long> keys = frameByTimestamp.keys(null, null, true, key, false,
				CursorConfig.READ_UNCOMMITTED))
		{
			return keys.last();
		}
	}
	
	
	@Override
	public long size()
	{
		return frameByTimestamp.count();
	}
	
	
	@Override
	public Long getFirstKey()
	{
		EntityCursor<Long> cursor = frameByTimestamp.keys(null,
				CursorConfig.READ_UNCOMMITTED);
		Long key = cursor.first();
		cursor.close();
		return key;
	}
	
	
	@Override
	public Long getLastKey()
	{
		EntityCursor<Long> cursor = frameByTimestamp.keys(null,
				CursorConfig.READ_UNCOMMITTED);
		Long key = cursor.last();
		cursor.close();
		return key;
	}
	
	
	@Override
	public synchronized List<T> load()
	{
		List<T> events = new ArrayList<>((int) frameByTimestamp.count());
		try (EntityCursor<T> cursor = frameByTimestamp.entities())
		{
			for (T event : cursor)
			{
				events.add(event);
			}
		}
		return events;
	}
	
	
	@Override
	public boolean isSumatraTimestampBased()
	{
		return sumatraTimestampBased;
	}
}
