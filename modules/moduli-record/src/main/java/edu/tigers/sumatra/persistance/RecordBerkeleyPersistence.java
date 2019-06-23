/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 20, 2013
 * Author(s): geforce
 * *********************************************************
 */
package edu.tigers.sumatra.persistance;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.sleepycat.persist.EntityStore;

import edu.tigers.sumatra.model.SumatraModel;


/**
 * Persistance class for dealing with a berkeley database
 * 
 * @author geforce
 */
public class RecordBerkeleyPersistence extends ABerkeleyPersistence implements IRecordPersistence
{
	@SuppressWarnings("unused")
	private static final Logger											log	= Logger
			.getLogger(RecordBerkeleyPersistence.class.getName());
	
	private final BerkeleyAccessorById<BerkeleyLogEvent>			logEventAccessor;
	private final BerkeleyAccessorByTimestamp<RecordFrame>		recordFrameAccessor;
	private final BerkeleyAccessorByTimestamp<RecordCamFrame>	camFrameAccessor;
	
	
	/**
	 * @param dbPath
	 * @param readOnly
	 */
	public RecordBerkeleyPersistence(final String dbPath, final boolean readOnly)
	{
		super(dbPath, readOnly);
		EntityStore store = getEnv().getEntityStore();
		logEventAccessor = new BerkeleyAccessorById<>(store, BerkeleyLogEvent.class);
		recordFrameAccessor = new BerkeleyAccessorByTimestamp<>(store, RecordFrame.class);
		camFrameAccessor = new BerkeleyAccessorByTimestamp<>(store, RecordCamFrame.class);
	}
	
	
	/**
	 * @return
	 */
	public static String getDefaultBasePath()
	{
		return SumatraModel.getInstance()
				.getUserProperty(RecordBerkeleyPersistence.class.getCanonicalName() + ".basePath", "data/record");
	}
	
	
	/**
	 * @param path
	 */
	public static void setDefaultBasePath(final String path)
	{
		SumatraModel.getInstance().setUserProperty(RecordBerkeleyPersistence.class.getCanonicalName() + ".basePath",
				path);
	}
	
	
	@Override
	public synchronized void saveLogEvent(final List<BerkeleyLogEvent> logEvents)
	{
		logEventAccessor.save(logEvents);
	}
	
	
	/**
	 * @return
	 */
	@Override
	public synchronized List<BerkeleyLogEvent> loadLogEvents()
	{
		return logEventAccessor.load();
	}
	
	
	@Override
	public RecordFrame getRecordFrame(final long tCur)
	{
		return recordFrameAccessor.get(tCur);
	}
	
	
	@Override
	public void saveRecordFrames(final Collection<RecordFrame> recordFrames)
	{
		recordFrameAccessor.saveFrames(recordFrames);
	}
	
	
	@Override
	public RecordCamFrame getCamFrame(final long tCur)
	{
		return camFrameAccessor.get(tCur);
	}
	
	
	@Override
	public void saveCamFrames(final Collection<RecordCamFrame> camFrames)
	{
		camFrameAccessor.saveFrames(camFrames);
	}
	
	
	@Override
	public Long getFirstKey()
	{
		Long k1 = recordFrameAccessor.getFirstKey();
		Long k2 = camFrameAccessor.getFirstKey();
		if (k1 == null)
		{
			return k2;
		}
		if (k2 == null)
		{
			return k1;
		}
		if (k1 < k2)
		{
			return k1;
		}
		return k2;
	}
	
	
	@Override
	public Long getLastKey()
	{
		Long k1 = recordFrameAccessor.getLastKey();
		Long k2 = camFrameAccessor.getLastKey();
		if (k1 == null)
		{
			return k2;
		}
		if (k2 == null)
		{
			return k1;
		}
		if (k1 > k2)
		{
			return k1;
		}
		return k2;
	}
	
	
	@Override
	public Long getKey(final long tCur)
	{
		Long k1 = recordFrameAccessor.getKey(tCur);
		Long k2 = camFrameAccessor.getKey(tCur);
		if (k1 == null)
		{
			return k2;
		}
		if (k2 == null)
		{
			return k1;
		}
		long diff1 = Math.abs(tCur - k1);
		long diff2 = Math.abs(tCur - k2);
		
		if (diff1 < diff2)
		{
			return k1;
		}
		return k2;
	}
	
	
	@Override
	public Long getNextKey(final long key)
	{
		Long k1 = recordFrameAccessor.getNextKey(key);
		Long k2 = camFrameAccessor.getNextKey(key);
		if (k1 == null)
		{
			return k2;
		}
		if (k2 == null)
		{
			return k1;
		}
		long diff1 = Math.abs(key - k1);
		long diff2 = Math.abs(key - k2);
		
		if (diff1 < diff2)
		{
			return k1;
		}
		return k2;
	}
	
	
	@Override
	public Long getPreviousKey(final long key)
	{
		Long k1 = recordFrameAccessor.getPreviousKey(key);
		Long k2 = camFrameAccessor.getPreviousKey(key);
		if (k1 == null)
		{
			return k2;
		}
		if (k2 == null)
		{
			return k1;
		}
		long diff1 = Math.abs(key - k1);
		long diff2 = Math.abs(key - k2);
		
		if (diff1 < diff2)
		{
			return k1;
		}
		return k2;
	}
}
