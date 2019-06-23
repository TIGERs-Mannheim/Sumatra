/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.10.2010
 * Author(s):
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.FrameID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.ares.Ares;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.ApollonControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.PlayFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.RoleFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IManualBotObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IAthenaControlHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ITimer;
import edu.dhbw.mannheim.tigers.sumatra.util.FpsCounter;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.IStatisticsObserver;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.exceptions.StartModuleException;


/**
 * This is the one-and-only agent implementation, which controls the AI-sub-modules, and sends out our MechWarriors in
 * the endless battle for fame and glory!
 * 
 * @author Gero
 * 
 */
public class Agent extends AAgent implements Runnable, IAthenaControlHandler, IManualBotObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger					log						= Logger.getLogger(Agent.class.getName());
	
	private static final int						QUEUE_LENGTH			= 1;
	
	private static final long						WF_TIMEOUT				= 1000;
	
	// Source
	private final SumatraModel						model						= SumatraModel.getInstance();
	private AWorldPredictor							predictor				= null;
	private static ITimer							timer						= null;
	private ACam										cam						= null;
	
	private final BlockingDeque<WorldFrame>	freshWorldFrames		= new LinkedBlockingDeque<WorldFrame>(QUEUE_LENGTH);
	
	
	// AI
	private Thread										nathan;
	
	private AReferee									referee					= null;
	
	/**
	 * Contains all referee-messages sent (Actually, as the Referee-box sends the last messages over and over again,
	 * these messages will only get here if they differ from the one sent before! See
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee.RefereeReceiver#isNewMessage(RefereeMsg)})
	 */
	private final Deque<RefereeMsg>				refereeMsgQueue		= new LinkedList<RefereeMsg>();
	private final Object								sync						= new Object();
	
	
	private AIInfoFrame								previousAIFrame		= null;
	private RefereeMsg								previousRefereeMsg	= null;
	/** Whether there was a reset before or not */
	private boolean									resetFlag				= false;
	
	
	/** {@link Metis} */
	private Metis										metis;
	
	/** {@link Athena} */
	private Athena										athena;
	
	/** {@link Ares} */
	private Ares										ares;
	
	private ASkillSystem								skillSystem;
	
	private FpsCounter								fpsCounter				= new FpsCounter();
	
	private Sisyphus									sisyphus;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param subnodeConfiguration
	 */
	public Agent(SubnodeConfiguration subnodeConfiguration)
	{
		AConfigManager.registerConfigClient(TeamConfig.getInstance());
		AConfigManager.registerConfigClient(AIConfig.getInstance().getGeomClient());
		AConfigManager.registerConfigClient(AIConfig.getInstance().getBotClient());
		AConfigManager.registerConfigClient(AIConfig.getInstance().getAiClient());
	}
	
	
	// --------------------------------------------------------------------------
	// --- lifecycle ------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
		try
		{
			cam = (ACam) model.getModule(ACam.MODULE_ID);
			
			predictor = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
			predictor.setWorldFrameConsumer(this);
			
			skillSystem = (ASkillSystem) model.getModule(ASkillSystem.MODULE_ID);
			
			referee = (AReferee) model.getModule(AReferee.MODULE_ID);
			referee.setRefereeMsgConsumer(this);
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find one or more modules!");
		}
		
		log.debug("Initialized.");
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		try
		{
			Agent.setTimer((ATimer) model.getModule(ATimer.MODULE_ID));
		} catch (final ModuleNotFoundException err)
		{
			log.debug("No timer found.");
		}
		
		// Instantiate...
		metis = new Metis();
		athena = new Athena();
		
		sisyphus = new Sisyphus(getObservers());
		ares = new Ares(sisyphus, skillSystem);
		
		// Check PlayFactory for play-creation-problems
		final PlayFactory factory = PlayFactory.getInstance();
		final List<EPlay> result = factory.selfCheckPlays();
		if (result.size() > 0)
		{
			final StringBuilder str = new StringBuilder("PlayFactory self-check failed for the following EPlays:\n");
			for (final EPlay type : result)
			{
				str.append("- ").append(type).append("\n");
			}
			log.warn(str);
		}
		log.debug("PlayFactory check done!");
		
		RoleFactory.selfCheckRoles();
		
		// Run
		nathan = new Thread(this, "AI_Nathan");
		// nathan.setPriority(Thread.MAX_PRIORITY); // Use the force, Luke!
		nathan.start();
		
		
		log.debug("Started.");
	}
	
	
	/**
	 * 
	 * @param name
	 * @param id
	 */
	public static void startTimer(String name, FrameID id)
	{
		if (Agent.getTimer() != null)
		{
			Agent.getTimer().start(name, id);
		}
	}
	
	
	/**
	 * 
	 * @param name
	 * @param id
	 */
	public static void stopTime(String name, FrameID id)
	{
		if (Agent.getTimer() != null)
		{
			Agent.getTimer().stop(name, id);
		}
	}
	
	
	/**
	 *
	 */
	@Override
	public void run()
	{
		FrameID id = new FrameID(0, 0);
		while (!Thread.currentThread().isInterrupted())
		{
			// ### Get latest worldframe
			WorldFrame wf;
			try
			{
				wf = freshWorldFrames.pollLast(WF_TIMEOUT, TimeUnit.MILLISECONDS);
				
				if ((wf == null))
				{
					continue;
				}
			} catch (final InterruptedException err)
			{
				// No error here...
				break;
			}
			
			startTimer("Agent", id);
			
			// ### Take the first of the referee-messages
			RefereeMsg refereeMsg = null;
			synchronized (sync)
			{
				refereeMsg = refereeMsgQueue.poll();
			}
			
			
			AIInfoFrame frame = new AIInfoFrame(wf, refereeMsg, previousRefereeMsg);
			frame.setFps(fpsCounter.getAvgFps());
			fpsCounter.newFrame();
			
			previousRefereeMsg = frame.refereeMsgCached;
			if (previousAIFrame == null)
			{
				// Skip first frame
				previousAIFrame = frame;
				continue;
			}
			
			
			// ### Process!
			try
			{
				// Check for reset-flag
				if (resetFlag)
				{
					frame.playStrategy.setForceNewDecision();
					resetFlag = false;
				}
				
				// Analyze
				startTimer("Metis", id);
				metis.process(frame, previousAIFrame);
				stopTime("Metis", id);
				
				// Choose and calculate behavior
				startTimer("Athena", id);
				athena.process(frame, previousAIFrame);
				stopTime("Athena", id);
				
				// Execute!
				startTimer("Ares", id);
				ares.process(frame, previousAIFrame);
				stopTime("Ares", id);
				
				// ### Populate used AIInfoFrame (for visualization etc)
				notifyNewAIInfoFrame(frame);
				
			} catch (final Exception ex)
			{
				// # Notify observers (gui) about errors...
				notifyNewAIException(ex, frame, previousAIFrame);
				
				// # Undo everything we've done this cycle to restore previous state
				// - RefereeMsg
				if (refereeMsg != null)
				{
					synchronized (sync)
					{
						refereeMsgQueue.addFirst(refereeMsg);
					}
				}
				
				// # Prepare next cycle
				stopTime("Agent", id);
				
				previousAIFrame = null;
				resetFlag = true;
				continue;
			}
			
			
			// ### End cycle
			stopTime("Agent", id);
			previousAIFrame = frame;
			Agent.getTimer().notifyNewTimerInfo(id);
			id = wf.id;
		}
	}
	
	
	@Override
	public void stopModule()
	{
		nathan.interrupt();
		athena.onStop();
		
		if (predictor != null)
		{
			predictor.setWorldFrameConsumer(null);
		}
		
		log.debug("Stopped.");
	}
	
	
	@Override
	public void deinitModule()
	{
		metis = null;
		
		athena = null;
		
		if (ares != null)
		{
			ares.setSkillSystem(null);
			ares = null;
		}
		
		if (referee != null)
		{
			referee.setRefereeMsgConsumer(null);
			referee = null;
		}
		
		if (cam != null)
		{
			cam = null;
		}
		
		previousAIFrame = null;
		
		refereeMsgQueue.clear();
		
		log.debug("Deinitialized.");
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onNewRefereeMsg(RefereeMsg msg)
	{
		synchronized (sync)
		{
			refereeMsgQueue.addLast(msg);
		}
	}
	
	
	@Override
	public synchronized void onNewWorldFrame(WorldFrame worldFrame)
	{
		freshWorldFrames.pollLast();
		freshWorldFrames.addFirst(worldFrame);
	}
	
	
	@Override
	public void onVisionSignalLost(WorldFrame emptyWf)
	{
		AIInfoFrame frame = new AIInfoFrame(emptyWf, previousRefereeMsg, previousRefereeMsg);
		// ### Populate used AIInfoFrame (for visualization etc)
		notifyNewAIInfoFrame(frame);
	}
	
	
	@Override
	public void onNewAthenaControl(AthenaControl newControl)
	{
		if (athena != null)
		{
			athena.onNewAthenaControl(newControl);
		}
	}
	
	
	@Override
	public void onNewApollonControl(ApollonControl newControl)
	{
		athena.onNewApollonControl(newControl);
	}
	
	
	@Override
	public void onSaveKnowledgeBase()
	{
		athena.onSaveKnowledgeBase();
	}
	
	
	@Override
	public void onStop()
	{
		nathan.interrupt();
	}
	
	
	// --------------------------------------------------------------------------
	// --- AI Visualization -----------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * This function is used to notify the last {@link AIInfoFrame} to visualization observers.
	 * @param lastAIInfoframe
	 */
	private void notifyNewAIInfoFrame(AIInfoFrame lastAIInfoframe)
	{
		synchronized (getObservers())
		{
			final AIInfoFrame newAIInfoFrame = new AIInfoFrame(lastAIInfoframe);
			for (final IAIObserver o : getObservers())
			{
				o.onNewAIInfoFrame(newAIInfoFrame);
			}
		}
	}
	
	
	private void notifyNewAIException(final Exception ex, final AIInfoFrame frame, final AIInfoFrame prevFrame)
	{
		synchronized (getObservers())
		{
			AIInfoFrame consFrame = frame;
			if (consFrame != null)
			{
				consFrame = new AIInfoFrame(consFrame);
			}
			AIInfoFrame consPrevFrame = prevFrame;
			if (prevFrame != null)
			{
				consPrevFrame = new AIInfoFrame(consPrevFrame);
			}
			
			for (final IAIObserver o : getObservers())
			{
				o.onAIException(ex, consFrame, consPrevFrame);
			}
		}
	}
	
	
	@Override
	public void onManualBotAdded(BotID bot)
	{
		metis.onManualBotAdded(bot);
	}
	
	
	@Override
	public void onManualBotRemoved(BotID bot)
	{
		metis.onManualBotRemoved(bot);
	}
	
	
	/**
	 * @return the timer
	 */
	protected static synchronized ITimer getTimer()
	{
		return timer;
	}
	
	
	/**
	 * @param timer the timer to set
	 */
	protected static synchronized void setTimer(ITimer timer)
	{
		Agent.timer = timer;
	}
	
	
	@Override
	public void setActiveCalculators(List<ECalculator> calculators)
	{
		if (metis != null)
		{
			for (ECalculator calc : ECalculator.values())
			{
				if (calculators.contains(calc))
				{
					metis.setCalculatorActive(calc, true);
				} else
				{
					metis.setCalculatorActive(calc, false);
				}
			}
		}
	}
	
	
	/**
	 * @param o
	 */
	public void addPlayStatisticsObserver(IStatisticsObserver o)
	{
		if (athena != null)
		{
			athena.addPlayStatisticsObserver(o);
		}
	}
	
	
	/**
	 * @param o
	 */
	public void removePlayStatisticsObserver(IStatisticsObserver o)
	{
		if (athena != null)
		{
			athena.removePlayStatisticsObserver(o);
		}
	}
	
	
	/**
	 * @return the sisyphus
	 */
	public Sisyphus getSisyphus()
	{
		return sisyphus;
	}
	
	
}
