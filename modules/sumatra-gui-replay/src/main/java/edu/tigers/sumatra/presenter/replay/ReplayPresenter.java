/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.AMainPresenter;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.snapshot.SnapshotController;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.view.replay.IReplayControlPanelObserver;
import edu.tigers.sumatra.view.replay.IReplayPositionObserver;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.EViewMode;
import edu.tigers.sumatra.views.ISumatraPresenter;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


@Log4j2
public class ReplayPresenter extends AMainPresenter
		implements IReplayControlPanelObserver, IWorldFrameObserver
{
	private static final String LAST_LAYOUT_FILENAME = "last_replay.ly";
	private static final String LAYOUT_DEFAULT = "default_replay.ly";
	private static final String KEY_LAYOUT_PROP = ReplayPresenter.class.getName() + ".layout";
	private static final double REFRESH_FPS = 30;

	private final List<IReplayPositionObserver> positionObservers = new CopyOnWriteArrayList<>();
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
			new NamedThreadFactory("Replay"));
	private final SnapshotController snapshotController;
	private final List<IReplayController> replayControllers = new ArrayList<>();
	private ReplayControlPresenter replayControlPresenter;
	private BerkeleyDb db = null;
	private double speed = 1;
	private RefreshThread refreshThread;

	private boolean skipStoppedGame = false;
	private SslGcRefereeMessage.Referee.Command searchCommand = null;
	private EGameEvent searchGameEvent = null;
	private EGameState searchGameState = null;
	private boolean skipBallPlacement = false;


	/**
	 * Default
	 *
	 * @param mainFrame e.g. ReplayWindow.java
	 */
	public ReplayPresenter(AMainFrame mainFrame)
	{
		super(mainFrame);

		for (ASumatraView view : getMainFrame().getViews())
		{
			view.setMode(EViewMode.REPLAY);
			view.ensureInitialized();

			if (view.getType() == ESumatraViewType.REPLAY_CONTROL)
			{
				replayControlPresenter = (ReplayControlPresenter) view.getPresenter();
			}
		}

		List<IWorldFrameObserver> wFrameObservers = getMainFrame().getObservers(IWorldFrameObserver.class);
		replayControllers.add(new ReplayWfwController(wFrameObservers, this::updateWorldframe));
		replayControllers.add(new ReplayLogController(getMainFrame().getViews()));
		replayControllers.add(new ReplayCamDetectionController(wFrameObservers));
		replayControllers.add(new ReplayShapeMapController(wFrameObservers));
		replayControllers.add(new ReplayAutoRefReCalcController(wFrameObservers, getMainFrame().getViews()));

		snapshotController = new SnapshotController(getMainFrame());
		snapshotController.setSaveMoveDestinations(true);

		replayControlPresenter.getViewPanel().addObserver(this);
		addPositionObserver(replayControlPresenter.getViewPanel());
		getMainFrame().activate();
	}


	protected void addReplayController(IReplayController replayController)
	{
		replayControllers.add(replayController);
	}


	private void updateWorldframe(WorldFrameWrapper wFrameWrapper)
	{
		replayControllers.forEach(c -> c.update(db, wFrameWrapper));
		snapshotController.updateWorldFrame(wFrameWrapper);
	}


	/**
	 * Activate this window by setting it visible and start refresh thread
	 *
	 * @param db
	 * @param startTime start time within recording
	 */
	public void start(final BerkeleyDb db, long startTime)
	{
		this.db = db;
		getMainFrame().setTitle(new File(db.getDbPath()).getName());
		refreshThread = new RefreshThread(startTime);
		getMainFrame().getPresenters().forEach(ISumatraPresenter::onStart);
		executor.execute(refreshThread);
	}


	/**
	 * Stop replay
	 */
	private void stop()
	{
		getMainFrame().getPresenters().forEach(ISumatraPresenter::onStop);
		refreshThread.active = false;
		if (executor.isShutdown())
		{
			log.warn("Tried to close controller multiple times.");
			return;
		}
		executor.execute(this::cleanup);
		executor.shutdown();
	}


	private void cleanup()
	{
		if (db != null)
		{
			db.close();
		}
	}


	/**
	 * @param o
	 */
	@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance should be used carefully
	private void addPositionObserver(final IReplayPositionObserver o)
	{
		positionObservers.add(o);
	}


	@Override
	public void onExit()
	{
		super.onExit();
		stop();
		getMainFrame().dispose();
	}


	@Override
	protected String getLayoutKey()
	{
		return KEY_LAYOUT_PROP;
	}


	@Override
	public String getLastLayoutFile()
	{
		return LAST_LAYOUT_FILENAME;
	}


	@Override
	protected String getDefaultLayout()
	{
		return LAYOUT_DEFAULT;
	}


	@Override
	public void onSetSpeed(final double speed)
	{
		this.speed = speed;
	}


	@Override
	public void onPlayPause(final boolean playing)
	{
		refreshThread.setPlaying(playing);
	}


	@Override
	public void onChangeAbsoluteTime(final long time)
	{
		refreshThread.jumpTime(time);
	}


	@Override
	public void onChangeRelativeTime(final long relTime)
	{
		refreshThread.jumpRelativeTime(relTime);
	}


	@Override
	public void onNextFrame()
	{
		refreshThread.jumpNextFrame();
	}


	@Override
	public void onPreviousFrame()
	{
		refreshThread.jumpPreviousFrame();
	}


	@Override
	public void onSearchCommand(final SslGcRefereeMessage.Referee.Command command)
	{
		this.searchCommand = command;
	}


	@Override
	public void onSearchGameEvent(final EGameEvent gameEvent)
	{
		this.searchGameEvent = gameEvent;
	}


	@Override
	public void onSearchGameState(final EGameState gameState)
	{
		this.searchGameState = gameState;
	}


	@Override
	public void onSetSkipStop(final boolean enable)
	{
		this.skipStoppedGame = enable;
	}


	@Override
	public void onSetSkipBallPlacement(final boolean enable)
	{
		this.skipBallPlacement = enable;
	}


	@Override
	public void onSnapshot()
	{
		snapshotController.onSnapshot();
	}


	@Override
	public void onCopySnapshot()
	{
		snapshotController.onCopySnapshot();
	}


	/**
	 * This thread will update the field periodically according to the speed
	 *
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	private class RefreshThread implements Runnable
	{
		private long replayLastTime = System.nanoTime();
		private long replayCurTime = 0;

		private long recEndTime;
		private Long recStartTime;

		private long lastKey = 0;

		private boolean playing = true;
		private boolean active = true;


		/**
		 * @param recCurTime the start time in this log file
		 */
		public RefreshThread(long recCurTime)
		{
			this.recStartTime = db.getFirstKey();
			if (recStartTime != null)
			{
				this.replayCurTime = recCurTime - recStartTime;
			}
		}


		private void notifyPositionChanged(final long pos)
		{
			for (IReplayPositionObserver o : positionObservers)
			{
				o.onPositionChanged(pos);
			}
		}


		private void updateReplayTime()
		{
			updateEndTime();
			if (playing)
			{
				replayCurTime += ((System.nanoTime() - replayLastTime) * speed);
				if (replayCurTime < 0)
				{
					replayCurTime = 0;
				}
				if (getCurrentTime() > recEndTime)
				{
					replayCurTime = recEndTime;
				}
			}
			replayLastTime = System.nanoTime();
		}


		private void updateEndTime()
		{
			long newRecEndTime = db.getLastKey();
			if (newRecEndTime != recEndTime)
			{
				replayControlPresenter.getViewPanel().setTimeMax(newRecEndTime - refreshThread.recStartTime);
			}
			recEndTime = newRecEndTime;
		}


		private long getCurrentTime()
		{
			return recStartTime + replayCurTime;
		}


		/**
		 * @param time
		 */
		public synchronized void jumpAbsoluteTime(final long time)
		{
			replayCurTime = time - recStartTime;
		}


		/**
		 * @param time
		 */
		public synchronized void jumpTime(final long time)
		{
			replayCurTime = time;
		}


		/**
		 * @param time time increment to jump
		 */
		public synchronized void jumpRelativeTime(final long time)
		{
			replayCurTime += time;
		}


		/**
		 * next frame
		 */
		public void jumpNextFrame()
		{
			Long key = db.getNextKey(lastKey);
			if (key != null)
			{
				jumpAbsoluteTime(key);
			}
		}


		/**
		 * last frame
		 */
		public void jumpPreviousFrame()
		{
			Long key = db.getPreviousKey(lastKey);
			if (key != null)
			{
				jumpAbsoluteTime(key);
			}
		}


		public void setPlaying(final boolean playing)
		{
			this.playing = playing;
		}


		private void skipFrames()
		{
			for (long t = getCurrentTime(); t < recEndTime; t += 250_000_000)
			{
				boolean skipStop = !skipStoppedGame || !skipFrameStoppedGame(db.get(WorldFrameWrapper.class, t));
				boolean command = searchCommand == null || !skipFrameCommand(db.get(WorldFrameWrapper.class, t));
				boolean gameEvent = searchGameEvent == null || !skipFrameGameEvent(db.get(WorldFrameWrapper.class, t));
				boolean gameState = searchGameState == null || !skipFrameGameState(db.get(WorldFrameWrapper.class, t));
				boolean skipPlacement = !skipBallPlacement || !skipFrameBallPlacement(db.get(WorldFrameWrapper.class, t));

				if (skipStop && command && gameEvent && skipPlacement && gameState)
				{
					jumpAbsoluteTime(t);
					break;
				}
			}
			searchCommand = null;
			searchGameEvent = null;
			searchGameState = null;
		}


		private boolean skipFrameStoppedGame(final WorldFrameWrapper wfw)
		{
			RefereeMsg refMsg = wfw.getRefereeMsg();
			return refMsg != null && wfw.getGameState().isStoppedGame();
		}


		private boolean skipFrameBallPlacement(final WorldFrameWrapper wfw)
		{
			return wfw.getGameState().isBallPlacement();
		}


		private boolean skipFrameCommand(final WorldFrameWrapper wfw)
		{
			RefereeMsg refMsg = wfw.getRefereeMsg();
			return refMsg != null && refMsg.getCommand() != searchCommand;
		}


		private boolean skipFrameGameEvent(final WorldFrameWrapper wfw)
		{
			RefereeMsg refMsg = wfw.getRefereeMsg();
			return refMsg != null && refMsg.getGameEvents().stream()
					.map(IGameEvent::getType)
					.noneMatch(gameEvent -> gameEvent == searchGameEvent);
		}


		private boolean skipFrameGameState(final WorldFrameWrapper wfw)
		{
			return wfw.getGameState().getState() != searchGameState;
		}


		@SuppressWarnings({ "java:S1181", "java:S2142" }) // catch throwable and busy-wait false positives
		@Override
		public void run()
		{
			while (active)
			{
				long t0 = System.nanoTime();
				try
				{
					if (recStartTime == null)
					{
						//noinspection BusyWait
						Thread.sleep(1000);
						recStartTime = db.getFirstKey();
						continue;
					}

					update();
				} catch (Throwable err)
				{
					log.error("Error in RefreshThread.", err);
				}
				long t1 = System.nanoTime();

				long dt = (long) (1_000_000_000L / REFRESH_FPS);
				long sleep = dt - (t1 - t0);
				if (sleep > 0)
				{
					assert sleep < (long) 1e9;
					ThreadUtil.parkNanosSafe(sleep);
				}
			}
		}


		private void update()
		{
			skipFrames();
			updateReplayTime();
			notifyPositionChanged(replayCurTime);

			long curT = getCurrentTime();

			lastKey = db.getKey(curT);

			replayControllers.forEach(r -> r.update(db, lastKey));
			updateTimeStep(Math.round(replayCurTime / 1e6));
		}


		/**
		 * get the current TimeStep
		 */
		private void updateTimeStep(final long timestamp)
		{
			Date date = new Date(timestamp);
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			String txt = sdf.format(date);
			replayControlPresenter.getViewPanel().getTimeStepLabel().setText(txt);
		}
	}
}
