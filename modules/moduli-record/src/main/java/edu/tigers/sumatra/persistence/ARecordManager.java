/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;


/**
 * Manager for central control of recordings
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ARecordManager extends AModule
{
	private static final Logger log = Logger.getLogger(ARecordManager.class.getName());
	
	/** */
	public static final String MODULE_TYPE = "RecordManager";
	/** */
	public static final String MODULE_ID = "recorder";
	
	
	private final List<IRecordObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public ARecordManager(final SubnodeConfiguration subnodeConfiguration)
	{
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IRecordObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IRecordObserver observer)
	{
		observers.remove(observer);
	}
	
	
	protected void notifyStartStopRecord(final boolean recording)
	{
		for (IRecordObserver observer : observers)
		{
			observer.onStartStopRecord(recording);
		}
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		// nothing to do
	}
	
	
	@Override
	public void deinitModule()
	{
		// nothing to do
	}
	
	
	/**
	 * Toggle recording state
	 */
	public abstract void toggleRecording();
	
	
	/**
	 * Create a new persistence instance
	 *
	 * @param dbPath
	 * @return
	 */
	public abstract ABerkeleyPersistence createPersistenceForReadInheritable(String dbPath);
	
	
	/**
	 * @return the current DB path
	 */
	public abstract String getCurrentDbPath();
	
	
	/**
	 * Notify UI to view a replay window
	 * 
	 * @param startTime
	 */
	public void notifyViewReplay(long startTime)
	{
		String dbPath = getCurrentDbPath();
		if (dbPath.isEmpty())
		{
			log.error("No open DB found.");
			return;
		}
		ABerkeleyPersistence persistence;
		try
		{
			persistence = createPersistenceForReadInheritable(dbPath);
			persistence.open();
			log.debug("First key: " + persistence.getFirstKey());
		} catch (Exception e)
		{
			log.error("Could not open DB", e);
			return;
		}
		for (IRecordObserver observer : observers)
		{
			observer.onViewReplay(persistence, startTime);
		}
	}
}
