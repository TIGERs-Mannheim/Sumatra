/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;


/**
 * Manager for central control of recordings
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RecordManager extends AModule implements IRefereeObserver
{
	/** */
	public static final String				MODULE_TYPE	= "RecordManager";
	/** */
	public static final String				MODULE_ID	= "recorder";
	
	private Recorder							recorder		= null;
	private static final Logger			log			= Logger.getLogger(RecordManager.class.getName());
	
	private final List<IRecordObserver>	observers	= new CopyOnWriteArrayList<IRecordObserver>();
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public RecordManager(final SubnodeConfiguration subnodeConfiguration)
	{
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IRecordObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IRecordObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyStartStopRecord(final boolean recording)
	{
		synchronized (observers)
		{
			for (IRecordObserver observer : observers)
			{
				observer.onStartStopRecord(recording);
			}
		}
	}
	
	
	/**
	 * @param saving
	 */
	public synchronized void toggleRecording(final boolean saving)
	{
		if (recorder == null)
		{
			startStopRecording(true);
		} else
		{
			startStopRecording(false);
		}
	}
	
	
	/**
	 * @param record start or stop record?
	 */
	public synchronized void startStopRecording(final boolean record)
	{
		if (record)
		{
			if (recorder != null)
			{
				log.warn("Start recording requested, but there is still an active recorder. Stopping it.");
				recorder.stop();
			}
			recorder = new Recorder();
			recorder.start();
		} else
		{
			if (recorder == null)
			{
				log.warn("Record stop requested, but there is no recorder");
			} else
			{
				recorder.stop();
				recorder = null;
			}
		}
		notifyStartStopRecord(record);
	}
	
	
	/**
	 * @return
	 */
	public synchronized boolean isRecording()
	{
		return (recorder != null);
	}
	
	
	@Override
	public void onNewRefereeMsg(final SSL_Referee refMsg)
	{
		if (SumatraModel.getInstance().isProductive() && (refMsg != null))
		{
			switch (refMsg.getStage())
			{
				case NORMAL_FIRST_HALF_PRE:
				case NORMAL_SECOND_HALF_PRE:
				case EXTRA_FIRST_HALF_PRE:
				case EXTRA_SECOND_HALF_PRE:
					if ((refMsg.getCommand() == Command.HALT))
					{
						break;
					}
				case NORMAL_FIRST_HALF:
				case NORMAL_SECOND_HALF:
				case EXTRA_FIRST_HALF:
				case EXTRA_SECOND_HALF:
				case PENALTY_SHOOTOUT:
					if (!isRecording())
					{
						startStopRecording(true);
					}
					break;
				case EXTRA_HALF_TIME:
				case NORMAL_HALF_TIME:
				case PENALTY_SHOOTOUT_BREAK:
				case POST_GAME:
				case EXTRA_TIME_BREAK:
					if (isRecording())
					{
						startStopRecording(false);
					}
					break;
			}
		}
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void deinitModule()
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		try
		{
			AReferee referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			referee.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find WP module", e);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		try
		{
			AReferee referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			referee.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find WP module", e);
		}
	}
}
