/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 20, 2013
 * Author(s): geforce
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.IndexNotAvailableException;
import com.sleepycat.persist.PrimaryIndex;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.RecordFrame;


/**
 * Persistance class for dealing with a berkeley database
 * 
 * @author geforce
 */
public class RecordBerkeleyPersistence extends ABerkeleyPersistence implements IRecordPersistence
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log			= Logger.getLogger(RecordBerkeleyPersistence.class.getName());
	private final DataAccessor		dataAccessor;
	/**  */
	public static final String		BASE_PATH	= "data/record/";
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param dbName
	 */
	public RecordBerkeleyPersistence(final String dbName)
	{
		this(BASE_PATH, dbName);
	}
	
	
	/**
	 * @param dbName
	 * @param readOnly
	 */
	public RecordBerkeleyPersistence(final String dbName, final boolean readOnly)
	{
		this(BASE_PATH, dbName, readOnly);
	}
	
	
	/**
	 * @param base
	 * @param dbName
	 */
	public RecordBerkeleyPersistence(final String base, final String dbName)
	{
		this(base, dbName, false);
	}
	
	
	/**
	 * @param base
	 * @param dbName
	 * @param readOnly
	 */
	public RecordBerkeleyPersistence(final String base, final String dbName, final boolean readOnly)
	{
		super(base + "/" + dbName, readOnly);
		dataAccessor = new DataAccessor(getEnv().getEntityStore());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param frames
	 */
	@Override
	public synchronized void saveFrames(final List<IRecordFrame> frames)
	{
		long start = System.nanoTime();
		for (IRecordFrame frame : frames)
		{
			dataAccessor.recordFrameById.put((RecordFrame) frame);
		}
		long time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		log.info(frames.size() + " record frames saved in " + time + "ms");
	}
	
	
	@Override
	public synchronized void saveLogEvent(final List<BerkeleyLogEvent> logEvents)
	{
		long start = System.nanoTime();
		if (dataAccessor.logEventByTimeStamp == null)
		{
			log.error("You are trying to save an logEvent to an old version of a database. How did you do this?");
			return;
		}
		for (BerkeleyLogEvent event : logEvents)
		{
			dataAccessor.logEventByTimeStamp.put(event);
		}
		long time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		log.info(logEvents.size() + " log events saved in " + time + "ms");
	}
	
	
	/**
	 * @return
	 */
	@Override
	public synchronized List<IRecordFrame> load()
	{
		List<IRecordFrame> frames = new ArrayList<IRecordFrame>((int) dataAccessor.recordFrameById.count());
		EntityCursor<RecordFrame> cursor = dataAccessor.recordFrameById.entities();
		try
		{
			for (RecordFrame rf : cursor)
			{
				frames.add(rf);
			}
		} finally
		{
			cursor.close();
		}
		return frames;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public synchronized List<BerkeleyLogEvent> loadLogEvents()
	{
		if (dataAccessor.logEventByTimeStamp == null)
		{
			// for compatibility
			log.info("Could not load log events");
			return new ArrayList<BerkeleyLogEvent>(0);
		}
		List<BerkeleyLogEvent> events = new ArrayList<BerkeleyLogEvent>((int) dataAccessor.logEventByTimeStamp.count());
		EntityCursor<BerkeleyLogEvent> cursor = dataAccessor.logEventByTimeStamp.entities();
		try
		{
			for (BerkeleyLogEvent event : cursor)
			{
				events.add(event);
			}
		} finally
		{
			cursor.close();
		}
		log.info("Loaded " + events.size() + " log events.");
		return events;
	}
	
	
	@Override
	public synchronized List<IRecordFrame> load(final int startIndex, final int length)
	{
		List<IRecordFrame> frames = new ArrayList<IRecordFrame>(length);
		final Integer indexOffset = getFirstKey();
		if (indexOffset == null)
		{
			log.error("No key found.");
			return frames;
		}
		int max = size();
		for (int i = startIndex; (i < (length + startIndex)) && (i < max); i++)
		{
			IRecordFrame frame = dataAccessor.recordFrameById.get(i + indexOffset);
			if (frame == null)
			{
				log.warn("Skipping frame: " + i);
				continue;
			}
			frames.add(frame);
		}
		log.info("Loaded " + frames.size() + " frames from db");
		return frames;
	}
	
	
	@Override
	public int size()
	{
		return (int) dataAccessor.recordFrameById.count();
	}
	
	
	private Integer getFirstKey()
	{
		EntityCursor<Integer> cursor = dataAccessor.recordFrameById.keys();
		Integer key = cursor.first();
		cursor.close();
		return key;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private static class DataAccessor
	{
		PrimaryIndex<Integer, RecordFrame>			recordFrameById		= null;
		PrimaryIndex<Integer, BerkeleyLogEvent>	logEventByTimeStamp	= null;
		
		
		/**
		 * @param store
		 */
		public DataAccessor(final EntityStore store)
		{
			recordFrameById = store.getPrimaryIndex(Integer.class, RecordFrame.class);
			try
			{
				logEventByTimeStamp = store.getPrimaryIndex(Integer.class, BerkeleyLogEvent.class);
			} catch (IndexNotAvailableException e)
			{
				log.warn("Index for logEvents not available. You are using an old database.");
			}
		}
	}
}
