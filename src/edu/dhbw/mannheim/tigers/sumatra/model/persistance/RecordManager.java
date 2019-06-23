/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.persistance.Recorder.ERecordMode;
import edu.dhbw.mannheim.tigers.sumatra.presenter.main.ReplayPresenter;


/**
 * Manager for central control of recordings
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RecordManager
{
	private static volatile Recorder					recorder				= null;
	private static final Logger						log					= Logger.getLogger(RecordManager.class.getName());
	
	/** a value of 0 means, record until memory almost full */
	private static final int							MAX_FRAMES			= 0;
	private static final int							FRAME_BUFFER_SIZE	= 1000;
	
	private static final List<IRecordObserver>	observers			= new CopyOnWriteArrayList<IRecordObserver>();
	
	
	private RecordManager()
	{
	}
	
	
	/**
	 * @param observer
	 */
	public static void addObserver(final IRecordObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public static void removeObserver(final IRecordObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private static void notifyStartStopRecord(final boolean recording, final boolean persisting)
	{
		synchronized (observers)
		{
			for (IRecordObserver observer : observers)
			{
				observer.onStartStopRecord(recording, persisting);
			}
		}
	}
	
	
	/**
	 * @param saving
	 */
	public static synchronized void toggleRecording(final boolean saving)
	{
		if (recorder == null)
		{
			startStopRecording(true, saving);
		} else
		{
			startStopRecording(false, saving);
		}
	}
	
	
	/**
	 * @param record start or stop record?
	 * @param saving persist data?
	 */
	public static synchronized void startStopRecording(final boolean record, final boolean saving)
	{
		if (record)
		{
			if (recorder != null)
			{
				log.warn("Start recording requested, but there is still an active recorder. Stopping it.");
				recorder.stop();
			}
			if (saving)
			{
				recorder = new Recorder(ERecordMode.DATABASE, FRAME_BUFFER_SIZE);
			} else
			{
				recorder = new Recorder(ERecordMode.LIMITED_BUFFER, MAX_FRAMES);
			}
			recorder.start();
		} else
		{
			if (recorder == null)
			{
				log.warn("Record stop requested, but there is no recorder");
			} else
			{
				recorder.stop();
				if (!saving)
				{
					ReplayPresenter presenter = new ReplayPresenter();
					presenter.load(recorder.getRecordFrames(), recorder.getLogEvents());
					presenter.start();
				}
				recorder = null;
			}
		}
		notifyStartStopRecord(record, saving);
	}
	
	
	/**
	 * @return
	 */
	public static synchronized boolean isRecording()
	{
		return (recorder != null) && (recorder.getRecordMode() == ERecordMode.DATABASE);
	}
}
