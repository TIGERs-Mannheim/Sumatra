/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.replay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.AMainPresenter;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.snapshot.SnapshotController;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.view.replay.IReplayControlPanelObserver;
import edu.tigers.sumatra.view.replay.IReplayPositionObserver;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.visualizer.VisualizerPresenter;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class ReplayPresenter extends AMainPresenter
		implements IReplayControlPanelObserver, IWorldFrameObserver
{
	private static final Logger log = Logger.getLogger(ReplayPresenter.class.getName());
	
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
	
	private VisualizerPresenter visualizerPresenter;
	
	private boolean skipStoppedGame = false;
	private boolean searchKickoff = false;
	private boolean skipBallPlacement = false;
	
	
	/**
	 * Default
	 */
	public ReplayPresenter(AMainFrame mainFrame)
	{
		super(mainFrame);
		
		for (ASumatraView view : getMainFrame().getViews())
		{
			view.setMode(ASumatraView.EViewMode.REPLAY);
			view.ensureInitialized();
			
			if (view.getType() == ESumatraViewType.REPLAY_CONTROL)
			{
				replayControlPresenter = (ReplayControlPresenter) view.getPresenter();
			}
			if (view.getType() == ESumatraViewType.VISUALIZER)
			{
				visualizerPresenter = (VisualizerPresenter) view.getPresenter();
				visualizerPresenter.getPanel().removeRobotsPanel();
				visualizerPresenter.getOptionsPanelPresenter().setSaveOptions(false);
			}
		}
		
		final ReplayWfwController replayWfwController = new ReplayWfwController(getMainFrame().getViews());
		replayWfwController.addWFrameObserver(this);
		replayControllers.add(replayWfwController);
		replayControllers.add(new ReplayLogController(getMainFrame().getViews()));
		replayControllers.add(new ReplayCamDetectionController(getMainFrame().getViews()));
		replayControllers.add(new ReplayAutoRefReCalcController(getMainFrame().getViews()));
		replayControllers.add(new ReplayShapeMapController(getMainFrame().getViews()));
		
		snapshotController = new SnapshotController(getMainFrame());
		
		replayControlPresenter.getReplayPanel().addObserver(this);
		addPositionObserver(replayControlPresenter.getReplayPanel());
		getMainFrame().activate();
	}
	
	
	protected void addReplayController(IReplayController replayController)
	{
		replayControllers.add(replayController);
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
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
		refreshThread = new RefreshThread(startTime);
		visualizerPresenter.start();
		executor.execute(refreshThread);
	}
	
	
	/**
	 * Stop replay
	 */
	private void stop()
	{
		visualizerPresenter.stop();
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
	public void onSearchKickoff(final boolean enable)
	{
		this.searchKickoff = enable;
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
				if ((getCurrentTime() > recEndTime) || (replayCurTime < 0))
				{
					replayCurTime = 0;
				}
			}
			replayLastTime = System.nanoTime();
		}
		
		
		private void updateEndTime()
		{
			long newRecEndTime = db.getLastKey();
			if (newRecEndTime != recEndTime)
			{
				replayControlPresenter.getReplayPanel()
						.setTimeMax(newRecEndTime - refreshThread.recStartTime);
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
			long t = getCurrentTime();
			for (; t < recEndTime; t += 250_000_000)
			{
				boolean skipStop = !skipStoppedGame || !skipFrameStoppedGame(db.get(WorldFrameWrapper.class, t));
				boolean kickoff = !searchKickoff || !skipFrameKickoff(db.get(WorldFrameWrapper.class, t));
				boolean skipPlacement = !skipBallPlacement || !skipFrameBallPlacement(db.get(WorldFrameWrapper.class, t));

				if (skipStop && kickoff && skipPlacement)
				{
					jumpAbsoluteTime(t);
					break;
				}
			}
			searchKickoff = false;
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
		
		
		private boolean skipFrameKickoff(final WorldFrameWrapper wfw)
		{
			RefereeMsg refMsg = wfw.getRefereeMsg();
			return (refMsg != null)
					&& !((refMsg.getCommand() == Referee.SSL_Referee.Command.PREPARE_KICKOFF_BLUE)
							|| (refMsg.getCommand() == Referee.SSL_Referee.Command.PREPARE_KICKOFF_YELLOW));
		}
		
		
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
			replayControlPresenter.getReplayPanel().getTimeStepLabel().setText(txt);
		}
	}
}
