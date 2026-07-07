package edu.tigers.sumatra.presenter;

import edu.tigers.autoref.presenter.BallSpeedPresenter;
import edu.tigers.autoref.presenter.GameLogPresenter;
import edu.tigers.sumatra.AMainPresenter;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.gui.ai.botoverview.presenter.BotOverviewPresenter;
import edu.tigers.sumatra.gui.ai.offensive.interceptions.presenter.OffensiveInterceptionsPresenter;
import edu.tigers.sumatra.gui.ai.offensive.statistics.presenter.OffensiveStatisticsPresenter;
import edu.tigers.sumatra.gui.ai.offensive.strategy.presenter.OffensiveStrategyPresenter;
import edu.tigers.sumatra.gui.ai.statistics.presenter.StatisticsPresenter;
import edu.tigers.sumatra.gui.ai.support.presenter.SupportBehaviorsPresenter;
import edu.tigers.sumatra.gui.log.presenter.LogPresenter;
import edu.tigers.sumatra.gui.replay.presenter.IReplayController;
import edu.tigers.sumatra.gui.replay.presenter.ReplayAutoRefReCalcController;
import edu.tigers.sumatra.gui.replay.presenter.ReplayCamDetectionController;
import edu.tigers.sumatra.gui.replay.presenter.ReplayControlPresenter;
import edu.tigers.sumatra.gui.replay.presenter.ReplayLogController;
import edu.tigers.sumatra.gui.replay.presenter.ReplayShapeMapController;
import edu.tigers.sumatra.gui.replay.presenter.ReplayWfwController;
import edu.tigers.sumatra.gui.replay.view.IReplayControlPanelObserver;
import edu.tigers.sumatra.gui.replay.view.IReplayPositionObserver;
import edu.tigers.sumatra.gui.visualizer.presenter.VisualizerPresenter;
import edu.tigers.sumatra.persistence.EPersistenceKeyType;
import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.persistence.PersistenceTable;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.replay.ReplayAiController;
import edu.tigers.sumatra.replay.ReplayAiReCalcController;
import edu.tigers.sumatra.snapshot.SnapshotController;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.util.ShortcutsDialog;
import edu.tigers.sumatra.view.ReplayMainFrame;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraPresenter;
import edu.tigers.sumatra.views.SumatraView;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;


@Log4j2
public class ReplayMainPresenter extends AMainPresenter<ReplayMainFrame>
		implements IReplayControlPanelObserver, IWorldFrameObserver
{
	private static final double REFRESH_FPS = 30;

	private final List<IReplayPositionObserver> positionObservers = new CopyOnWriteArrayList<>();
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
			new NamedThreadFactory("Replay"));
	private final SnapshotController snapshotController;
	private final List<IReplayController> replayControllers = new ArrayList<>();
	private ReplayControlPresenter replayControlPresenter;
	private PersistenceDb db = null;
	private double speed = 1;
	private RefreshThread refreshThread;

	private boolean skipStoppedGame = false;
	private SslGcRefereeMessage.Referee.Command searchCommand = null;
	private EGameEvent searchGameEvent = null;
	private EGameState searchGameState = null;
	private boolean skipBallPlacement = false;


	public ReplayMainPresenter()
	{
		super(new ReplayMainFrame(), createViews(), "replay");

		getMainFrame().getShortcutMenuItem().addActionListener(_ -> new ShortcutsDialog(getMainFrame()));

		addReplayController(new ReplayAiReCalcController(getViews()));
		addReplayController(new ReplayAiController(getViews()));

		for (SumatraView view : getViews())
		{
			if (view.getType() == ESumatraViewType.REPLAY_CONTROL)
			{
				view.ensureInitialized();
				replayControlPresenter = (ReplayControlPresenter) view.getPresenter();
			}

			if (view.getType() == ESumatraViewType.VISUALIZER)
			{
				view.ensureInitialized();
				var visualizerPresenter = (VisualizerPresenter) view.getPresenter();
				visualizerPresenter.setPropertiesPrefix(
						ReplayMainPresenter.class.getCanonicalName() + ".VisualizerPresenter.");
			}
		}

		List<IWorldFrameObserver> wFrameObservers = getObservers(IWorldFrameObserver.class);
		replayControllers.add(new ReplayWfwController(wFrameObservers, this::updateWorldframe));
		replayControllers.add(new ReplayLogController(getViews()));
		replayControllers.add(new ReplayCamDetectionController(wFrameObservers));
		replayControllers.add(new ReplayShapeMapController(wFrameObservers));
		replayControllers.add(new ReplayAutoRefReCalcController(wFrameObservers, getViews()));

		snapshotController = new SnapshotController(getMainFrame());
		snapshotController.setSaveMoveDestinations(true);

		replayControlPresenter.getViewPanel().addObserver(this);
		addPositionObserver(replayControlPresenter.getViewPanel());
	}


	private static List<SumatraView> createViews()
	{
		return List.of(
				new SumatraView(ESumatraViewType.LOG, () -> new LogPresenter(false)),
				new SumatraView(ESumatraViewType.REPLAY_CONTROL, ReplayControlPresenter::new),
				new SumatraView(ESumatraViewType.AUTOREFEREE_GAME_LOG, GameLogPresenter::new),
				new SumatraView(ESumatraViewType.VISUALIZER, VisualizerPresenter::new),
				new SumatraView(ESumatraViewType.BALL_SPEED, BallSpeedPresenter::new),
				new SumatraView(ESumatraViewType.BOT_OVERVIEW, BotOverviewPresenter::new),
				new SumatraView(ESumatraViewType.STATISTICS, StatisticsPresenter::new),
				new SumatraView(ESumatraViewType.OFFENSIVE_STRATEGY, OffensiveStrategyPresenter::new),
				new SumatraView(ESumatraViewType.OFFENSIVE_STATISTICS, OffensiveStatisticsPresenter::new),
				new SumatraView(ESumatraViewType.OFFENSIVE_INTERCEPTIONS, OffensiveInterceptionsPresenter::new),
				new SumatraView(ESumatraViewType.SUPPORT_BEHAVIORS, SupportBehaviorsPresenter::new)
		);
	}


	/**
	 * Collect all presenters that implement the given type.
	 *
	 * @param type the observer type
	 * @param <U>  the observer type
	 * @return list of all presenters that implement the type
	 */
	private <U> List<U> getObservers(Class<U> type)
	{
		return getPresenters()
				.filter(p -> type.isAssignableFrom(p.getClass()))
				.map(type::cast)
				.toList();
	}


	/**
	 * @return a stream of all presenters (including children)
	 */
	private Stream<ISumatraPresenter> getPresenters()
	{
		return getViews().stream()
				.flatMap(SumatraView::getPresenters);
	}


	private void addReplayController(IReplayController replayController)
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
	public void start(final PersistenceDb db, long startTime)
	{
		this.db = db;
		getMainFrame().setTitle(new File(db.getDbPath()).getName());
		refreshThread = new RefreshThread(startTime);
		getPresenters().forEach(ISumatraPresenter::onStart);
		executor.execute(refreshThread);
	}


	/**
	 * Stop replay
	 */
	private void stop()
	{
		getPresenters().forEach(ISumatraPresenter::onStop);
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
	public void onClose()
	{
		super.onClose();
		stop();
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


	@Override
	public void cutReplay()
	{
		String lengthString = JOptionPane.showInputDialog("Create a new recording from the previous n seconds.", 10);
		if (lengthString == null)
		{
			return;
		}

		CompletableFuture.runAsync(() -> cutTableAsync(lengthString)).exceptionally(e -> {
			log.error("Failed to cut the replay.", e);
			return null;
		});
	}


	private void cutTableAsync(String lengthString)
	{
		long length = (long) (Double.parseDouble(lengthString) * 1e9);
		long end = refreshThread.getCurrentTime();

		var wfwTable = db.getTable(WorldFrameWrapper.class);
		long unixEnd = wfwTable.get(wfwTable.getNearestKey(end)).getUnixTimestamp();

		PersistenceDb cutDb = new PersistenceDb(
				Path.of(db.getDbPath() + "-cut-" + refreshThread.currentTimeFormatted("HH_mm_ss-SSS"))
		);

		log.info("Creating replay cut {}", cutDb.getDbPath());
		db.forEachTable(table -> {
			if(table.getKeyType() == EPersistenceKeyType.SUMATRA_TIMESTAMP)
				copySection(cutDb, table, end - length, end);
			else
				copySection(cutDb, table, unixEnd - length, unixEnd);
		});

		cutDb.setCompressOnClose(true);
		cutDb.close();

		try
		{
			cutDb.delete();
		} catch (IOException e)
		{
			log.error("Failed to delete the uncompressed replay.", e);
		}
	}


	private <T extends PersistenceTable.IEntry<T>> void copySection(
			PersistenceDb cutDb, PersistenceTable<T> table,
			long start, long end
	)
	{
		cutDb.add(table.getType(), table.getKeyType());
		PersistenceTable<T> cutTable = cutDb.getTable(table.getType());
		Long time = table.getNearestKey(start);
		while (time != null && time < end)
		{
			T data = table.get(time);
			cutDb.getIoExecutor().execute(() -> cutTable.write(data));
			time = table.getNextKey(time);
		}
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
				replayCurTime += Math.round((System.nanoTime() - replayLastTime) * speed);
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
			PersistenceTable<WorldFrameWrapper> table = db.getTable(WorldFrameWrapper.class);
			for (long t = getCurrentTime(); t < recEndTime; t += 250_000_000)
			{
				WorldFrameWrapper wfw = table.get(table.getNearestKey(t));
				boolean skipStop = !skipStoppedGame || !skipFrameStoppedGame(wfw);
				boolean command = searchCommand == null || !skipFrameCommand(wfw);
				boolean gameEvent = searchGameEvent == null || !skipFrameGameEvent(wfw);
				boolean gameState = searchGameState == null || !skipFrameGameState(wfw);
				boolean skipPlacement = !skipBallPlacement || !skipFrameBallPlacement(wfw);

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
			replayControlPresenter.getViewPanel().getTimeStepLabel().setText(currentTimeFormatted("HH:mm:ss,SSS"));
		}


		public String currentTimeFormatted(String pattern)
		{
			Date date = new Date(Math.round(replayCurTime / 1e6));
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			return sdf.format(date);
		}
	}
}
