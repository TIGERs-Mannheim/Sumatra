/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordWfFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.RecordWfFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.RedirectPosGPUCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.BerkeleyLogEvent;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.IRecordPersistence;
import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.replay.ReplayControlPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.VisualizerPresenter;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.IReplayControlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.IReplayPositionObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.replay.ReplayWindow;
import edu.dhbw.mannheim.tigers.sumatra.views.ASumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ASumatraView.EViewMode;
import edu.dhbw.mannheim.tigers.sumatra.views.ESumatraViewType;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.AICenterView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.BotOverviewView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.LogView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.OffensiveStrategyView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.ReplayControlView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.StatisticsView;
import edu.dhbw.mannheim.tigers.sumatra.views.impl.VisualizerView;


/**
 * Presenter for Replay GUI
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayPresenter extends AMainPresenter implements IReplayControlPanelObserver
{
	private static final Logger						log						= Logger.getLogger(ReplayPresenter.class
																									.getName());
	
	private static final String						LAST_LAYOUT_FILENAME	= "last_replay.ly";
	/** */
	private static final String						LAYOUT_DEFAULT			= "default_replay.ly";
	protected static final String						KEY_LAYOUT_PROP		= ReplayPresenter.class.getName() + ".layout";
	private final ReplayWindow							replayWindow;
	
	private static final int							BUFFER_SIZE				= 2000;
	/** fetch new frames, if only FETCH_THRESHOLD frames left */
	private static final int							FETCH_THRESHOLD		= 500;
	
	private static final long							LOG_BUFFER_BEFORE		= 500;
	private static final long							LOG_BUFFER_AFTER		= 500;
	
	private final List<IReplayPositionObserver>	positionObservers		= new CopyOnWriteArrayList<IReplayPositionObserver>();
	
	private IRecordPersistence							persistance				= null;
	private List<IRecordFrame>							aiFrameBuffer			= new ArrayList<IRecordFrame>(BUFFER_SIZE);
	private List<BerkeleyLogEvent>					logEventBuffer			= null;
	
	private int												position					= 0;
	/** fps */
	private int												speed						= 0;
	private int												speedCtr					= 0;
	private int												lastSpeed				= 0;
	private boolean										playing					= true;
	private int												maxFrames;
	
	private final Object									frameBufferLock		= new Object();
	private final Fetcher								fetcher					= new Fetcher();
	private volatile int									currentStart			= 0;
	private boolean										firstUpdate				= true;
	
	private final ScheduledExecutorService			executor					= Executors
																									.newScheduledThreadPool(2,
																											new NamedThreadFactory(
																													"Replay"));
	
	private long											timePositionLock		= SumatraClock.nanoTime();
	
	private final long									sleepTime;
	
	private final RedirectPosGPUCalc					redGpuCalcYellow		= new RedirectPosGPUCalc();
	private final RedirectPosGPUCalc					redGpuCalcBlue			= new RedirectPosGPUCalc();
	private final SimplifiedAgent						agentYellow				= new SimplifiedAgent();
	private final SimplifiedAgent						agentBlue				= new SimplifiedAgent();
	
	private boolean										skipStoppedGame		= false;
	private boolean										searchKickoff			= false;
	private boolean										runCurrentAi			= false;
	private boolean										forceRefresh			= false;
	
	private List<LoggingEvent>							lastLogEventsPast		= new LinkedList<LoggingEvent>();
	private List<LoggingEvent>							lastLogEventsFuture	= new LinkedList<LoggingEvent>();
	
	private final List<ISumatraViewPresenter>		viewPresenters			= new CopyOnWriteArrayList<>();
	private LogPresenter									logPresenter;
	private ReplayControlPresenter					replayControlPresenter;
	private VisualizerPresenter						visualizerPresenter;
	
	
	/**
	 */
	public ReplayPresenter()
	{
		replayWindow = new ReplayWindow();
		replayWindow.addView(new AICenterView(ETeamColor.YELLOW));
		replayWindow.addView(new AICenterView(ETeamColor.BLUE));
		replayWindow.addView(new LogView(false));
		replayWindow.addView(new VisualizerView());
		replayWindow.addView(new BotOverviewView());
		replayWindow.addView(new StatisticsView(ETeamColor.YELLOW));
		replayWindow.addView(new StatisticsView(ETeamColor.BLUE));
		replayWindow.addView(new OffensiveStrategyView());
		replayWindow.addView(new ReplayControlView());
		
		for (ASumatraView view : replayWindow.getViews())
		{
			view.setMode(EViewMode.REPLAY);
			view.ensureInitialized();
			addObserver(view.getPresenter());
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
		
		init(replayWindow);
		
		replayControlPresenter.getReplayPanel().getControlPanel().addObserver(this);
		addPositionObserver(replayControlPresenter.getReplayPanel().getControlPanel());
		sleepTime = 16000000;
		replayWindow.activate();
	}
	
	
	/**
	 * @param buffer
	 * @param logEvents
	 */
	public void load(final List<IRecordFrame> buffer, final List<BerkeleyLogEvent> logEvents)
	{
		load(buffer, buffer.size());
		logEventBuffer = logEvents;
	}
	
	
	/**
	 * @param persistance
	 */
	public void load(final IRecordPersistence persistance)
	{
		load(persistance.load(0, BUFFER_SIZE), persistance.size());
		this.persistance = persistance;
	}
	
	
	/**
	 * @param buffer
	 * @param maxFrames
	 */
	public void load(final List<IRecordFrame> buffer, final int maxFrames)
	{
		if ((buffer.isEmpty()))
		{
			throw new IllegalArgumentException("aiFrameBuffer must have at least one Frame");
		}
		this.maxFrames = maxFrames;
		replayControlPresenter.getReplayPanel().getControlPanel().setNumFrames(maxFrames);
		aiFrameBuffer = buffer;
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ISumatraViewPresenter observer)
	{
		viewPresenters.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISumatraViewPresenter observer)
	{
		viewPresenters.remove(observer);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Activate this window by setting it visible and start refresh thread
	 */
	public void start()
	{
		visualizerPresenter.start();
		executor.scheduleAtFixedRate(new RefreshThread(), 100, (sleepTime), TimeUnit.NANOSECONDS);
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
		visualizerPresenter.stop();
		playing = false;
		if (executor.isShutdown())
		{
			log.warn("Tried to close controller multiple times.");
			return;
		}
		executor.shutdown();
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
		synchronized (frameBufferLock)
		{
			for (IRecordFrame rf : aiFrameBuffer)
			{
				rf.cleanUp();
			}
			aiFrameBuffer.clear();
		}
		redGpuCalcYellow.deinit();
		redGpuCalcBlue.deinit();
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
		private int	lastPos	= 0;
		
		
		@Override
		public void run()
		{
			try
			{
				synchronized (frameBufferLock)
				{
					int pos = position;
					int prePos = lastPos;
					if (playing)
					{
						if (speed < 0)
						{
							speedCtr--;
							if (speedCtr <= 0)
							{
								pos++;
								speedCtr = -speed + 1;
							}
						} else if (speed >= 0)
						{
							pos += speed + 1;
						}
						
						if ((pos < 0) || (pos >= (maxFrames - 1)))
						{
							pos = 0;
						}
					}
					int loopCounter = 0;
					while ((pos != lastPos))
					{
						loopCounter++;
						if (loopCounter >= maxFrames)
						{
							break;
						}
						if (persistance != null)
						{
							fetcher.newPosition(pos);
						}
						if (skipStoppedGame)
						{
							IRecordFrame rf = getCurrentRecordFrame(pos);
							if ((rf.getLatestRefereeMsg() != null)
									&&
									((rf.getLatestRefereeMsg().getCommand() == Command.STOP) || (rf.getLatestRefereeMsg()
											.getCommand() == Command.HALT)))
							{
								pos = (pos + 1) % maxFrames;
								for (IReplayPositionObserver o : positionObservers)
								{
									o.onPositionChanged(pos);
								}
								continue;
							}
							skipStoppedGame = false;
						}
						if (searchKickoff)
						{
							IRecordFrame rf = getCurrentRecordFrame(pos);
							if ((rf.getLatestRefereeMsg() != null)
									&&
									((rf.getLatestRefereeMsg().getCommand() != Command.PREPARE_KICKOFF_BLUE) && (rf
											.getLatestRefereeMsg()
											.getCommand() != Command.PREPARE_KICKOFF_YELLOW)))
							{
								pos = (pos + 1) % maxFrames;
								for (IReplayPositionObserver o : positionObservers)
								{
									o.onPositionChanged(pos);
								}
								continue;
							}
							searchKickoff = false;
						}
						lastPos = pos;
					}
					if ((prePos != pos) || forceRefresh)
					{
						update(pos);
						for (IReplayPositionObserver o : positionObservers)
						{
							o.onPositionChanged(pos);
						}
						if (!runCurrentAi)
						{
							forceRefresh = false;
						}
					}
					if (TimeUnit.NANOSECONDS.toMillis(SumatraClock.nanoTime() - timePositionLock) > 500)
					{
						position = pos;
					}
				}
			} catch (Throwable err)
			{
				log.error("Error in RefreshThread.", err);
			}
		}
	}
	
	
	private void update(final int pos)
	{
		updateField(pos);
		updateTimeStep(pos);
		updateLogPresenter(pos);
		
		if (firstUpdate)
		{
			firstUpdate = false;
		}
	}
	
	
	/**
	 * Get the current image and paint it to the field panel
	 */
	private void updateField(final int position)
	{
		IRecordWfFrame wf = getCurrentRecordFrame(position).getWorldFrame();
		IBotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
		for (TrackedTigerBot bot : wf.getTigerBotsVisible().values())
		{
			bots.put(bot.getId(), bot);
		}
		for (TrackedTigerBot bot : wf.getFoeBots().values())
		{
			bots.put(bot.getId(), bot);
		}
		WorldFramePrediction wfp = new FieldPredictor(bots.values(), wf.getBall()).create();
		if (wf.getClass().equals(RecordWfFrame.class))
		{
			((RecordWfFrame) wf).setWorldFramePrediction(wfp);
		}
		SimpleWorldFrame swf = new SimpleWorldFrame(wf);
		if (wf.isInverted())
		{
			swf = swf.mirrorNew();
		}
		WorldFrameWrapper wfw = new WorldFrameWrapper(swf);
		
		final IRecordFrame recFrame;
		if (runCurrentAi)
		{
			if (wf.getTeamColor() == ETeamColor.YELLOW)
			{
				recFrame = agentYellow.generateFrame(getCurrentRecordFrame(position), wfw);
			} else
			{
				recFrame = agentBlue.generateFrame(getCurrentRecordFrame(position), wfw);
			}
		} else
		{
			recFrame = getCurrentRecordFrame(position);
			TacticalField tf = (TacticalField) recFrame.getTacticalField();
			if (recFrame.getTeamColor() == ETeamColor.YELLOW)
			{
				redGpuCalcYellow.doCalc(tf, new BaseAiFrame(new WorldFrame(recFrame.getWorldFrame()), null, null,
						null, recFrame.getTeamColor()));
			} else
			{
				redGpuCalcBlue.doCalc(tf, new BaseAiFrame(new WorldFrame(recFrame.getWorldFrame()), null, null,
						null, recFrame.getTeamColor()));
			}
		}
		
		for (ISumatraViewPresenter vp : viewPresenters)
		{
			vp.onNewWorldFrame(wfw);
			vp.onNewAIInfoFrame(recFrame);
			vp.onNewRefereeMsg(recFrame.getLatestRefereeMsg());
		}
	}
	
	
	/**
	 * 
	 */
	private void updateLogPresenter(final int position)
	{
		Date systime = getCurrentRecordFrame(position).getWorldFrame().getSystemTime();
		long timeStamp = systime.getTime();
		
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
		
		List<LoggingEvent> logEventsPast = new LinkedList<LoggingEvent>();
		List<LoggingEvent> logEventsFuture = new LinkedList<LoggingEvent>();
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
	
	
	private IRecordFrame getCurrentRecordFrame(final int position)
	{
		int pos = position - currentStart;
		if ((pos < 0) || (pos >= aiFrameBuffer.size()))
		{
			log.error("invalid position-currentStart combination. " + position + " " + currentStart + " " + pos + " "
					+ aiFrameBuffer.size());
		}
		return aiFrameBuffer.get(position - currentStart);
	}
	
	
	/**
	 * get the current TimeStep
	 */
	private void updateTimeStep(final int position)
	{
		SimpleDateFormat timeF = new SimpleDateFormat("HH:mm:ss,SSS");
		Date systime = getCurrentRecordFrame(position).getWorldFrame().getSystemTime();
		String timestep = timeF.format(systime);
		replayControlPresenter.getReplayPanel().getControlPanel().getTimeStepLabel().setText(timestep);
	}
	
	
	@Override
	public void onSlower()
	{
		speed--;
	}
	
	
	@Override
	public void onFaster()
	{
		speed++;
	}
	
	
	@Override
	public void onPlayStateChanged(final boolean playing)
	{
		this.playing = playing;
	}
	
	
	@Override
	public void onPositionChanged(final int position)
	{
		if ((position >= 0) && (position < maxFrames))
		{
			this.position = position;
			timePositionLock = SumatraClock.nanoTime();
		}
	}
	
	
	@Override
	public void onNextFrame()
	{
		onChangeRelPos(1);
	}
	
	
	@Override
	public void onSearchKickoff()
	{
		searchKickoff = !searchKickoff;
	}
	
	private class Fetcher implements Runnable
	{
		private boolean	fetching	= false;
		private int			pos		= 0;
		
		
		@Override
		public void run()
		{
			fetch(pos);
		}
		
		
		public void fetch(final int pos)
		{
			int start = pos - (BUFFER_SIZE / 2);
			if (start <= 0)
			{
				start = 0;
			}
			if (start != currentStart)
			{
				List<IRecordFrame> frames = persistance.load(start, BUFFER_SIZE);
				log.debug("New start: " + start);
				synchronized (frameBufferLock)
				{
					currentStart = start;
					aiFrameBuffer = frames;
				}
			}
			fetching = false;
		}
		
		
		/**
		 * @param position
		 */
		public void newPosition(final int position)
		{
			pos = position;
			if (((position - currentStart) >= aiFrameBuffer.size()) || ((position - currentStart) < 0))
			{
				// position is out of range, fetch and block
				fetch(position);
			}
			
			if (!fetching)
			{
				int upper = (currentStart + aiFrameBuffer.size()) - FETCH_THRESHOLD;
				int lower = currentStart + FETCH_THRESHOLD;
				if (lower < (aiFrameBuffer.size() / 2))
				{
					lower = 0;
				}
				if (((position > upper) || (position < lower)))
				{
					startFetch();
				}
			}
		}
		
		
		private void startFetch()
		{
			// reload in background
			fetching = true;
			executor.execute(fetcher);
		}
	}
	
	
	@Override
	public void onSetSpeed(final int speed)
	{
		this.speed = speed;
	}
	
	
	/**
	 * @param enable
	 */
	@Override
	public void setFrameByFrame(final boolean enable)
	{
		if (enable)
		{
			if (speed != 1)
			{
				lastSpeed = speed;
			}
			speed = 1;
		}
		else
		{
			speed = lastSpeed;
		}
	}
	
	
	@Override
	public void onChangeRelPos(final int relPos)
	{
		onPositionChanged(position + relPos);
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
		forceRefresh = true;
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
	
	private static class SimplifiedAgent
	{
		private final Metis	metis		= new Metis();
		private final Athena	athena	= new Athena();
		
		
		private IRecordFrame generateFrame(final IRecordFrame recFrame, final WorldFrameWrapper wfw)
		{
			BaseAiFrame newFrame = new BaseAiFrame(wfw.getWorldFrame(recFrame.getTeamColor()), null,
					recFrame.getLatestRefereeMsg(), recFrame, recFrame.getTeamColor());
			MetisAiFrame metisFrame = metis.process(newFrame);
			AthenaAiFrame athenaFrame = athena.process(metisFrame);
			AresData aresData = new AresData();
			AIInfoFrame aiFrame = new AIInfoFrame(athenaFrame, aresData, 0);
			
			// for (ARole role : aiFrame.getPlayStrategy().getActiveRoles().values())
			// {
			// ISkill skill;
			// BotID botId = role.getBotID();
			// if (!botId.isBot())
			// {
			// continue;
			// }
			// if (role.getNewSkill() != null)
			// {
			// skill = role.getNewSkill();
			// } else if (role.getCurrentSkill() != null)
			// {
			// skill = role.getCurrentSkill();
			// } else
			// {
			// continue;
			// }
			// if (skill.getBot() == null)
			// {
			// skill.setBot(new DummyBot(botId));
			// }
			// skill.setMinDt(0);
			// skill.update(wfw.getWorldFrame(role.getBotID().getTeamColor()));
			// skill.calcActions(new ArrayList<>());
			// // if (role.getNewSkill() != null)
			// {
			// skill.calcEntryActions(new ArrayList<>());
			// }
			//
			// aresData.getPaths().put(botId, skill.getDrawablePath());
			// aresData.getLatestPaths().put(botId, skill.getLatestDrawablePath());
			// aresData.getNumPaths().put(botId, skill.getNewPathCounter());
			// aresData.getSkills().put(botId, skill.getSkillName().name());
			// }
			
			// for (Map.Entry<BotID, String> entry : recFrame.getAresData().getSkills().entrySet())
			// {
			// BotID botId = entry.getKey();
			// String skillName = entry.getValue();
			// ESkillName eSkill = ESkillName.valueOf(skillName);
			// if (eSkill != ESkillName.KICK)
			// {
			// continue;
			// }
			// ASkill aSkill;
			// try
			// {
			// aSkill = (ASkill) eSkill.getInstanceableClass().newDefaultInstance();
			// aSkill.setBot(new DummyBot(botId));
			// aSkill.setMinDt(0);
			// aSkill.update(wfw.getWorldFrame(botId.getTeamColor()));
			// aSkill.calcEntryActions(new ArrayList<>());
			// aSkill.calcActions(new ArrayList<>());
			//
			// aresData.getPaths().put(botId, aSkill.getDrawablePath());
			// aresData.getLatestPaths().put(botId, aSkill.getLatestDrawablePath());
			// aresData.getNumPaths().put(botId, aSkill.getNewPathCounter());
			// aresData.getSkills().put(botId, aSkill.getSkillName().name());
			// } catch (NotCreateableException err)
			// {
			// log.error("Could not create skill: " + eSkill, err);
			// }
			// }
			
			return aiFrame;
		}
	}
}
