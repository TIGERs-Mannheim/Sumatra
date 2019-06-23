/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.replay.ReplayControlPresenter;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.IReplayControlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.IReplayPositionObserver;
import edu.tigers.autoref.presenter.GameLogPresenter;
import edu.tigers.autoreferee.AutoRefFramePreprocessor;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.PassiveAutoRefEngine;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.Ai;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.MultiTeamMessage;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.aicenter.AICenterPresenter;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.persistence.ABerkeleyPersistence;
import edu.tigers.sumatra.persistence.AiBerkeleyPersistence;
import edu.tigers.sumatra.persistence.BerkeleyLogEvent;
import edu.tigers.sumatra.persistence.RecordCamFrame;
import edu.tigers.sumatra.persistence.RecordFrame;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
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
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Presenter for Replay GUI
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayPresenter extends AMainPresenter implements IReplayControlPanelObserver
{
	private static final Logger log = Logger.getLogger(ReplayPresenter.class
			.getName());
	
	private static final String LAST_LAYOUT_FILENAME = "last_replay.ly";
	/** */
	private static final String LAYOUT_DEFAULT = "default_replay.ly";
	protected static final String KEY_LAYOUT_PROP = ReplayPresenter.class.getName()
			+ ".layout";
	
	private static final long LOG_BUFFER_BEFORE = 500;
	private static final long LOG_BUFFER_AFTER = 500;
	
	private final List<IReplayPositionObserver> positionObservers = new CopyOnWriteArrayList<>();
	
	private AiBerkeleyPersistence persistence = null;
	private List<BerkeleyLogEvent> logEventBuffer = null;
	
	
	private final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor(
					new NamedThreadFactory(
							"Replay"));
	
	private static final double REFRESH_FPS = 30;
	private double speed = 1;
	private boolean skipStoppedGame = false;
	private boolean searchKickoff = false;
	private boolean runCurrentAi = false;
	private boolean runAutoRef = false;
	private ASkillSystem skillSystem = new GenericSkillSystem();
	private final Map<EAiTeam, Ai> ais = new EnumMap<>(EAiTeam.class);
	
	private final AutoRefFramePreprocessor refPreprocessor = new AutoRefFramePreprocessor();
	private final PassiveAutoRefEngine autoRefEngine = new PassiveAutoRefEngine();
	private IAutoRefFrame lastAutoRefFrame = null;
	
	private List<LoggingEvent> lastLogEventsPast = new LinkedList<>();
	private List<LoggingEvent> lastLogEventsFuture = new LinkedList<>();
	
	private final List<IVisualizationFrameObserver> visFrameObservers = new CopyOnWriteArrayList<>();
	private final List<IWorldFrameObserver> wFrameObservers = new CopyOnWriteArrayList<>();
	private final List<IAutoRefStateObserver> refObservers = new CopyOnWriteArrayList<>();
	
	private WorldFrameWrapper lastWorldFrame;
	
	private LogPresenter logPresenter;
	private ReplayControlPresenter replayControlPresenter;
	private VisualizerPresenter visualizerPresenter;
	private AICenterPresenter aiCenterPresenter;
	
	private RefreshThread refreshThread;
	
	
	/**
	 * Default
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
			if (view.getPresenter() instanceof IAutoRefStateObserver)
			{
				IAutoRefStateObserver observer = (IAutoRefStateObserver) view.getPresenter();
				refObservers.add(observer);
			}
			if (view.getType() == ESumatraViewType.AUTOREFEREE_GAME_LOG)
			{
				GameLogPresenter gameLogPresenter = (GameLogPresenter) view.getPresenter();
				gameLogPresenter.setGameLog(autoRefEngine.getGameLog());
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
				visualizerPresenter.getPanel().removeRobotsPanel();
			}
			if (view.getType() == ESumatraViewType.AI_CENTER)
			{
				aiCenterPresenter = (AICenterPresenter) view.getPresenter();
				aiCenterPresenter.setActive(true);
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
	
	
	/**
	 * Activate this window by setting it visible and start refresh thread
	 *
	 * @param persistance
	 */
	public void start(final ABerkeleyPersistence persistance)
	{
		start(persistance, 0);
	}
	
	
	/**
	 * Activate this window by setting it visible and start refresh thread
	 * 
	 * @param persistance
	 * @param startTime start time within recording
	 */
	public void start(final ABerkeleyPersistence persistance, long startTime)
	{
		createAi(EAiTeam.BLUE_PRIMARY);
		createAi(EAiTeam.YELLOW_PRIMARY);
		
		this.persistence = (AiBerkeleyPersistence) persistance;
		refreshThread = new RefreshThread(startTime);
		visualizerPresenter.start();
		executor.execute(refreshThread);
	}
	
	
	private void createAi(EAiTeam aiTeam)
	{
		Ai preAi = ais.put(aiTeam, new Ai(aiTeam, skillSystem));
		if (preAi != null)
		{
			preAi.stop();
		}
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
		if (persistence != null)
		{
			persistence.close();
		}
		
		ais.clear();
	}
	
	
	/**
	 * @param o
	 */
	private void addPositionObserver(final IReplayPositionObserver o)
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
		 * 
		 */
		public RefreshThread()
		{
			recStartTime = persistence.getFirstKey();
		}
		
		
		/**
		 * @param recCurTime the start time in this log file
		 */
		public RefreshThread(long recCurTime)
		{
			this.recStartTime = persistence.getFirstKey();
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
			long newRecEndTime = persistence.getLastKey();
			if (newRecEndTime != recEndTime)
			{
				replayControlPresenter.getReplayPanel().getControlPanel()
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
			Long key = persistence.getNextKey(lastKey);
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
			Long key = persistence.getPreviousKey(lastKey);
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
				if (skipStoppedGame && skipFrameStoppedGame(persistence.getRecordFrame(t).getWorldFrameWrapper()))
				{
					continue;
				}
				if (searchKickoff && skipFrameKickoff(persistence.getRecordFrame(t).getWorldFrameWrapper()))
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
			return refMsg != null && wfw.getGameState().isStoppedGame();
			
		}
		
		
		private boolean skipFrameKickoff(final WorldFrameWrapper wfw)
		{
			RefereeMsg refMsg = wfw.getRefereeMsg();
			return (refMsg != null)
					&& !((refMsg.getCommand() == Command.PREPARE_KICKOFF_BLUE)
							|| (refMsg.getCommand() == Command.PREPARE_KICKOFF_YELLOW));
		}
		
		
		/**
		 * get the current TimeStep
		 */
		private void updateTimeStep(final long timestamp)
		{
			Date date = new Date(timestamp);
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");
			String txt = sdf.format(date);
			replayControlPresenter.getReplayPanel().getControlPanel().getTimeStepLabel().setText(txt);
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
						recStartTime = persistence.getFirstKey();
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
			
			lastKey = persistence.getKey(curT);
			
			updateCamFrame();
			updateRecordFrame();
		}
		
		
		private void updateRecordFrame()
		{
			RecordFrame frame = persistence.getRecordFrame(lastKey);
			if (frame == null)
			{
				return;
			}
			updateTimeStep(frame.getTimestampMs());
			updateLogPresenter(frame.getTimestampMs());
			
			for (IWorldFrameObserver vp : wFrameObservers)
			{
				vp.onNewWorldFrame(frame.getWorldFrameWrapper());
			}
			if (runAutoRef)
			{
				processAutoReferee(frame);
			}
			if (runCurrentAi)
			{
				processCurrentAi(frame);
			} else
			{
				for (VisualizationFrame visFrame : frame.getVisFrames())
				{
					updateField(visFrame);
				}
			}
		}
		
		
		private void processCurrentAi(final RecordFrame frame)
		{
			for (VisualizationFrame vF : frame.getVisFrames())
			{
				EAiTeam eAiTeam = EAiTeam.primary(vF.getTeamColor());
				Ai ai = ais.get(eAiTeam);
				WorldFrameWrapper wfw = new WorldFrameWrapper(
						vF.getWorldFrameWrapper().getSimpleWorldFrame(),
						vF.getWorldFrameWrapper().getRefereeMsg(),
						vF.getWorldFrameWrapper().getGameState());
				
				AIInfoFrame aiFrame = ai.processWorldFrame(wfw, MultiTeamMessage.DEFAULT);
				if (aiFrame != null)
				{
					skillSystem.process(wfw);
					VisualizationFrame visFrame = new VisualizationFrame(aiFrame);
					updateField(visFrame);
				}
			}
		}
		
		
		private void processAutoReferee(final RecordFrame frame)
		{
			IAutoRefFrame refFrame;
			/*
			 * We only run the ref engine if the current frame is not equal to the last frame. Otherwise we
			 * simply repaint the last frame.
			 */
			if ((lastAutoRefFrame != null) && (lastAutoRefFrame.getTimestamp() == frame.getTimestamp()))
			{
				refFrame = lastAutoRefFrame;
			} else
			{
				WorldFrameWrapper wFrame = frame.getWorldFrameWrapper();
				boolean hasLastFrame = refPreprocessor.hasLastFrame();
				refFrame = refPreprocessor.process(wFrame);
				
				if (hasLastFrame)
				{
					autoRefEngine.process(refFrame);
				}
			}
			
			updateRefObserver(refFrame);
			lastAutoRefFrame = refFrame;
		}
		
		
		private void updateCamFrame()
		{
			RecordCamFrame camFrame = persistence.getCamFrame(lastKey);
			if (camFrame != null)
			{
				ExtendedCamDetectionFrame eFrame = camFrame.getCamFrame();
				for (IWorldFrameObserver vp : wFrameObservers)
				{
					vp.onNewCamDetectionFrame(eFrame);
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
			aiCenterPresenter.update(visFrame);
		}
		
		
		private void updateRefObserver(final IAutoRefFrame frame)
		{
			for (IAutoRefStateObserver observer : refObservers)
			{
				observer.onNewAutoRefFrame(frame);
			}
		}
		
		
		private void updateLogPresenter(final long curTime)
		{
			if (logEventBuffer == null)
			{
				if (persistence != null)
				{
					logEventBuffer = persistence.loadLogEvents();
				} else
				{
					logEventBuffer = new ArrayList<>(0);
				}
			}
			
			if (logEventBuffer.isEmpty())
			{
				return;
			}
			
			List<LoggingEvent> logEventsPast = new LinkedList<>();
			List<LoggingEvent> logEventsFuture = new LinkedList<>();
			long timeStamp = curTime;
			for (BerkeleyLogEvent event : logEventBuffer)
			{
				if ((event.getTimeStamp() >= (timeStamp - LOG_BUFFER_BEFORE))
						&& (event.getTimeStamp() <= (timeStamp + LOG_BUFFER_AFTER))
						&& logPresenter.checkFilters(event.getLoggingEvent()))
				{
					if (event.getTimeStamp() >= timeStamp)
					{
						logEventsFuture.add(event.getLoggingEvent());
					} else
					{
						logEventsPast.add(event.getLoggingEvent());
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
			createAi(EAiTeam.BLUE_PRIMARY);
			createAi(EAiTeam.YELLOW_PRIMARY);
		}
	}
	
	
	@Override
	public void onRunAutoRef(final boolean selected)
	{
		runAutoRef = selected;
		
		if (runAutoRef)
		{
			refPreprocessor.clear();
			autoRefEngine.reset();
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		
		SimpleWorldFrame worldFrame = lastWorldFrame.getSimpleWorldFrame();
		Snapshot snapshot = createSnapshot(worldFrame);
		String defaultFilename = "data/snapshots/" + sdf.format(new Date()) + ".snap";
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("data/snapshots"));
		fileChooser.setSelectedFile(new File(defaultFilename));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("snapshot files", "snap");
		fileChooser.setFileFilter(filter);
		if (fileChooser.showSaveDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			// save to file
			try
			{
				snapshot.save(file.getAbsolutePath());
			} catch (IOException e)
			{
				log.error("Could not save snapshot file", e);
			}
		}
	}
	
	
	@Override
	public void onCopySnapshot()
	{
		if (lastWorldFrame == null)
		{
			return;
		}
		
		SimpleWorldFrame worldFrame = lastWorldFrame.getSimpleWorldFrame();
		Snapshot snapshot = createSnapshot(worldFrame);
		String snapJson = snapshot.toJSON().toJSONString();
		
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(snapJson);
		clipboard.setContents(stringSelection, null);
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
			snapBots.put(entry.getKey(),
					new SnapObject(Vector3.from2d(bot.getPos(), bot.getOrientation()),
							Vector3.from2d(bot.getVel(), bot.getAngularVel())));
		}
		
		ITrackedBall ball = worldFrame.getBall();
		SnapObject snapBall = new SnapObject(ball.getPos3(), ball.getVel3());
		
		return new Snapshot(snapBots, snapBall);
	}
}
