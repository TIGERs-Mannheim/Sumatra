/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.gamelog.SSLGameLogRecorder;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;


/**
 * Manager for central control of recordings
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RecordManager extends ARecordManager implements IRefereeObserver
{
	private static final Logger log = Logger.getLogger(RecordManager.class.getName());
	
	@Configurable(defValue = "true", comment = "Automatically compress recordings after they were closed")
	private static boolean compressAutomatically = true;
	
	static
	{
		ConfigRegistration.registerClass("user", RecordManager.class);
	}
	
	
	private AiBerkeleyRecorder recorder = null;
	private SSLGameLogRecorder gamelogRecorder = new SSLGameLogRecorder();
	private long lastCommandCounter = -1;
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public RecordManager(final SubnodeConfiguration subnodeConfiguration)
	{
		super(subnodeConfiguration);
	}
	
	
	@Override
	public synchronized void toggleRecording()
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
	 * @return
	 */
	public static String getDefaultBasePath()
	{
		return SumatraModel.getInstance()
				.getUserProperty("edu.tigers.sumatra.persistence.basePath", "data/record");
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
				gamelogRecorder.stop();
			}
			recorder = new AiBerkeleyRecorder(getDefaultBasePath());
			recorder.setCompressAutomatically(compressAutomatically);
			recorder.start();
			gamelogRecorder.start();
		} else
		{
			if (recorder == null)
			{
				log.warn("Record stop requested, but there is no recorder");
			} else
			{
				recorder.stop();
				gamelogRecorder.stop();
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
		return recorder != null;
	}
	
	
	@Override
	public synchronized String getCurrentDbPath()
	{
		if (recorder == null)
		{
			return "";
		}
		return recorder.getDbPath();
	}
	
	
	/**
	 * Create a new persistence instance
	 * 
	 * @param dbPath
	 * @return
	 */
	public static ABerkeleyPersistence createPersistenceForRead(final String dbPath)
	{
		return new AiBerkeleyPersistence(dbPath);
	}
	
	
	@Override
	public ABerkeleyPersistence createPersistenceForReadInheritable(final String dbPath)
	{
		return createPersistenceForRead(dbPath);
	}
	
	
	private boolean isPreStage(final SSL_Referee refMsg)
	{
		switch (refMsg.getStage())
		{
			case NORMAL_FIRST_HALF_PRE:
			case NORMAL_SECOND_HALF_PRE:
			case EXTRA_FIRST_HALF_PRE:
			case EXTRA_SECOND_HALF_PRE:
				return true;
			default:
				return false;
		}
	}
	
	
	private boolean isGameStage(final SSL_Referee refMsg)
	{
		switch (refMsg.getStage())
		{
			case NORMAL_FIRST_HALF:
			case NORMAL_SECOND_HALF:
			case EXTRA_FIRST_HALF:
			case EXTRA_SECOND_HALF:
			case PENALTY_SHOOTOUT:
				return true;
			default:
				return false;
		}
	}
	
	
	private boolean isNoGameStage(final SSL_Referee refMsg)
	{
		switch (refMsg.getStage())
		{
			case EXTRA_HALF_TIME:
			case NORMAL_HALF_TIME:
			case PENALTY_SHOOTOUT_BREAK:
			case POST_GAME:
			case EXTRA_TIME_BREAK:
				return true;
			default:
				return false;
		}
	}
	
	
	@Override
	public void onNewRefereeMsg(final SSL_Referee refMsg)
	{
		if (SumatraModel.getInstance().isProductive() && (refMsg != null)
				&& refMsg.getCommandCounter() != lastCommandCounter)
		{
			if (!isRecording() &&
					(isGameStage(refMsg)
							|| (isPreStage(refMsg) && (refMsg.getCommand() != Command.HALT))))
			{
				startStopRecording(true);
			} else if (isRecording() && isNoGameStage(refMsg))
			{
				startStopRecording(false);
			}
			lastCommandCounter = refMsg.getCommandCounter();
		}
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
