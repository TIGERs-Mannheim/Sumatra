/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.AModule;
import edu.tigers.sumatra.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.persistence.log.PersistenceLogEvent;
import edu.tigers.sumatra.persistence.log.PersistenceLogRecorder;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Manager for central control of recordings
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RecordManager extends AModule implements IRefereeObserver
{
	private static final Logger log = LogManager.getLogger(RecordManager.class.getName());
	private final List<IRecordObserver> observers = new CopyOnWriteArrayList<>();
	private final List<IRecorderHook> hooks = new CopyOnWriteArrayList<>();
	private long lastCommandCounter = -1;
	private PersistenceAsyncRecorder recorder = null;
	protected String teamYellow = "";
	protected String teamBlue = "";
	protected String matchType = "";
	protected String matchStage = "";

	@Configurable(defValue = "false", comment = "Automatically compress recordings after they were closed")
	private static boolean compressOnClose = false;

	@Configurable(defValue = "true", comment = "Automatically record game in tournament mode")
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


	public void addHook(IRecorderHook hook)
	{
		hooks.add(hook);
	}


	public void removeHook(IRecorderHook hook)
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


	private boolean isPreStage(final SslGcRefereeMessage.Referee refMsg)
	{
		return switch (refMsg.getStage())
		{
			case NORMAL_FIRST_HALF_PRE, NORMAL_SECOND_HALF_PRE, EXTRA_FIRST_HALF_PRE, EXTRA_SECOND_HALF_PRE -> true;
			default -> false;
		};
	}


	private boolean isGameStage(final SslGcRefereeMessage.Referee refMsg)
	{
		return switch (refMsg.getStage())
		{
			case NORMAL_FIRST_HALF, NORMAL_SECOND_HALF, EXTRA_FIRST_HALF, EXTRA_SECOND_HALF, PENALTY_SHOOTOUT -> true;
			default -> false;
		};
	}


	private boolean isNoGameStage(final SslGcRefereeMessage.Referee refMsg)
	{
		return switch (refMsg.getStage())
		{
			case EXTRA_HALF_TIME, NORMAL_HALF_TIME, PENALTY_SHOOTOUT_BREAK, POST_GAME, EXTRA_TIME_BREAK -> true;
			default -> false;
		};
	}


	@Override
	public void onNewRefereeMsg(final SslGcRefereeMessage.Referee refMsg)
	{
		if (refMsg != null && recorder == null)
		{
			teamYellow = refMsg.getYellow().getName().replace(" ", "_");
			teamBlue = refMsg.getBlue().getName().replace(" ", "_");
			matchType = refMsg.getMatchType().toString();
			matchStage = refMsg.getStage().toString().replace("_PRE", "");
		}
		if (autoRecord && SumatraModel.getInstance().isTournamentMode() && (refMsg != null)
				&& refMsg.getCommandCounter() != lastCommandCounter)
		{
			startStopRecording(refMsg);
			lastCommandCounter = refMsg.getCommandCounter();
		}
	}


	private void startStopRecording(final SslGcRefereeMessage.Referee refMsg)
	{
		if (!isRecording() &&
				(isGameStage(refMsg)
						|| (isPreStage(refMsg) && (refMsg.getCommand() != SslGcRefereeMessage.Referee.Command.HALT))))
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
		recorder = new PersistenceAsyncRecorder(newPersistenceDb());
		recorder.getDb().setCompressOnClose(compressOnClose);
		onNewPersistanceRecorder(recorder);
		recorder.start();
		hooks.forEach(IRecorderHook::start);
		notifyStartStopRecord(true);
	}


	protected void stopRecording()
	{
		if (recorder == null)
		{
			log.warn("Record stop requested, but there is no recorder");
		} else
		{
			hooks.forEach(IRecorderHook::stop);
			recorder.stop();
			recorder = null;
		}
		notifyStartStopRecord(false);
	}


	/**
	 * Open or create a persistence database with default accessors attached
	 *
	 * @param dbPath at this location
	 * @return a new unopened handle
	 */
	public PersistenceDb newPersistenceDb(Path dbPath)
	{
		PersistenceDb db = PersistenceDb.withCustomLocation(dbPath);
		onNewPersistenceDb(db);
		return db;
	}


	/**
	 * Create a new persistence database with default accessors attached
	 *
	 * @return a new empty database
	 */
	private PersistenceDb newPersistenceDb()
	{
		PersistenceDb db = PersistenceDb.withDefaultLocation(matchType, matchStage, teamYellow, teamBlue);
		onNewPersistenceDb(db);
		return db;
	}


	/**
	 * This is called when a new db will be created
	 *
	 * @param db
	 */
	protected void onNewPersistenceDb(PersistenceDb db)
	{
		db.add(PersistenceLogEvent.class, EPersistenceKeyType.ARBITRARY);
	}


	/**
	 * This is called when a new recorder will be created
	 *
	 * @param recorder
	 */
	protected void onNewPersistanceRecorder(PersistenceAsyncRecorder recorder)
	{
		recorder.add(new PersistenceLogRecorder(recorder.getDb()));
	}


	/**
	 * @return
	 */
	private synchronized boolean isRecording()
	{
		return recorder != null;
	}
}
