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
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <FRAME_TYPE>
 */
public class BerkeleyAccessorByTimestamp<FRAME_TYPE>
{
	@SuppressWarnings("unused")
	private static final Logger						log						= Logger
			.getLogger(BerkeleyAccessorByTimestamp.class.getName());
	
	private final PrimaryIndex<Long, FRAME_TYPE>	frameByTimestamp;
	
	private static final long							EXPECTED_FRAME_RATE	= 16;
	
	
	/**
	 * @param store
	 * @param clazz
	 */
	public BerkeleyAccessorByTimestamp(final EntityStore store, final Class<FRAME_TYPE> clazz)
	{
		frameByTimestamp = store.getPrimaryIndex(Long.class, clazz);
	}
	
	
	/**
	 * @param frames
	 */
	public void saveFrames(final Collection<FRAME_TYPE> frames)
	{
		for (FRAME_TYPE frame : frames)
		{
			if (frame == null)
			{
				log.error("null frame! sth is wrong...");
			} else
			{
				try
				{
					frameByTimestamp.put(frame);
				} catch (Exception err)
				{
					log.error("Could not save frame.", err);
				}
			}
		}
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public synchronized FRAME_TYPE get(final long tCur)
	{
		Long key = getKey(tCur);
		if (key == null)
		{
			return null;
		}
		return frameByTimestamp.get(key);
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public synchronized Long getKey(final long tCur)
	{
		long t = tCur - (EXPECTED_FRAME_RATE / 2);
		EntityCursor<Long> keys = frameByTimestamp.keys(null, t, true, null, true,
				CursorConfig.READ_UNCOMMITTED);
		try
		{
			Long key = keys.first();
			if (key == null)
			{
				EntityCursor<Long> cursor = frameByTimestamp.keys(null, CursorConfig.READ_UNCOMMITTED);
				key = cursor.first();
				cursor.close();
			}
			return key;
		} finally
		{
			keys.close();
		}
	}
	
	
	/**
	 * @param key
	 * @return the next key or null, of there is no next key
	 */
	public synchronized Long getNextKey(final long key)
	{
		EntityCursor<Long> keys = frameByTimestamp.keys(null, key, false, null, true,
				CursorConfig.READ_UNCOMMITTED);
		try
		{
			Long nextKey = keys.first();
			return nextKey;
		} finally
		{
			keys.close();
		}
	}
	
	
	/**
	 * @param key
	 * @return the previous key or null, of there is no previous key
	 */
	public synchronized Long getPreviousKey(final long key)
	{
		EntityCursor<Long> keys = frameByTimestamp.keys(null, null, true, key, false,
				CursorConfig.READ_UNCOMMITTED);
		try
		{
			Long nextKey = keys.last();
			return nextKey;
		} finally
		{
			keys.close();
		}
	}
	
	
	/**
	 * @return
	 */
	public long size()
	{
		return frameByTimestamp.count();
	}
	
	
	/**
	 * @return
	 */
	public Long getFirstKey()
	{
		EntityCursor<Long> cursor = frameByTimestamp.keys(null,
				CursorConfig.READ_UNCOMMITTED);
		Long key = cursor.first();
		cursor.close();
		return key;
	}
	
	
	/**
	 * @return
	 */
	public Long getLastKey()
	{
		EntityCursor<Long> cursor = frameByTimestamp.keys(null,
				CursorConfig.READ_UNCOMMITTED);
		Long key = cursor.last();
		cursor.close();
		return key;
	}
	
	
	/**
	 * @return
	 */
	public List<Long> getKeys()
	{
		EntityCursor<Long> cursor = frameByTimestamp.keys(null,
				CursorConfig.READ_UNCOMMITTED);
		try
		{
			List<Long> keys = new ArrayList<>(cursor.count());
			return keys;
		} finally
		{
			cursor.close();
		}
	}
}
