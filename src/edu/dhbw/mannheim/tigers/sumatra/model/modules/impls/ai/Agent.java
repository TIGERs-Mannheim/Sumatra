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

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.ares.Ares;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.RoleFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.ETimable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.SumatraTimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.Director;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.WorldFrameFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.util.FpsCounter;


/**
 * This is the one-and-only agent implementation, which controls the AI-sub-modules, and sends out our MechWarriors in
 * the endless battle for fame and glory!
 * 
 * @author Gero
 */
public class Agent extends AAgent implements Runnable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger					log					= Logger.getLogger(Agent.class.getName());
	
	private static final int						QUEUE_LENGTH		= 1;
	
	private static final long						WF_TIMEOUT			= 1000;
	
	// Source
	private final SumatraModel						model					= SumatraModel.getInstance();
	private SumatraTimer								timer					= null;
	private ACam										cam					= null;
	
	private final BlockingDeque<WorldFrame>	freshWorldFrames	= new LinkedBlockingDeque<WorldFrame>(QUEUE_LENGTH);
	
	
	// AI
	private Thread										nathan;
	
	private AReferee									referee				= null;
	
	private AIInfoFrame								previousAIFrame	= null;
	private RefereeMsg								latestRefereeMsg	= null;
	
	/** {@link Metis} */
	private Metis										metis;
	
	/** {@link Athena} */
	private final Athena								athena				= new Athena();
	
	/** {@link Ares} */
	private Ares										ares;
	
	private ASkillSystem								skillSystem;
	
	private FpsCounter								fpsCounter			= new FpsCounter();
	
	/** was the agent activated yet? Can only be activated once */
	private boolean									activated			= false;
	private String										activatedKey;
	/** is the agent active atm? Can also be disabled again */
	private boolean									active				= false;
	
	private ETimable									timableAgent;
	private ETimable									timableAthena;
	private ETimable									timableMetis;
	private ETimable									timableAres;
	private ETimable									timableNotify;
	
	
	private long										lastRefMsgCounter	= -1;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param subnodeConfiguration
	 */
	public Agent(final SubnodeConfiguration subnodeConfiguration)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- lifecycle ------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
		if (MODULE_ID_YELLOW.equals(getId()))
		{
			timableAgent = ETimable.AGENT_Y;
			timableAthena = ETimable.ATHENA_Y;
			timableMetis = ETimable.METIS_Y;
			timableAres = ETimable.ARES_Y;
			timableNotify = ETimable.AI_NOTIFY_Y;
			setTeamColor(ETeamColor.YELLOW);
		} else if (MODULE_ID_BLUE.equals(getId()))
		{
			timableAgent = ETimable.AGENT_B;
			timableAthena = ETimable.ATHENA_B;
			timableMetis = ETimable.METIS_B;
			timableAres = ETimable.ARES_B;
			timableNotify = ETimable.AI_NOTIFY_B;
			setTeamColor(ETeamColor.BLUE);
		}
		
		activatedKey = Agent.class.getName() + "-" + getId() + ".activated";
		active = Boolean.valueOf(SumatraModel.getInstance().getUserProperty(activatedKey));
		activated = active;
		try
		{
			cam = (ACam) model.getModule(ACam.MODULE_ID);
			
			AWorldPredictor predictor = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
			predictor.addWorldFrameConsumer(this);
			
			skillSystem = (ASkillSystem) model.getModule(ASkillSystem.MODULE_ID);
			
			referee = (AReferee) model.getModule(AReferee.MODULE_ID);
			referee.addRefereeMsgConsumer(this);
		} catch (final ModuleNotFoundException err)
		{
			log.error("Unable to find one or more modules!");
		}
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		lastRefMsgCounter = -1;
		if (!activated)
		{
			return;
		}
		try
		{
			timer = ((SumatraTimer) model.getModule(ATimer.MODULE_ID));
		} catch (final ModuleNotFoundException err)
		{
			log.debug("No timer found.");
		}
		
		metis = new Metis();
		ares = new Ares(skillSystem);
		
		RoleFactory.selfCheckRoles();
		
		nathan = new Thread(this, "AI_Nathan_" + getId());
		nathan.start();
		log.trace("Nathan started");
	}
	
	
	/**
	 * @param msg The recently received message
	 * @return Whether this message does really new game-state information
	 * @author FriederB
	 */
	private boolean isNewMessage(final RefereeMsg msg)
	{
		if (msg.getCommandCounter() != lastRefMsgCounter)
		{
			lastRefMsgCounter = msg.getCommandCounter();
			return true;
		}
		
		return false;
	}
	
	
	/**
	 *
	 */
	@Override
	public void run()
	{
		long id = 0;
		while (!Thread.currentThread().isInterrupted())
		{
			if (!active)
			{
				onVisionSignalLost(WorldFrameFactory.createEmptyWorldFrame(0L));
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException err)
				{
					// not important
				}
				continue;
			}
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
			
			id = wf.getId().getFrameNumber();
			
			timer.start(timableAgent, id);
			
			// ### Take the first of the referee-messages
			RefereeMsg refereeMsg = latestRefereeMsg;
			
			RefereeMsg newRefereeMsg = null;
			if ((refereeMsg != null) && isNewMessage(refereeMsg))
			{
				newRefereeMsg = refereeMsg;
			}
			
			BaseAiFrame baseAiFrame = new BaseAiFrame(wf, newRefereeMsg, refereeMsg, previousAIFrame, getTeamColor());
			
			if (previousAIFrame == null)
			{
				// Skip first frame
				previousAIFrame = generateAIInfoFrame(baseAiFrame);
				continue;
			}
			
			previousAIFrame.cleanUp();
			
			// ### Process!
			try
			{
				// Analyze
				timer.start(timableMetis, id);
				MetisAiFrame metisAiFrame = metis.process(baseAiFrame);
				timer.stop(timableMetis, id);
				
				// Choose and calculate behavior
				timer.start(timableAthena, id);
				AthenaAiFrame athenaAiFrame = athena.process(metisAiFrame);
				timer.stop(timableAthena, id);
				
				// Execute!
				timer.start(timableAres, id);
				AresData aresData = ares.process(athenaAiFrame);
				timer.start(timableAres, id);
				
				timer.start(timableNotify, id);
				// ### Populate used AIInfoFrame (for visualization etc)
				AIInfoFrame frame = new AIInfoFrame(athenaAiFrame, aresData, fpsCounter.getAvgFps());
				fpsCounter.newFrame();
				
				notifyNewAIInfoFrame(frame);
				previousAIFrame = frame;
				timer.stop(timableNotify, id);
			} catch (final Exception ex)
			{
				// # Notify observers (gui) about errors...
				notifyNewAIException(ex, generateAIInfoFrame(baseAiFrame), previousAIFrame);
				
				// # Undo everything we've done this cycle to restore previous state
				// - RefereeMsg
				if (refereeMsg != null)
				{
					lastRefMsgCounter--;
				}
				
				skillSystem.getSisyphus().stopAllPathPlanning();
				skillSystem.reset(getTeamColor());
				
				// # Prepare next cycle
				timer.stop(timableAgent, id);
				
				previousAIFrame.cleanUp();
				continue;
			}
			
			
			// ### End cycle
			timer.stop(timableAgent, id);
		}
	}
	
	
	private AIInfoFrame generateAIInfoFrame(final BaseAiFrame baseAiFrame)
	{
		return new AIInfoFrame(new AthenaAiFrame(new MetisAiFrame(baseAiFrame, new TacticalField(
				baseAiFrame.getWorldFrame())), new PlayStrategy(new PlayStrategy.Builder())), new AresData(), 0);
	}
	
	
	@Override
	public void stopModule()
	{
		if (!activated)
		{
			return;
		}
		nathan.interrupt();
	}
	
	
	@Override
	public void deinitModule()
	{
		if (ares != null)
		{
			ares = null;
		}
		
		if (referee != null)
		{
			referee = null;
		}
		
		if (cam != null)
		{
			cam = null;
		}
		
		previousAIFrame = null;
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onNewRefereeMsg(final RefereeMsg msg)
	{
		latestRefereeMsg = msg;
	}
	
	
	@Override
	public synchronized void onNewSimpleWorldFrame(final SimpleWorldFrame worldFrame)
	{
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrame wFrame)
	{
		if (wFrame.getTeamColor() == getTeamColor())
		{
			freshWorldFrames.pollLast();
			freshWorldFrames.addFirst(wFrame);
		}
	}
	
	
	@Override
	public void onVisionSignalLost(final SimpleWorldFrame emptyWf)
	{
		WorldFrame wf = Director.createWorldFrame(emptyWf, getTeamColor());
		BaseAiFrame bFrame = new BaseAiFrame(wf, null, latestRefereeMsg, previousAIFrame, getTeamColor());
		// ### Populate used AIInfoFrame (for visualization etc)
		notifyNewAIInfoFrame(generateAIInfoFrame(bFrame));
	}
	
	
	@Override
	public void onStop()
	{
		if (!activated)
		{
			return;
		}
		nathan.interrupt();
	}
	
	
	// --------------------------------------------------------------------------
	// --- AI Visualization -----------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * This function is used to notify the last {@link AIInfoFrame} to visualization observers.
	 * 
	 * @param lastAIInfoframe
	 */
	private void notifyNewAIInfoFrame(final AIInfoFrame lastAIInfoframe)
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
	
	
	/**
	 */
	private void setActivated()
	{
		if (!activated && (SumatraModel.getInstance().getModulesState().get() == ModulesState.ACTIVE))
		{
			try
			{
				activated = true;
				startModule();
			} catch (StartModuleException err)
			{
				log.error("Could not start Agent " + getId());
			}
		}
		activated = true;
	}
	
	
	/**
	 * @return the active
	 */
	public final boolean isActive()
	{
		return active;
	}
	
	
	/**
	 * @param active the active to set
	 */
	public final void setActive(final boolean active)
	{
		setActivated();
		this.active = active;
		SumatraModel.getInstance().setUserProperty(activatedKey, String.valueOf(active));
	}
	
	
	/**
	 * @return the metis
	 */
	public final Metis getMetis()
	{
		return metis;
	}
	
	
	/**
	 * @return the ares
	 */
	public final Ares getAres()
	{
		return ares;
	}
	
	
	/**
	 * @return the athena
	 */
	public final Athena getAthena()
	{
		return athena;
	}
}
