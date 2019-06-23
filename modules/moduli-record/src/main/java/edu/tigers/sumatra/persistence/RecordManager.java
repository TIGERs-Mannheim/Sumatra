/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.log.BerkeleyLogEvent;
import edu.tigers.sumatra.persistence.log.BerkeleyLogRecorder;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;


/**
 * Manager for central control of recordings
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RecordManager extends AModule implements IRefereeObserver
{
	private static final Logger log = Logger.getLogger(RecordManager.class.getName());
	private final List<IRecordObserver> observers = new CopyOnWriteArrayList<>();
	private final List<IBerkeleyRecorderHook> hooks = new CopyOnWriteArrayList<>();
	private long lastCommandCounter = -1;
	private BerkeleyAsyncRecorder recorder = null;
	
	@Configurable(defValue = "false", comment = "Automatically compress recordings after they were closed")
	private static boolean compressOnClose = false;
	
	@Configurable(defValue = "true", comment = "Automatically record game in productive/match mode (-Dproductive=true)")
	private static boolean autoRecord = true;
	
	static
	{
		ConfigRegistration.registerClass("user", RecordManager.class);
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
	
	
	public void addHook(IBerkeleyRecorderHook hook)
	{
		hooks.add(hook);
	}
	
	
	public void removeHook(IBerkeleyRecorderHook hook)
	{
		hooks.remove(hook);
	}
	
	
	private void notifyStartStopRecord(final boolean recording)
	{
		for (IRecordObserver observer : observers)
		{
			observer.onStartStopRecord(recording);
		}
	}
	
	
	public void pauseRecorder()
	{
		recorder.pause();
	}
	
	
	public void resumeRecorder()
	{
		recorder.resume();
	}
	
	
	private boolean isPreStage(final Referee.SSL_Referee refMsg)
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
	
	
	private boolean isGameStage(final Referee.SSL_Referee refMsg)
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
	
	
	private boolean isNoGameStage(final Referee.SSL_Referee refMsg)
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
	public void onNewRefereeMsg(final Referee.SSL_Referee refMsg)
	{
		if (autoRecord && SumatraModel.getInstance().isProductive() && (refMsg != null)
				&& refMsg.getCommandCounter() != lastCommandCounter)
		{
			startStopRecording(refMsg);
			lastCommandCounter = refMsg.getCommandCounter();
		}
	}
	
	
	private void startStopRecording(final Referee.SSL_Referee refMsg)
	{
		if (!isRecording() &&
				(isGameStage(refMsg)
						|| (isPreStage(refMsg) && (refMsg.getCommand() != Referee.SSL_Referee.Command.HALT))))
		{
			startRecording();
		} else if (isRecording() && isNoGameStage(refMsg))
		{
			stopRecording();
		}
	}
	
	
	@Override
	public void initModule()
	{
		// nothing to do
	}
	
	
	@Override
	public void deinitModule()
	{
		// nothing to do
	}
	
	
	@Override
	public void startModule()
	{
		try
		{
			AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
			referee.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find Referee module", e);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		try
		{
			AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
			referee.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find Referee module", e);
		}
	}
	
	
	/**
	 * Toggle recording state
	 */
	public synchronized void toggleRecording()
	{
		if (recorder == null)
		{
			startRecording();
		} else
		{
			stopRecording();
		}
	}
	
	
	protected void startRecording()
	{
		if (recorder != null)
		{
			log.warn("Start recording requested, but there is still an active recorder. Stopping it.");
			recorder.stop();
		}
		recorder = new BerkeleyAsyncRecorder(newBerkeleyDb());
		recorder.getDb().getEnv().setCompressOnClose(compressOnClose);
		onNewBerkeleyRecorder(recorder);
		recorder.start();
		hooks.forEach(IBerkeleyRecorderHook::start);
		notifyStartStopRecord(true);
	}
	
	
	protected void stopRecording()
	{
		if (recorder == null)
		{
			log.warn("Record stop requested, but there is no recorder");
		} else
		{
			hooks.forEach(IBerkeleyRecorderHook::stop);
			recorder.stop();
			recorder = null;
		}
		notifyStartStopRecord(false);
	}
	
	
	/**
	 * Open or create a berkeley database with default accessors attached
	 * 
	 * @param dbPath at this location
	 * @return a new unopened handle
	 */
	public BerkeleyDb newBerkeleyDb(Path dbPath)
	{
		BerkeleyDb db = BerkeleyDb.withCustomLocation(dbPath);
		onNewBerkeleyDb(db);
		return db;
	}
	
	
	/**
	 * Create a new berkeley database with default accessors attached
	 * 
	 * @return a new empty database
	 */
	private BerkeleyDb newBerkeleyDb()
	{
		BerkeleyDb db = BerkeleyDb.withDefaultLocation();
		onNewBerkeleyDb(db);
		return db;
	}
	
	
	/**
	 * @return the current DB path
	 */
	private synchronized String getCurrentDbPath()
	{
		if (recorder == null)
		{
			return "";
		}
		return recorder.getDb().getDbPath();
	}
	
	
	/**
	 * This is called when a new berkeley db will be created
	 * 
	 * @param db
	 */
	protected void onNewBerkeleyDb(BerkeleyDb db)
	{
		db.add(BerkeleyLogEvent.class, new BerkeleyAccessor<>(BerkeleyLogEvent.class, false));
	}
	
	
	/**
	 * This is called when a new berkeley recorder will be created
	 * 
	 * @param recorder
	 */
	protected void onNewBerkeleyRecorder(BerkeleyAsyncRecorder recorder)
	{
		recorder.add(new BerkeleyLogRecorder(recorder.getDb()));
	}
	
	
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
		BerkeleyDb db;
		try
		{
			db = newBerkeleyDb(Paths.get(dbPath));
			db.open();
		} catch (Exception e)
		{
			log.error("Could not open DB", e);
			return;
		}
		for (IRecordObserver observer : observers)
		{
			observer.onViewReplay(db, startTime);
		}
	}
	
	
	/**
	 * @return
	 */
	private synchronized boolean isRecording()
	{
		return recorder != null;
	}
}
