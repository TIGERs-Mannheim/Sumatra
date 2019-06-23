/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.replay.ReplayControlPresenter;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.IReplayControlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.IReplayPositionObserver;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.bot.DummyBot;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.persistance.BerkeleyLogEvent;
import edu.tigers.sumatra.persistance.IRecordPersistence;
import edu.tigers.sumatra.persistance.RecordCamFrame;
import edu.tigers.sumatra.persistance.RecordFrame;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.snapshot.SnapObject;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.view.ReplayWindow;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ASumatraView.EViewMode;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.visualizer.VisualizerPresenter;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ShapeMap;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Presenter for Replay GUI
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayPresenter extends AMainPresenter implements IReplayControlPanelObserver
{
	private static final Logger							log						= Logger.getLogger(ReplayPresenter.class
			.getName());
	
	private static final String							LAST_LAYOUT_FILENAME	= "last_replay.ly";
	/** */
	private static final String							LAYOUT_DEFAULT			= "default_replay.ly";
	protected static final String							KEY_LAYOUT_PROP		= ReplayPresenter.class.getName() + ".layout";
	
	private static final long								LOG_BUFFER_BEFORE		= 500;
	private static final long								LOG_BUFFER_AFTER		= 500;
	
	private final List<IReplayPositionObserver>		positionObservers		= new CopyOnWriteArrayList<IReplayPositionObserver>();
	
	private IRecordPersistence								persistance				= null;
	private List<BerkeleyLogEvent>						logEventBuffer			= null;
	
	
	private final ScheduledExecutorService				executor					= Executors
			.newSingleThreadScheduledExecutor(
					new NamedThreadFactory(
							"Replay"));
	
	private static final double							refreshFps				= 30;
	private double												speed						= 1;
	private boolean											skipStoppedGame		= false;
	private boolean											searchKickoff			= false;
	private boolean											runCurrentAi			= false;
	
	private final Map<ETeamColor, Agent>				agents					= new EnumMap<>(ETeamColor.class);
	
	private List<LoggingEvent>								lastLogEventsPast		= new LinkedList<LoggingEvent>();
	private List<LoggingEvent>								lastLogEventsFuture	= new LinkedList<LoggingEvent>();
	
	private final List<IVisualizationFrameObserver>	visFrameObservers		= new CopyOnWriteArrayList<>();
	private final List<IWorldFrameObserver>			wFrameObservers		= new CopyOnWriteArrayList<>();
	
	private WorldFrameWrapper								lastWorldFrame;
	
	private LogPresenter										logPresenter;
	private ReplayControlPresenter						replayControlPresenter;
	private VisualizerPresenter							visualizerPresenter;
	
	private RefreshThread									refreshThread;
	
	
	/**
	 */
	public ReplayPresenter()
	{
		super(new ReplayWindow());
		
		for (ASumatraView view : getMainFrame().getViews())
		{
			view.setMode(EViewMode.REPLAY);
			view.ensureInitialized();
			if (view.getPresenter() instanceof IVisualizationFrameObserver)
			{
				addVisFrameObserver((IVisualizationFrameObserver) view.getPresenter());
			}
			if (view.getPresenter() instanceof IWorldFrameObserver)
			{
				addWFrameObserver((IWorldFrameObserver) view.getPresenter());
			}
			if (view.getType() == ESumatraViewType.LOG)
			{
				logPresenter = (LogPresenter) view.getPresenter();
			}
			if (view.getType() == ESumatraViewType.REPLAY_CONTROL)
			{
				replayControlPresenter = (ReplayControlPresenter) view.getPresenter();
			}
			if (view.getType() == ESumatraViewType.VISUALIZER)
			{
				visualizerPresenter = (VisualizerPresenter) view.getPresenter();
			}
		}
		
		addWFrameObserver(new IWorldFrameObserver()
		{
			@Override
			public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
			{
				lastWorldFrame = wFrameWrapper;
			}
		});
		
		replayControlPresenter.getReplayPanel().getControlPanel().addObserver(this);
		addPositionObserver(replayControlPresenter.getReplayPanel().getControlPanel());
		getMainFrame().activate();
	}
	
	
	/**
	 * @param observer
	 */
	public void addVisFrameObserver(final IVisualizationFrameObserver observer)
	{
		visFrameObservers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeVisFrameObserver(final IVisualizationFrameObserver observer)
	{
		visFrameObservers.remove(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void addWFrameObserver(final IWorldFrameObserver observer)
	{
		wFrameObservers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeWFrameObserver(final IWorldFrameObserver observer)
	{
		wFrameObservers.remove(observer);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Activate this window by setting it visible and start refresh thread
	 * 
	 * @param persistance
	 */
	public void start(final IRecordPersistence persistance)
	{
		agents.put(ETeamColor.YELLOW, new Agent(ETeamColor.YELLOW));
		agents.put(ETeamColor.BLUE, new Agent(ETeamColor.BLUE));
		
		this.persistance = persistance;
		refreshThread = new RefreshThread();
		replayControlPresenter.getReplayPanel().getControlPanel()
				.setTimeMax(refreshThread.recEndTime - refreshThread.recStartTime);
		visualizerPresenter.start();
		executor.execute(refreshThread);
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
		visualizerPresenter.stop();
		refreshThread.active = false;
		if (executor.isShutdown())
		{
			log.warn("Tried to close controller multiple times.");
			return;
		}
		executor.shutdownNow();
		try
		{
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException err)
		{
			log.error("Interrupted while waiting for refresh executer", err);
		}
		if (persistance != null)
		{
			persistance.close();
		}
		
		for (Agent agent : agents.values())
		{
			agent.deinitModule();
		}
		agents.clear();
	}
	
	
	/**
	 * @param o
	 */
	public final void addPositionObserver(final IReplayPositionObserver o)
	{
		positionObservers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public final void removePositionObserver(final IReplayPositionObserver o)
	{
		positionObservers.remove(o);
	}
	
	
	private void notifyPositionChanged(final long pos)
	{
		for (IReplayPositionObserver o : positionObservers)
		{
			o.onPositionChanged(pos);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This thread will update the field periodically according to the speed
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	private class RefreshThread implements Runnable
	{
		private long			replayLastTime	= System.nanoTime();
		private long			replayCurTime	= 0;
		
		private final long	recEndTime;
		private final long	recStartTime;
		
		private long			lastKey			= 0;
		
		private boolean		playing			= true;
		private boolean		active			= true;
		
		
		/**
		 * 
		 */
		public RefreshThread()
		{
			recStartTime = persistance.getFirstKey();
			recEndTime = persistance.getLastKey();
		}
		
		
		private void updateReplayTime()
		{
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
		
		
		private long getCurrentTime()
		{
			long curT = recStartTime + replayCurTime;
			return curT;
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
		
		
		public synchronized void jumpRelativeTime(final long time)
		{
			replayCurTime += time;
		}
		
		
		public void jumpNextFrame()
		{
			Long key = persistance.getNextKey(lastKey);
			if (key != null)
			{
				jumpAbsoluteTime(key);
			}
		}
		
		
		public void jumpPreviousFrame()
		{
			Long key = persistance.getPreviousKey(lastKey);
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
				if (skipStoppedGame && skipFrameStoppedGame(persistance.getRecordFrame(t).getWorldFrameWrapper()))
				{
					continue;
				}
				if (searchKickoff && skipFrameKickoff(persistance.getRecordFrame(t).getWorldFrameWrapper()))
				{
					continue;
				}
				jumpAbsoluteTime(t);
				break;
			}
			searchKickoff = false;
		}
		
		
		private boolean skipFrameStoppedGame(final WorldFrameWrapper wfw)
		{
			RefereeMsg refMsg = wfw.getRefereeMsg();
			if (refMsg == null)
			{
				return false;
			}
			switch (wfw.getGameState())
			{
				case BREAK:
				case HALTED:
				case POST_GAME:
				case STOPPED:
				case TIMEOUT_BLUE:
				case TIMEOUT_YELLOW:
				case UNKNOWN:
					return true;
				default:
					break;
			}
			
			return false;
		}
		
		
		private boolean skipFrameKickoff(final WorldFrameWrapper wfw)
		{
			RefereeMsg refMsg = wfw.getRefereeMsg();
			if (refMsg == null)
			{
				return false;
			}
			if ((refMsg.getCommand() == Command.PREPARE_KICKOFF_BLUE)
					|| (refMsg.getCommand() == Command.PREPARE_KICKOFF_YELLOW))
			{
				return false;
			}
			return true;
		}
		
		
		@Override
		public void run()
		{
			while (active)
			{
				long t0 = System.nanoTime();
				try
				{
					skipFrames();
					updateReplayTime();
					notifyPositionChanged(replayCurTime);
					
					long curT = getCurrentTime();
					
					lastKey = persistance.getKey(curT);
					
					RecordCamFrame camFrame = persistance.getCamFrame(lastKey);
					if (camFrame != null)
					{
						ExtendedCamDetectionFrame eFrame = camFrame.getCamFrame();
						for (IWorldFrameObserver vp : wFrameObservers)
						{
							vp.onNewCamDetectionFrame(eFrame);
						}
					}
					
					RecordFrame frame = persistance.getRecordFrame(lastKey);
					if (frame != null)
					{
						updateTimeStep(frame.getTimestampMs());
						updateLogPresenter(frame.getTimestampMs());
						
						for (IWorldFrameObserver vp : wFrameObservers)
						{
							vp.onNewWorldFrame(frame.getWorldFrameWrapper());
						}
						if (runCurrentAi)
						{
							for (VisualizationFrame vF : frame.getVisFrames())
							{
								Agent agent = agents.get(vF.getTeamColor());
								SimpleWorldFrame swf = vF.getWorldFrameWrapper().getSimpleWorldFrame();
								for (ITrackedBot bot : swf.getBots().values())
								{
									DummyBot dBot = new DummyBot();
									dBot.setAvail2Ai(true);
									((TrackedBot) bot).setBot(dBot);
								}
								
								WorldFrameWrapper wfw = new WorldFrameWrapper(
										vF.getWorldFrameWrapper().getSimpleWorldFrame(),
										vF.getWorldFrameWrapper().getRefereeMsg(), new ShapeMap());
								
								// if (
								// (agent.getLatestAiFrame() == null)
								// || (agent.getLatestAiFrame().getWorldFrame().getTimestamp() != wfw.getSimpleWorldFrame()
								// .getTimestamp())
								// )
								{
									AIInfoFrame aiFrame = agent.processWorldFrame(wfw);
									if (aiFrame != null)
									{
										agent.getSkillSystem().process(wfw);
										VisualizationFrame visFrame = new VisualizationFrame(aiFrame);
										updateField(visFrame);
									}
								}
							}
						} else
						{
							for (VisualizationFrame visFrame : frame.getVisFrames())
							{
								updateField(visFrame);
							}
						}
					}
				} catch (Throwable err)
				{
					log.error("Error in RefreshThread.", err);
				}
				long t1 = System.nanoTime();
				
				long dt = (long) (1_000_000_000L / refreshFps);
				long sleep = (dt - (t1 - t0));
				if (sleep > 0)
				{
					assert sleep < (long) 1e9;
					ThreadUtil.parkNanosSafe(sleep);
				}
			}
		}
		
	}
	
	
	/**
	 * Get the current image and paint it to the field panel
	 */
	private void updateField(final VisualizationFrame visFrame)
	{
		for (IVisualizationFrameObserver vp : visFrameObservers)
		{
			vp.onNewVisualizationFrame(visFrame);
		}
	}
	
	
	/**
	 * 
	 */
	private void updateLogPresenter(final long curTime)
	{
		if (logEventBuffer == null)
		{
			if (persistance != null)
			{
				logEventBuffer = persistance.loadLogEvents();
			} else
			{
				logEventBuffer = new ArrayList<BerkeleyLogEvent>(0);
			}
		}
		
		if (logEventBuffer.isEmpty())
		{
			return;
		}
		
		List<LoggingEvent> logEventsPast = new LinkedList<LoggingEvent>();
		List<LoggingEvent> logEventsFuture = new LinkedList<LoggingEvent>();
		// long offset = logEventBuffer.get(0).getTimeStamp();
		// long timeStamp = offset + (long) (curTime / 1e6);
		long timeStamp = curTime;
		for (BerkeleyLogEvent event : logEventBuffer)
		{
			if ((event.getTimeStamp() >= (timeStamp - LOG_BUFFER_BEFORE))
					&& (event.getTimeStamp() <= (timeStamp + LOG_BUFFER_AFTER)))
			{
				if (logPresenter.checkFilters(event.getLoggingEvent()))
				{
					if ((event.getTimeStamp() >= timeStamp))
					{
						logEventsFuture.add(event.getLoggingEvent());
					} else
					{
						logEventsPast.add(event.getLoggingEvent());
					}
				}
			}
		}
		boolean reprint = lastLogEventsPast.size() != logEventsPast.size();
		for (int i = 0; !reprint && (i < lastLogEventsPast.size()); i++)
		{
			String str1 = lastLogEventsPast.get(i).getRenderedMessage();
			String str2 = logEventsPast.get(i).getRenderedMessage();
			if (!str1.equals(str2))
			{
				reprint = true;
				break;
			}
		}
		reprint = reprint || (lastLogEventsFuture.size() != logEventsFuture.size());
		for (int i = 0; !reprint && (i < lastLogEventsFuture.size()); i++)
		{
			String str1 = lastLogEventsFuture.get(i).getRenderedMessage();
			String str2 = logEventsFuture.get(i).getRenderedMessage();
			if (!str1.equals(str2))
			{
				reprint = true;
				break;
			}
		}
		
		if (reprint)
		{
			logPresenter.clearEventStorage();
			for (LoggingEvent event : logEventsPast)
			{
				logPresenter.append(event);
			}
			logPresenter.appendLine();
			for (LoggingEvent event : logEventsFuture)
			{
				logPresenter.append(event);
			}
		}
		lastLogEventsPast = logEventsPast;
		lastLogEventsFuture = logEventsFuture;
	}
	
	
	/**
	 * get the current TimeStep
	 */
	private void updateTimeStep(final long timestamp)
	{
		Date date = new Date(timestamp);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");
		String txt = sdf.format(date);
		// long remTime = timestamp;
		// long minutes = (long) (remTime / 60e9);
		// remTime -= minutes * 60e9;
		// long seconds = (long) (remTime / 1e9);
		// remTime -= seconds * 1e9;
		// long ms = (long) (remTime / 1e6);
		//
		// String txt = String.format("%3d:%02d,%03d", minutes, seconds, ms);
		
		replayControlPresenter.getReplayPanel().getControlPanel().getTimeStepLabel().setText(txt);
	}
	
	
	@Override
	public void onSearchKickoff(final boolean enable)
	{
		searchKickoff = enable;
	}
	
	
	@Override
	public void onSetSkipStop(final boolean enable)
	{
		skipStoppedGame = enable;
	}
	
	
	@Override
	public void onRunCurrentAi(final boolean selected)
	{
		runCurrentAi = selected;
		
		if (runCurrentAi)
		{
			agents.put(ETeamColor.YELLOW, new Agent(ETeamColor.YELLOW));
			agents.put(ETeamColor.BLUE, new Agent(ETeamColor.BLUE));
			// for (Agent agent : agents.values())
			// {
			// agent.reset();
			// agent.getSkillSystem().emergencyStop();
			// }
		}
	}
	
	
	/**
	 * @param presenter
	 */
	public void setLogPresenter(final LogPresenter presenter)
	{
		logPresenter = presenter;
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
	public void onSnapshot()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
		
		SimpleWorldFrame worldFrame = lastWorldFrame.getSimpleWorldFrame();
		Snapshot snapshot = createSnapshot(worldFrame);
		try
		{
			snapshot.save("data/snapshots/" + sdf.format(new Date()) + ".snap");
		} catch (IOException e)
		{
			log.error("", e);
		}
	}
	
	
	/**
	 * @param worldFrame
	 * @return
	 */
	private static Snapshot createSnapshot(final SimpleWorldFrame worldFrame)
	{
		Map<BotID, SnapObject> snapBots = new HashMap<>();
		for (Entry<BotID, ITrackedBot> entry : worldFrame.getBots())
		{
			ITrackedBot bot = entry.getValue();
			snapBots.put(entry.getKey(), new SnapObject(bot.getPos(), bot.getVel()));
		}
		
		TrackedBall ball = worldFrame.getBall();
		SnapObject snapBall = new SnapObject(ball.getPos(), ball.getVel());
		
		Snapshot snapshot = new Snapshot(snapBots, snapBall);
		return snapshot;
	}
}
