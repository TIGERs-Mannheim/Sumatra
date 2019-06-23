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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.ares.Ares;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.PlayMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.exceptions.LoadConfigException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.fieldraster.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.PlayFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee.RefereeReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.Timer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IAthenaControlHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ITimer;
import edu.dhbw.mannheim.tigers.sumatra.util.collection.ISyncedFIFO;
import edu.dhbw.mannheim.tigers.sumatra.util.collection.SyncedLinkedFIFO;
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
public class Agent extends AAgent implements Runnable, IAthenaControlHandler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final int					QUEUE_LENGTH		= 10;
	
	protected final Logger						log					= Logger.getLogger(getClass());
	

	// Source
	private final SumatraModel					model					= SumatraModel.getInstance();
	private AWorldPredictor						predictor			= null;
	protected ITimer								timer					= null;
	private ACam									cam					= null;
	
	private final ISyncedFIFO<WorldFrame>	freshWorldFrames	= new SyncedLinkedFIFO<WorldFrame>(QUEUE_LENGTH);
	

	// AI
	private Thread									nathan;
	private long									oldTime				= 0;
	
	private AReferee								referee				= null;
	
	/**
	 * Contains all referee-messages sent (Actually, as the Referee-box sends the last messages over and over again,
	 * these messages will only get here if they differ from the one sent before! See
	 * {@link RefereeReceiver#isNewMessage(RefereeMsg)})
	 */
	private final Queue<RefereeMsg>			refereeMsgQueue	= new LinkedList<RefereeMsg>();
	private final Object							sync					= new Object();
	

	private AIInfoFrame							previousAIFrame	= null;
	

	/** {@link Metis} */
	private Metis									metis;
	

	/** {@link Lachesis} */
	private Lachesis								lachesis;
	// private static final int UNINITIALIZED_KEEPER_ID = -1;
	// private int keeperId = UNINITIALIZED_KEEPER_ID;
	
	/** {@link Athena} */
	private Athena									athena;
	
	/** {@link Ares} */
	private Ares									ares;
	/** {@link Sisyphus} */
	private Sisyphus								sisyphus;
	

	private ASkillSystem							skillSystem;
	
	private ABotManager							botManager;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Agent(SubnodeConfiguration subnodeConfiguration)
	{
		
	}
	

	// --------------------------------------------------------------------------
	// --- lifecycle ------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
		resetCountDownLatch();
		
		try
		{
			cam = (ACam) model.getModule(ACam.MODULE_ID);
			// cam.addCamGeometryObserver(AIConfig.getInstance());
			
			predictor = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
			predictor.setWorldFrameConsumer(this);
			
			skillSystem = (ASkillSystem) model.getModule(ASkillSystem.MODULE_ID);
			
			referee = (AReferee) model.getModule(AReferee.MODULE_ID);
			referee.setRefereeMsgConsumer(this);
			
			botManager = (ABotManager) model.getModule(ABotManager.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("Unable to find one or more modules!");
		}
		
		// Load ai configuration from xml-file
		try
		{
			AIConfig.getInstance().loadAIConfig(AAgent.AI_CONFIG_PATH + currentConfig);
		} catch (LoadConfigException err)
		{
			log.error("Unable to load ai configuration: " + err);
			throw new RuntimeException("Unable to load ai Config:" + AAgent.AI_CONFIG_PATH + currentConfig);
		}
		
		// Load tactics configuration from xml-file
		try
		{
			AIConfig.getInstance().loadTacticsConfig(AAgent.TACTICS_CONFIG_PATH + currentTactics);
		} catch (LoadConfigException err)
		{
			log.error("Unable to load tactics configuration: " + err);
			throw new RuntimeException("Unable to load tactics Config:" + AAgent.TACTICS_CONFIG_PATH + currentTactics);
		}
		
		log.info("Initialized.");
	}
	

	@Override
	public void startModule() throws StartModuleException
	{
		try
		{
			timer = (ATimer) model.getModule(Timer.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.debug("No timer found.");
		}
		
		// // KeeperId
		// for (ABot bot : botManager.getAllBots().values())
		// {
		// if (bot.is
		//
		// )
		// }
		

		// AI visualization
		notifyNewFieldRaster();
		
		// Instantiate...
		metis = new Metis();
		lachesis = new Lachesis();
		athena = new Athena(lachesis, botManager);
		
		sisyphus = new Sisyphus(observers);
		ares = new Ares(sisyphus, freshWorldFrames);
		ares.setSkillSystem(skillSystem);
		
		// Check PlayFactory for play-creation-problems
		PlayFactory factory = PlayFactory.getInstance();
		List<EPlay> result = factory.selfCheckPlays();
		if (result.size() > 0)
		{
			String str = "PlayFactory self-check failed for the following EPlays:\n";
			for (EPlay type : result)
			{
				str += "- " + type + "\n";
			}
		}
		log.info("PlayFactory check done!");
		
		// Check the play-map for errors
		try
		{
			PlayMap.getInstance();
		} catch (IllegalArgumentException err)
		{
			log.fatal("Play-map check failed: " + err.getMessage());
		} finally
		{
			log.info("Play-map check done!");
		}
		

		// Run
		nathan = new Thread(this, "AI_Nathan");
		// nathan.setPriority(Thread.MAX_PRIORITY); // Use the force, Luke!
		nathan.start();
		
		// metis.start();
		sisyphus.start();
		

		log.info("Started.");
	}
	

	/**
	 *
	 */
	@Override
	public void run()
	{
		try
		{
			startSignal.await();
		} catch (InterruptedException err)
		{
			log.error("Error while waiting for start-signal!");
			return;
		}
		

		while (!Thread.currentThread().isInterrupted())
		{
			WorldFrame wf;
			try
			{
				wf = freshWorldFrames.take();
				
				// Not '>=', because the real cameras may take pictures at the same time. So the resulting WorldFrames - one
				// with the current situation from the left, one from the right camera - may have the same timestamp but
				// contain different information
				if (oldTime > wf.time)
				{
					continue;
				}
				oldTime = wf.time;
				
				startTime(wf);
				
			} catch (InterruptedException err)
			{
				log.debug("Error while waiting for new world frame, quitting...");
				break;
			}
			
			// Take the first of the referee-messages
			RefereeMsg refereeMsg;
			synchronized (sync)
			{
				refereeMsg = refereeMsgQueue.poll();
			}
			

			// Process!
			AIInfoFrame frame = new AIInfoFrame(wf, refereeMsg);
			

			if (previousAIFrame != null) // Skip first frame
			{
				// Analyze
				frame = metis.process(frame, previousAIFrame);
				
				// Choose and calculate behavior
				frame = athena.process(frame, previousAIFrame);
				
				// Execute!
				ares.process(frame, previousAIFrame);
			}
			

			// AI visualization
			notifyNewAIInfoFrame(frame);
			
			stopTime(frame);
			

			// Cleanup
			refereeMsg = null;
			
			previousAIFrame = frame;
		}
	}
	

	private void startTime(WorldFrame wFrame)
	{
		if (timer != null)
		{
			timer.startAI(wFrame);
		}
	}
	

	private void stopTime(AIInfoFrame aiFrame)
	{
		if (timer != null)
		{
			timer.stopAI(aiFrame);
		}
	}
	

	@Override
	public void stopModule()
	{
		nathan.interrupt();
		
		// metis.stop();
		
		sisyphus.stop();
		
		if (predictor != null)
		{
			predictor.setWorldFrameConsumer(null);
		}
		
		try
		{
			AIConfig.getInstance().saveAIConfig();
		} catch (ConfigurationException err)
		{
			// TODO Auto-generated catch block
			err.printStackTrace();
		}
		
		log.info("Stopped.");
	}
	

	@Override
	public void deinitModule()
	{
		metis = null;
		
		athena = null;
		lachesis = null;
		
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
			// cam.removeCamGeometryObserver(AIConfig.getInstance());
			cam = null;
		}
		
		sisyphus = null;
		
		previousAIFrame = null;
		
		refereeMsgQueue.clear();
		oldTime = 0;
		
		log.info("Deinitialized.");
	}
	

	// --------------------------------------------------------------------------
	// --- observer methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onNewRefereeMsg(RefereeMsg msg)
	{
		synchronized (sync)
		{
			refereeMsgQueue.add(msg);
		}
	}
	

	@Override
	public void onNewWorldFrame(WorldFrame worldFrame)
	{
		freshWorldFrames.put(worldFrame);
	}
	

	@Override
	public void onNewAthenaControl(AthenaControl newControl)
	{
		if (athena != null)
		{
			athena.onNewAthenaControl(newControl);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- AI Visualization -----------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * This function is used to visualize the positioning field raster in sumatra field view.
	 * Thus field raster will only be loaded once at startup this method is private and will
	 * be called with AI-Module start.
	 * 
	 */
	private void notifyNewFieldRaster()
	{
		synchronized (observers)
		{
			for (IAIObserver o : observers)
			{
				int columnSize = (int) FieldRasterGenerator.getInstance().getColumnSize();
				int rowSize = (int) FieldRasterGenerator.getInstance().getRowSize();
				
				int analysingFactor = AIConfig.getFieldRaster().getAnalysingFactor();
				
				o.onNewFieldRaster(columnSize, rowSize, columnSize / analysingFactor, rowSize / analysingFactor);
			}
		}
	}
	

	/**
	 * 
	 * This function is used to notify the last {@link AIInfoFrame} to visualization observers.
	 * @param lastAIInfoframe
	 */
	private void notifyNewAIInfoFrame(AIInfoFrame lastAIInfoframe)
	{
		synchronized (observers)
		{
			for (IAIObserver o : observers)
			{
				o.onNewAIInfoFrame(new AIInfoFrame(lastAIInfoframe));
			}
		}
	}
	
}
