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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.MultiTeamMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.ares.Ares;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.RoleFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.DummyTimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.ETimable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.ITimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.SumatraTimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AMultiTeamMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordManager;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.util.AugmentedDataSender;
import edu.dhbw.mannheim.tigers.sumatra.util.FpsCounter;
import edu.dhbw.mannheim.tigers.sumatra.util.MultiTeamMessageSender;
import edu.dhbw.mannheim.tigers.sumatra.util.config.UserConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * This is the one-and-only agent implementation, which controls the AI-sub-modules, and sends out our MechWarriors in
 * the endless battle for fame and glory!
 * 
 * @author Gero
 */
public class Agent extends AAgent implements Runnable
{
	private static final Logger					log							= Logger.getLogger(Agent.class.getName());
	
	private static final int						QUEUE_LENGTH				= 1;
	
	private static final long						WF_TIMEOUT					= 1000;
	
	// Source
	private final SumatraModel						model							= SumatraModel.getInstance();
	private ITimer										timer							= new DummyTimer();
	private ACam										cam							= null;
	
	private final BlockingDeque<WorldFrame>	freshWorldFrames			= new LinkedBlockingDeque<WorldFrame>(
																									QUEUE_LENGTH);
	private List<IDrawableShape>					shapeInjectionsQueue		= new LinkedList<IDrawableShape>();
	private List<IDrawableShape>					shapeInjectionsQueue2	= new LinkedList<IDrawableShape>();
	private final Object								shapeSync					= new Object();
	// AI
	private Thread										nathan;
	
	private AReferee									referee						= null;
	
	private AIInfoFrame								previousAIFrame			= null;
	private RefereeMsg								latestRefereeMsg			= null;
	
	/** {@link Metis} */
	private Metis										metis;
	
	/** {@link Athena} */
	private final Athena								athena						= new Athena();
	
	/** {@link Ares} */
	private Ares										ares;
	
	private ASkillSystem								skillSystem;
	
	private FpsCounter								fpsCounter					= new FpsCounter();
	
	/** was the agent activated yet? Can only be activated once */
	private boolean									activated					= false;
	private String										activatedKey;
	/** is the agent active atm? Can also be disabled again */
	private boolean									active						= false;
	
	private ETimable									timableAgent;
	private ETimable									timableMetis;
	private ETimable									timableNotify;
	
	
	private long										lastRefMsgCounter			= -1;
	private AugmentedDataSender					augmentedDataSender		= null;
	
	private AMultiTeamMessage						multiTeamMessage			= null;
	private MultiTeamMessage						multiTeamMsg				= null;
	private MultiTeamMessageSender				multiTeamMessageSender	= null;
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public Agent(final SubnodeConfiguration subnodeConfiguration)
	{
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		if (MODULE_ID_YELLOW.equals(getId()))
		{
			timableAgent = ETimable.AGENT_Y;
			timableMetis = ETimable.METIS_Y;
			timableNotify = ETimable.AI_NOTIFY_Y;
			setTeamColor(ETeamColor.YELLOW);
		} else if (MODULE_ID_BLUE.equals(getId()))
		{
			timableAgent = ETimable.AGENT_B;
			timableMetis = ETimable.METIS_B;
			timableNotify = ETimable.AI_NOTIFY_B;
			setTeamColor(ETeamColor.BLUE);
		}
		
		activatedKey = Agent.class.getName() + "-" + getId() + ".activated";
		setActive(Boolean.valueOf(SumatraModel.getInstance().getUserProperty(activatedKey)));
		fpsCounter = new FpsCounter();
		
		try
		{
			cam = (ACam) model.getModule(ACam.MODULE_ID);
			
			AWorldPredictor predictor = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
			predictor.addWorldFrameConsumer(this);
			
			skillSystem = (ASkillSystem) model.getModule(ASkillSystem.MODULE_ID);
			
			referee = (AReferee) model.getModule(AReferee.MODULE_ID);
			referee.addRefereeMsgConsumer(this);
			
			multiTeamMessage = (AMultiTeamMessage) model.getModule(AMultiTeamMessage.MODULE_ID);
			multiTeamMessage.addMultiTeamMessageConsumer(this);
			multiTeamMessageSender = new MultiTeamMessageSender();
			
			metis = new Metis();
			ares = new Ares(skillSystem);
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
	 * Process a {@link WorldFrame} (one AI cycle)
	 * 
	 * @param wf
	 */
	public void processWorldFrame(final WorldFrame wf)
	{
		long id = wf.getId();
		
		timer.start(timableAgent, id);
		
		// ### Take the first of the referee-messages
		RefereeMsg refereeMsg = latestRefereeMsg;
		
		RefereeMsg newRefereeMsg = null;
		if ((refereeMsg != null) && isNewMessage(refereeMsg))
		{
			log.trace("Referee cmd: " + refereeMsg.getCommand());
			newRefereeMsg = refereeMsg;
		}
		
		if (SumatraModel.getInstance().isProductive() && (newRefereeMsg != null))
		{
			switch (newRefereeMsg.getStage())
			{
				case NORMAL_FIRST_HALF_PRE:
				case NORMAL_SECOND_HALF_PRE:
				case EXTRA_FIRST_HALF_PRE:
				case EXTRA_SECOND_HALF_PRE:
					if ((newRefereeMsg.getCommand() == Command.HALT))
					{
						break;
					}
				case NORMAL_FIRST_HALF:
				case NORMAL_SECOND_HALF:
				case EXTRA_FIRST_HALF:
				case EXTRA_SECOND_HALF:
				case PENALTY_SHOOTOUT:
					if (!RecordManager.isRecording())
					{
						RecordManager.startStopRecording(true, true);
					}
					break;
				case EXTRA_HALF_TIME:
				case NORMAL_HALF_TIME:
				case PENALTY_SHOOTOUT_BREAK:
				case POST_GAME:
				case EXTRA_TIME_BREAK:
					if (RecordManager.isRecording())
					{
						RecordManager.startStopRecording(false, true);
					}
					break;
			}
		}
		BaseAiFrame baseAiFrame = new BaseAiFrame(wf, newRefereeMsg, refereeMsg, previousAIFrame, getTeamColor());
		
		if (previousAIFrame == null)
		{
			// Skip first frame
			previousAIFrame = generateAIInfoFrame(baseAiFrame);
			return;
		}
		
		previousAIFrame.cleanUp();
		
		// ### Process!
		try
		{
			// Analyze
			timer.start(timableMetis, id);
			MetisAiFrame metisAiFrame = metis.process(baseAiFrame);
			timer.stop(timableMetis, id);
			
			// Set multi-team message
			if (multiTeamMsg != null)
			{
				metisAiFrame.getTacticalField().setMultiTeamMessage(multiTeamMsg);
			}
			
			// Choose and calculate behavior
			AthenaAiFrame athenaAiFrame = athena.process(metisAiFrame);
			
			if (baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState() == EAIControlState.MIXED_TEAM_MODE)
			{
				multiTeamMessageSender.send(athenaAiFrame);
			}
			
			synchronized (shapeSync)
			{
				List<IDrawableShape> shapes = new ArrayList<IDrawableShape>(shapeInjectionsQueue2);
				shapes.addAll(shapeInjectionsQueue);
				shapeInjectionsQueue2 = shapeInjectionsQueue;
				shapeInjectionsQueue = new LinkedList<IDrawableShape>();
				for (IDrawableShape shape : shapes)
				{
					athenaAiFrame.getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.INJECT).add(shape);
				}
			}
			
			// Execute!
			AresData aresData = ares.process(athenaAiFrame);
			
			timer.start(timableNotify, id);
			// ### Populate used AIInfoFrame (for visualization etc)
			AIInfoFrame frame = new AIInfoFrame(athenaAiFrame, aresData, fpsCounter.getAvgFps());
			fpsCounter.newFrame();
			
			notifyNewAIInfoFrame(frame);
			
			if (UserConfig.isAugmentedDataSenderEnabled())
			{
				if (augmentedDataSender == null)
				{
					augmentedDataSender = new AugmentedDataSender();
					augmentedDataSender.start();
				}
				augmentedDataSender.addFrame(frame);
			} else if (augmentedDataSender != null)
			{
				augmentedDataSender.stop();
				augmentedDataSender = null;
			}
			
			previousAIFrame = frame;
			notifyNewAIInfoFrameVisualize(previousAIFrame);
			timer.stop(timableNotify, id);
		} catch (final Throwable ex)
		{
			// # Notify observers (gui) about errors...
			if (previousAIFrame != null)
			{
				notifyNewAIExceptionVisualize(ex, generateAIInfoFrame(baseAiFrame), previousAIFrame);
			}
			
			// # Undo everything we've done this cycle to restore previous state
			// - RefereeMsg
			if (refereeMsg != null)
			{
				lastRefMsgCounter--;
			}
			
			skillSystem.reset(getTeamColor());
			
			// # Prepare next cycle
			timer.stop(timableAgent, id);
			
			if (previousAIFrame != null)
			{
				previousAIFrame.cleanUp();
			}
			return;
		}
		
		
		// ### End cycle
		timer.stop(timableAgent, id);
	}
	
	
	/**
	 *
	 */
	@Override
	public void run()
	{
		while (!Thread.currentThread().isInterrupted())
		{
			if (!active)
			{
				if (previousAIFrame != null)
				{
					notifyAIStopped(getTeamColor());
				}
				previousAIFrame = null;
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException err)
				{
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
			
			processWorldFrame(wf);
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
		if (augmentedDataSender != null)
		{
			augmentedDataSender.stop();
			augmentedDataSender = null;
		}
		nathan.interrupt();
	}
	
	
	@Override
	public void deinitModule()
	{
		metis.stop();
		active = false;
		
		if (ares != null)
		{
			ares = null;
		}
		
		if (referee != null)
		{
			referee = null;
		}
		
		if (multiTeamMessage != null)
		{
			multiTeamMessage = null;
		}
		
		if (cam != null)
		{
			cam = null;
		}
		
		previousAIFrame = null;
		latestRefereeMsg = null;
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onNewRefereeMsg(final RefereeMsg msg)
	{
		if ((msg != null) && msg.getColor().equals(getTeamColor()))
		{
			latestRefereeMsg = msg;
		}
	}
	
	
	@Override
	public void onNewMultiTeamMessage(final MultiTeamMessage message)
	{
		if (message != null)
		{
			if (multiTeamMsg == null)
			{
				log.info("Received first multi team message");
			}
			multiTeamMsg = message;
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		freshWorldFrames.pollLast();
		freshWorldFrames.addFirst(wfWrapper.getWorldFrame(getTeamColor()));
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
	 * Inject shape from anywhere into next ai frame
	 * 
	 * @param shape
	 */
	public void injectDrawableShape(final IDrawableShape shape)
	{
		synchronized (shapeSync)
		{
			shapeInjectionsQueue.add(shape);
		}
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
	
	
	/**
	 * @return
	 */
	public final AIInfoFrame getLatestAiFrame()
	{
		return previousAIFrame;
	}
	
	
	/**
	 */
	public void visualizeLatestAiFrame()
	{
		notifyNewAIInfoFrameVisualize(previousAIFrame);
	}
	
	
	/**
	 * @param previousAIFrame the previousAIFrame to set
	 */
	public final void setPreviousAIFrame(final AIInfoFrame previousAIFrame)
	{
		this.previousAIFrame = previousAIFrame;
	}
}
