/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.GenericManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.SumatraBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.SumatraCam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee.RefereeHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.SimulationParameters.SimulationObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario.ASimulationScenario;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPFacade;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.FpsCounter;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.ThreadUtil;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.RealTimeClock;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.StaticSimulationClock;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Simulate AI, WP, SKILL, etc in batch
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Simulation extends AModule implements IModuliStateObserver
{
	private static final Logger					log					= Logger.getLogger(Simulation.class.getName());
	
	/** */
	public static final String						MODULE_TYPE			= "Simulation";
	/** */
	public static final String						MODULE_ID			= "sim";
	
	private Agent										agentBlue, agentYellow;
	private WPFacade									wpFacade;
	private SumatraCam								cam;
	private GenericSkillSystem						skillSystem;
	private RefereeHandler							referee;
	
	// private OracleExtKalman kalman;
	private SimulatedScheduledExecutorService	pathPlanningScheduler;
	private boolean									activated			= false;
	private ScheduledExecutorService				scheduler			= null;
	
	private static String							lastModuli			= "";
	private static String							lastBotmanager		= "";
	private static boolean							simulationRunning	= false;
	private FpsCounter								fpsc					= new FpsCounter(new RealTimeClock());
	private ASimulationScenario					scenario				= null;
	@Configurable
	private static float								simFps				= 60;
	
	private boolean									firstFrame			= true;
	
	
	/**
	 * @param config
	 */
	public Simulation(final SubnodeConfiguration config)
	{
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	
	
	/**
	 * Prepare Sumatra for performing simulation
	 * 
	 * @param scenario
	 */
	public void activateSimulation(final ASimulationScenario scenario)
	{
		activated = true;
		this.scenario = scenario;
	}
	
	
	private void setupVision(final SimulationParameters params)
	{
		try
		{
			GenericManager bm = (GenericManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			for (ABot bot : bm.getAllBots().values())
			{
				SumatraBot sBot = (SumatraBot) bot;
				sBot.setNetworkState(ENetworkState.OFFLINE);
				float y = 4000;
				if (sBot.getBotID().getTeamColor() == ETeamColor.YELLOW)
				{
					y *= -1;
				}
				sBot.setPos(new Vector3(-1000 + (300 * sBot.getBotID().getNumber()), y, 0));
			}
			for (Map.Entry<BotID, SimulationObject> entry : params.getInitBots().entrySet())
			{
				SumatraBot bot = (SumatraBot) bm.getAllBots().get(entry.getKey());
				if (bot == null)
				{
					log.warn("Could not find bot with id " + entry.getKey());
				} else
				{
					bot.setPos(entry.getValue().getPos());
					bot.setVel(entry.getValue().getVel());
					bot.setNetworkState(ENetworkState.ONLINE);
				}
			}
			cam.replaceBall(params.getInitBall().getPos().getXYVector(), params.getInitBall().getVel().getXYVector());
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find botmanager module.", err);
		}
	}
	
	
	/**
	 */
	private void start(final boolean newThread)
	{
		scenario.setupSimulation();
		setupVision(scenario.getParams());
		fpsc = new FpsCounter(new RealTimeClock());
		if (!newThread)
		{
			ScheduledRunner runner = new ScheduledRunner();
			runner.run();
		} else
		{
			scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Simulation"));
			if (scenario.getParams().getSpeedFactor() == 0)
			{
				ScheduledRunner runner = new ScheduledRunner();
				scheduler.execute(runner);
			} else
			{
				// SumatraClock.setClock(new SimulationClock(params.getSpeedFactor()));
				long period = (long) ((1000000f / simFps) / scenario.getParams().getSpeedFactor());
				if (period == 0)
				{
					period = 1;
				}
				scheduler.scheduleAtFixedRate(new SingleRunner(), 0, period, TimeUnit.MICROSECONDS);
			}
		}
	}
	
	
	/**
	 * 
	 */
	public void togglePause()
	{
		scenario.togglePause();
	}
	
	
	/**
	 */
	private void stop()
	{
		float relSpeed = scenario.getSimTimeSinceStart() / scenario.getRealTimeSinceStart();
		log.info(String.format("Simulation took %7.3fs and simulated for %7.3fs. Relative speed: %4.1f",
				scenario.getRealTimeSinceStart(), scenario.getSimTimeSinceStart(), relSpeed));
		if (scheduler != null)
		{
			scheduler.shutdown();
		}
		SumatraClock.setClock(new RealTimeClock());
		firstFrame = true;
	}
	
	
	private WorldFrameWrapper getWorldFrameWrapper()
	{
		final CamDetectionFrame frame = cam.createFrame();
		BotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
		for (SumatraBot sBot : cam.getBots())
		{
			TrackedTigerBot tBot = new TrackedTigerBot(sBot.getBotID(), sBot.getPos().getXYVector(), sBot.getVel()
					.getXYVector(), AVector2.ZERO_VECTOR, 150, sBot.getPos().z(), sBot.getVel().z(), 0, 1, sBot,
					sBot.getColor());
			bots.put(sBot.getBotID(), tBot);
		}
		
		TrackedBall trackedBall = new TrackedBall(cam.getBall().getPos(), cam.getBall().getVel(),
				AVector3.ZERO_VECTOR,
				0, true);
		WorldFramePrediction wfp = new FieldPredictor(bots.values(), trackedBall).create();
		
		SimpleWorldFrame simpleFrame = new SimpleWorldFrame(bots, trackedBall, frame.getFrameNumber(), wfp);
		
		// SimpleWorldFrame simpleFrame = kalman.createWorldFrameWrapper(frame);
		simpleFrame.setWfFps(fpsc.getAvgFps());
		WorldFrameWrapper frameWrapper = new WorldFrameWrapper(simpleFrame);
		return frameWrapper;
	}
	
	
	private void simulationStep()
	{
		// vision
		WorldFrameWrapper frameWrapper = getWorldFrameWrapper();
		
		if (firstFrame)
		{
			scenario.start();
			agentBlue.setPreviousAIFrame(scenario.createInitialAiFrame(frameWrapper.getWorldFrame(ETeamColor.BLUE)));
			agentYellow.setPreviousAIFrame(scenario.createInitialAiFrame(frameWrapper.getWorldFrame(ETeamColor.YELLOW)));
			firstFrame = false;
		}
		
		WorldFrame normalWf = frameWrapper.getWorldFrame(ETeamColor.YELLOW);
		if (normalWf.isInverted())
		{
			normalWf = frameWrapper.getWorldFrame(ETeamColor.BLUE);
		}
		List<RefereeCaseMsg> caseMsgs = scenario.processAutoReferee(normalWf, agentBlue.getLatestAiFrame()
				.getNewRefereeMsg());
		
		// AI
		if (agentBlue.isActive())
		{
			scenario.onUpdate(agentBlue.getLatestAiFrame(), caseMsgs);
			agentBlue.processWorldFrame(frameWrapper.getWorldFrame(ETeamColor.BLUE));
		}
		if (agentYellow.isActive())
		{
			scenario.onUpdate(agentYellow.getLatestAiFrame(), caseMsgs);
			agentYellow.processWorldFrame(frameWrapper.getWorldFrame(ETeamColor.YELLOW));
		}
		
		// skill, pathplanning
		skillSystem.getSkillExecutorScheduler().process(frameWrapper);
		skillSystem.getPathFinderScheduler().setWorldframe(frameWrapper);
		pathPlanningScheduler.runAll();
		
		// notify GUI
		wpFacade.notifyNewWorldFrame(frameWrapper);
		
		fpsc.newFrame();
		
		if (agentBlue.isActive() && scenario.checkStopSimulation(agentBlue.getLatestAiFrame()))
		{
			stopSimulation();
		}
		if (agentYellow.isActive() && scenario.checkStopSimulation(agentYellow.getLatestAiFrame()))
		{
			stopSimulation();
		}
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		try
		{
			wpFacade = (WPFacade) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			cam = (SumatraCam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			skillSystem = (GenericSkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
			referee = (RefereeHandler) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			
			if (activated)
			{
				// following modules will not be started
				agentBlue.setStartModule(false);
				agentYellow.setStartModule(false);
				cam.setStartModule(false);
				skillSystem.setStartModule(false);
				wpFacade.setStartModule(false);
				referee.setStartModule(false);
				activated = false;
			}
		} catch (ModuleNotFoundException err)
		{
			log.error("One or more modules could not be found.", err);
		} catch (ClassCastException err)
		{
			log.error("Unexpected module class.", err);
		}
	}
	
	
	@Override
	public void deinitModule()
	{
		agentBlue.setStartModule(true);
		agentYellow.setStartModule(true);
		cam.setStartModule(true);
		skillSystem.setStartModule(true);
		wpFacade.setStartModule(true);
		referee.setStartModule(true);
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		if (simulationRunning)
		{
			// kalman = (OracleExtKalman) wpFacade.getPredictors().get(PredictorKey.Kalman);
			skillSystem.init();
			pathPlanningScheduler = new SimulatedScheduledExecutorService();
			skillSystem.getPathFinderScheduler().setScheduler(pathPlanningScheduler);
			if (scenario.isEnableYellow())
			{
				agentYellow.setActive(true);
			}
			if (scenario.isEnableBlue())
			{
				agentBlue.setActive(true);
			}
		}
	}
	
	
	@Override
	public void stopModule()
	{
		if (simulationRunning)
		{
			skillSystem.deinit();
			SumatraModel.getInstance().setCurrentModuliConfig(lastModuli);
			SumatraModel.getInstance().setUserProperty(ABotManager.KEY_BOTMANAGER_CONFIG, lastBotmanager);
			simulationRunning = false;
		}
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				break;
			default:
				break;
		
		}
	}
	
	
	/**
	 * Run simulation in a new thread
	 * 
	 * @param scenario
	 */
	public static void runSimulation(final ASimulationScenario scenario)
	{
		runSimulation(scenario, false);
	}
	
	
	/**
	 * Run simulation in the callers thread
	 * 
	 * @param scenario
	 */
	public static void runSimulationBlocking(final ASimulationScenario scenario)
	{
		scenario.setStopOnError(true);
		runSimulation(scenario, true);
	}
	
	
	private static void runSimulation(final ASimulationScenario scenario, final boolean blocking)
	{
		stopSimulation();
		log.info("Start scenario " + scenario.getClass().getSimpleName());
		
		lastModuli = SumatraModel.getInstance().getCurrentModuliConfig();
		lastBotmanager = SumatraModel.getInstance().getUserProperty(ABotManager.KEY_BOTMANAGER_CONFIG);
		
		SumatraModel.getInstance().setUserProperty(ABotManager.KEY_BOTMANAGER_CONFIG, "botmanager_sumatra.xml");
		String moduliConfig = "moduli_sumatra.xml";
		SumatraModel.getInstance().setCurrentModuliConfig(moduliConfig);
		SumatraModel.getInstance().loadModulesSafe(moduliConfig);
		Simulation sim;
		try
		{
			sim = (Simulation) SumatraModel.getInstance().getModule(Simulation.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find Simulation module!", err);
			return;
		}
		sim.activateSimulation(scenario);
		simulationRunning = true;
		try
		{
			SumatraModel.getInstance().startModules();
		} catch (InitModuleException | StartModuleException err)
		{
			log.error("An error occurred while loading modules", err);
			return;
		}
		sim.start(!blocking);
	}
	
	
	/**
	 */
	public static void stopSimulation()
	{
		if (!simulationRunning)
		{
			return;
		}
		Simulation sim;
		try
		{
			sim = (Simulation) SumatraModel.getInstance().getModule(Simulation.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find Simulation module!", err);
			return;
		}
		sim.scenario.afterSimulation(sim.agentYellow.getLatestAiFrame());
		sim.stop();
		
		SumatraModel.getInstance().stopModules();
		SumatraModel.getInstance().setCurrentModuliConfig(lastModuli);
		SumatraModel.getInstance().setUserProperty(ABotManager.KEY_BOTMANAGER_CONFIG, lastBotmanager);
		simulationRunning = false;
	}
	
	
	/**
	 * @return the simulationRunning
	 */
	public static final boolean isSimulationRunning()
	{
		return simulationRunning;
	}
	
	private class ScheduledRunner implements Runnable
	{
		SingleRunner	runner	= new SingleRunner();
		
		
		@Override
		public void run()
		{
			while (simulationRunning)
			{
				runner.run();
			}
		}
	}
	
	private class SingleRunner implements Runnable
	{
		private StaticSimulationClock	clock	= new StaticSimulationClock();
		
		private long						dt		= (long) (1e9 / simFps);
		
		
		/**
		 * 
		 */
		public SingleRunner()
		{
			SumatraClock.setClock(clock);
		}
		
		
		@Override
		public void run()
		{
			try
			{
				if (scenario.isPaused())
				{
					ThreadUtil.parkNanosSafe(10_000_000);
					return;
				}
				simulationStep();
				clock.step(dt);
			} catch (Exception err)
			{
				log.error("Exception during simulation", err);
				stopSimulation();
			}
		}
	}
}
