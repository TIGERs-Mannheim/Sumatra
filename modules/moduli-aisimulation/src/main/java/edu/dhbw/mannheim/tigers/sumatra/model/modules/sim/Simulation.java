/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.sim;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.SimulationParameters.SimulationObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.scenario.ASimulationScenario;
import edu.tigers.moduli.AModule;
import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.BotManager;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.clock.StaticSimulationClock;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.RefereeHandler;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.sim.SumatraBot;
import edu.tigers.sumatra.sim.SumatraCam;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.thread.SimulatedScheduledExecutorService;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ShapeMap;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


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
	private AWorldPredictor							wp;
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
	private ASimulationScenario					scenario				= null;
	@Configurable
	private static double							simFps				= 60;
	
	private boolean									firstFrame			= true;
	private final RefereeMsg						latestRefereeMsg	= new RefereeMsg();
	
	
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
			BotManager bm = (BotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			for (IBot bot : bm.getAllBots().values())
			{
				SumatraBot sBot = (SumatraBot) bot;
				double y = 4000;
				if (sBot.getBotId().getTeamColor() == ETeamColor.YELLOW)
				{
					y *= -1;
				}
				sBot.setPos(new Vector3(-1000 + (300 * sBot.getBotId().getNumber()), y, 0));
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
				}
			}
			cam.replaceBall(params.getInitBall().getPos(), params.getInitBall().getVel());
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
				// SumatraClock.setClock(new
				// SimulationClock(params.getSpeedFactor()));
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
		WorldFrameWrapper wfw = getWorldFrameWrapper(latestRefereeMsg);
		long ts = wfw.getSimpleWorldFrame().getTimestamp();
		double relSpeed = scenario.getSimTimeSinceStart(ts) / scenario.getRealTimeSinceStart();
		log.info(String.format("Simulation took %7.3fs and simulated for %7.3fs. Relative speed: %4.1f",
				scenario.getRealTimeSinceStart(), scenario.getSimTimeSinceStart(ts), relSpeed));
		if (scheduler != null)
		{
			scheduler.shutdown();
		}
		firstFrame = true;
	}
	
	
	private WorldFrameWrapper getWorldFrameWrapper(final RefereeMsg refereeMsg)
	{
		BotIDMap<ITrackedBot> bots = new BotIDMap<ITrackedBot>();
		for (SumatraBot sBot : cam.getBots())
		{
			TrackedBot tBot = new TrackedBot(cam.getSimTime(), sBot.getBotId());
			tBot.setPos(sBot.getPos().getXYVector());
			tBot.setVel(sBot.getVel().getXYVector());
			tBot.setAngle(sBot.getPos().z());
			tBot.setaVel(sBot.getVel().z());
			bots.put(sBot.getBotId(), tBot);
		}
		
		TrackedBall trackedBall = new TrackedBall(cam.getBall().getPos(), cam.getBall().getVel(), AVector3.ZERO_VECTOR);
		
		SimpleWorldFrame simpleFrame = new SimpleWorldFrame(bots, trackedBall, cam.getFrameId(), cam.getSimTime());
		WorldFrameWrapper frameWrapper = new WorldFrameWrapper(simpleFrame, refereeMsg, new ShapeMap());
		return frameWrapper;
	}
	
	
	private void simulationStep()
	{
		// vision
		WorldFrameWrapper frameWrapper = getWorldFrameWrapper(latestRefereeMsg);
		
		if (firstFrame)
		{
			scenario.start(frameWrapper.getSimpleWorldFrame().getTimestamp());
			agentBlue.setPreviousAIFrame(scenario.createInitialAiFrame(frameWrapper, ETeamColor.BLUE));
			agentYellow.setPreviousAIFrame(scenario.createInitialAiFrame(frameWrapper, ETeamColor.YELLOW));
			firstFrame = false;
		}
		
		// WorldFrame normalWf = frameWrapper.getWorldFrame(ETeamColor.YELLOW);
		// if (normalWf.isInverted()) {
		// normalWf = frameWrapper.getWorldFrame(ETeamColor.BLUE);
		// }
		List<RefereeCaseMsg> caseMsgs = scenario.processAutoReferee(frameWrapper, latestRefereeMsg);
		
		// AI
		if (agentBlue.isActive())
		{
			scenario.onUpdate(agentBlue.getLatestAiFrame(), caseMsgs);
			if (scenario.getNewRefereeMsg() != null)
			{
				// agentBlue.onNewRefereeMsg(scenario.getNewRefereeMsg());
			}
			agentBlue.processWorldFrame(frameWrapper);
		}
		if (agentYellow.isActive())
		{
			scenario.onUpdate(agentYellow.getLatestAiFrame(), caseMsgs);
			if (scenario.getNewRefereeMsg() != null)
			{
				// agentYellow.onNewRefereeMsg(scenario.getNewRefereeMsg());
			}
			agentYellow.processWorldFrame(frameWrapper);
		}
		
		// skill, pathplanning
		skillSystem.process(frameWrapper);
		skillSystem.getPathFinderScheduler().setWorldframe(frameWrapper);
		pathPlanningScheduler.runAll();
		
		// notify GUI
		wp.pushFrame(frameWrapper.getSimpleWorldFrame());
		
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
			wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			cam = (SumatraCam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			skillSystem = new GenericSkillSystem();
			referee = (RefereeHandler) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			
			if (activated)
			{
				// following modules will not be started
				agentBlue.setStartModule(false);
				agentYellow.setStartModule(false);
				cam.setStartModule(false);
				skillSystem.setStartModule(false);
				wp.setStartModule(false);
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
		wp.setStartModule(true);
		referee.setStartModule(true);
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		if (simulationRunning)
		{
			// kalman = (OracleExtKalman)
			// wpFacade.getPredictors().get(PredictorKey.Kalman);
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
		SingleRunner runner = new SingleRunner();
		
		
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
		private final StaticSimulationClock	clock	= new StaticSimulationClock();
		
		private final long						dt		= (long) (1e9 / simFps);
		
		
		/**
		 * 
		 */
		public SingleRunner()
		{
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
