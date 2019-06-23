/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.util.Collection;

import com.sleepycat.persist.EntityStore;


/**
 * Persistence layer for vision data
 * 
 * @author nicolai.ommer
 */
public class VisionBerkeleyPersistence extends LogBerkeleyPersistence
{
	private BerkeleyAccessorByTimestamp<RecordCamFrame> camFrameAccessor;
	
	
	/**
	 * @param dbPath absolute path to database folder or zip file
	 */
	public VisionBerkeleyPersistence(final String dbPath)
	{
		super(dbPath);
	}
	
	
	@Override
	public void open()
	{
		super.open();
		EntityStore store = getEnv().getEntityStore();
		camFrameAccessor = new BerkeleyAccessorByTimestamp<>(store, RecordCamFrame.class);
	}
	
	
	/**
	 * @return total number of cam frames
	 */
	public long getNumberOfCamFrames()
	{
		return camFrameAccessor.size();
	}
	
	
	/**
	 * @param tCur nearest timestamp
	 * @return frame at timestamp
	 */
	public RecordCamFrame getCamFrame(final long tCur)
	{
		return camFrameAccessor.get(tCur);
	}
	
	
	/**
	 * @param camFrames to save
	 */
	public void saveCamFrames(final Collection<RecordCamFrame> camFrames)
	{
		camFrameAccessor.saveFrames(camFrames);
	}
	
	
	@Override
	public Long getFirstKey()
	{
		Long k1 = super.getFirstKey();
		Long k2 = camFrameAccessor.getFirstKey();
		return getSmallerKey(k1, k2);
	}
	
	
	@Override
	public Long getLastKey()
	{
		Long k1 = super.getLastKey();
		Long k2 = camFrameAccessor.getLastKey();
		return getLargerKey(k1, k2);
	}
	
	
	@Override
	public Long getKey(final long tCur)
	{
		Long k1 = super.getKey(tCur);
		Long k2 = camFrameAccessor.getKey(tCur);
		return getNearestKey(tCur, k1, k2);
	}
	
	
	@Override
	public Long getNextKey(final long key)
	{
		Long k1 = super.getNextKey(key);
		Long k2 = camFrameAccessor.getNextKey(key);
		return getNearestKey(key, k1, k2);
	}
	
	
	@Override
	public Long getPreviousKey(final long key)
	{
		Long k1 = super.getPreviousKey(key);
		Long k2 = camFrameAccessor.getPreviousKey(key);
		return getNearestKey(key, k1, k2);
	}
}
