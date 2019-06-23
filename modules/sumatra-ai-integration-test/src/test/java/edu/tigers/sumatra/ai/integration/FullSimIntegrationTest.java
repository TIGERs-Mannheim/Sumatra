/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import static edu.tigers.sumatra.persistence.RecordManager.getDefaultBasePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.autoreferee.engine.IAutoRefEngine;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.AiBerkeleyRecorder;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.data.RefBoxRemoteControlFactory;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.sim.SimulationObject;
import edu.tigers.sumatra.sim.SimulationParameters;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.WorldInfoCollector;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FullSimIntegrationTest
{
	private static final String MODULI_CONFIG = "moduli_integration_test.xml";
	
	private static LogEventWatcher logEventWatcher;
	
	@Rule
	public TestName testName = new TestName();
	
	private AiBerkeleyRecorder recorder;
	
	private boolean testCaseSucceeded;
	
	
	@BeforeClass
	public static void beforeClass() throws Exception
	{
		ConfigRegistration.setDefPath("../../config/");
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesSafe(MODULI_CONFIG);
		SumatraModel.getInstance().setTestMode(true);
		
		SumatraModel.getInstance().startModules();
		SimulationHelper.setSimulateWithMaxSpeed();
		SimulationHelper.stopSimulation();
	}
	
	
	@AfterClass
	public static void afterClass() throws Exception
	{
		SumatraModel.getInstance().stopModules();
		SumatraModel.getInstance().setTestMode(false);
		
		File botParamsDbFile = new File("config/botParamsDatabase.json");
		if (botParamsDbFile.exists())
		{
			botParamsDbFile.deleteOnExit();
		}
	}
	
	
	@Before
	public void before() throws Exception
	{
		logEventWatcher = new LogEventWatcher(Level.WARN, Level.ERROR);
		Logger.getRootLogger().addAppender(logEventWatcher);
		
		recorder = new AiBerkeleyRecorder("../../" + getDefaultBasePath(), testName.getMethodName());
		recorder.start();
		
		AutoRefModule autoRefModule = (AutoRefModule) SumatraModel.getInstance().getModule(AutoRefModule.MODULE_ID);
		autoRefModule.start(IAutoRefEngine.AutoRefMode.ACTIVE);
		
		testCaseSucceeded = false;
	}
	
	
	@After
	public void after() throws Exception
	{
		AutoRefModule autoRefModule = (AutoRefModule) SumatraModel.getInstance().getModule(AutoRefModule.MODULE_ID);
		autoRefModule.stop();
		
		Logger.getRootLogger().removeAppender(logEventWatcher);
		
		recorder.stop();
		recorder.awaitStop();
		if (!testCaseSucceeded)
		{
			recorder.compress();
		}
		recorder.delete();
	}
	
	
	@Test
	public void testForceStart() throws Exception
	{
		startTestByRefereeMsg("snapshots/maintenance.snap", Command.FORCE_START, 15);
	}
	
	
	@Test
	public void testKickoff() throws Exception
	{
		startTestByRefereeMsg("snapshots/maintenance.snap", Command.PREPARE_KICKOFF_BLUE, 25);
	}
	
	
	@Test
	public void testKickoffWithoutFoes() throws Exception
	{
		startTestByRefereeMsg("snapshots/NoFoes.snap", Command.PREPARE_KICKOFF_YELLOW, 25);
	}
	
	
	@Test
	public void forceStateWithoutFoes() throws Exception
	{
		startTestByRefereeMsg("snapshots/NoFoes.snap", Command.FORCE_START, 25);
	}
	
	
	@Test
	public void testIndirectFreekick() throws Exception
	{
		startTestByRefereeMsg("snapshots/maintenance.snap", Command.INDIRECT_FREE_BLUE, 25);
	}
	
	
	@Test
	public void testDirectFreekick() throws Exception
	{
		startTestByRefereeMsg("snapshots/maintenance.snap", Command.DIRECT_FREE_BLUE, 25);
	}
	
	
	@Test
	public void testPenalty() throws Exception
	{
		startTestByRefereeMsg("snapshots/maintenance.snap", Command.PREPARE_PENALTY_BLUE, 25);
	}
	
	
	@Test
	public void testStop() throws Exception
	{
		startTestByRefereeMsg("snapshots/maintenance.snap", Command.STOP, 15);
	}
	
	
	@Test
	public void testBallPlacement() throws Exception
	{
		Snapshot snapshot = Snapshot.loadFromResources("snapshots/maintenance.snap");
		SimulationParameters params = new SimulationParameters(snapshot);
		params.setBallPlacementPos(Vector2.fromXY(2900, -900));
		params.setRefereeCommand(Command.NORMAL_START);
		SimulationHelper.loadSimulation(params);
		SimulationHelper.startSimulation();
		
		SimTimeBlocker simTimeBlocker = new SimTimeBlocker(3);
		simTimeBlocker.await();
		
		AReferee ref = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
		ref.handleControlRequest(
				RefBoxRemoteControlFactory.fromBallPlacement(ETeamColor.BLUE, params.getBallPlacementPos()));
		
		simTimeBlocker = new SimTimeBlocker(15);
		simTimeBlocker.await();
		SimulationHelper.stopSimulation();
		
		assertThat(logEventWatcher.getNumEvents(Level.ERROR)).isZero();
		assertThat(logEventWatcher.getNumEvents(Level.WARN)).isZero();
		assertBotsHaveMoved(params);
		assertNoAvoidableViolations();
		testCaseSucceeded = true;
	}
	
	
	private void startTestByRefereeMsg(String snapshotFile, Command command, double duration) throws Exception
	{
		Snapshot snapshot = Snapshot.loadFromResources(snapshotFile);
		SimulationParameters params = new SimulationParameters(snapshot);
		params.setRefereeCommand(command);
		SimulationHelper.loadSimulation(params);
		SimulationHelper.startSimulation();
		
		SimTimeBlocker simTimeBlocker = new SimTimeBlocker(duration);
		simTimeBlocker.await();
		SimulationHelper.stopSimulation();
		
		assertThat(logEventWatcher.getNumEvents(Level.ERROR)).isZero();
		assertThat(logEventWatcher.getNumEvents(Level.WARN)).isZero();
		assertBotsHaveMoved(params);
		assertNoAvoidableViolations();
		testCaseSucceeded = true;
	}
	
	
	private void assertBotsHaveMoved(final SimulationParameters params) throws ModuleNotFoundException
	{
		WorldInfoCollector wic = (WorldInfoCollector) SumatraModel.getInstance().getModule(WorldInfoCollector.MODULE_ID);
		WorldFrameWrapper wfw = wic.getLastWorldFrameWrapper();
		for (Map.Entry<BotID, SimulationObject> simBotEntry : params.getInitBots().entrySet())
		{
			ITrackedBot tBot = wfw.getSimpleWorldFrame().getBot(simBotEntry.getKey());
			assertThat(tBot.getPos().distanceTo(simBotEntry.getValue().getPos().getXYVector()))
					.withFailMessage("A robot did not move during test").isGreaterThan(1);
		}
	}
	
	
	private void assertNoAvoidableViolations() throws ModuleNotFoundException
	{
		Set<EGameEvent> avoidableViolations = new HashSet<>();
		avoidableViolations.add(EGameEvent.ATTACKER_TO_DEFENCE_AREA);
		avoidableViolations.add(EGameEvent.BOT_COLLISION);
		avoidableViolations.add(EGameEvent.DOUBLE_TOUCH);
		avoidableViolations.add(EGameEvent.DEFENDER_TO_KICK_POINT_DISTANCE);
		avoidableViolations.add(EGameEvent.MULTIPLE_DEFENDER);
		avoidableViolations.add(EGameEvent.KICK_TIMEOUT);
		avoidableViolations.add(EGameEvent.BALL_HOLDING);
		// MULTIPLE_DEFENDER_PARTIALLY -> this is currently often caused by pushing and would need better detection
		// ATTACKER_IN_DEFENSE_AREA -> we do not try to avoid this
		// BALL_SPEEDING -> accidental double kicking between two bots often cause a higher ball speed. Ignore this for now
		// BOT_STOP_SPEED -> unreliable
		AutoRefModule autoRefModule = (AutoRefModule) SumatraModel.getInstance().getModule(AutoRefModule.MODULE_ID);
		List<GameLogEntry> allGameEvents = new ArrayList<>(autoRefModule.getEngine().getGameLog().getEntries());
		List<IGameEvent> avoidableGameEvents = allGameEvents.stream()
				.filter(e -> e.getType() == GameLogEntry.ELogEntryType.GAME_EVENT)
				.map(GameLogEntry::getGameEvent)
				.filter(e -> avoidableViolations.contains(e.getType()))
				.collect(Collectors.toList());
		assertThat(avoidableGameEvents).isEmpty();
	}
	
	private static class SimTimeBlocker implements IWorldFrameObserver
	{
		private final CountDownLatch latch = new CountDownLatch(1);
		private final double duration;
		private Long startTime;
		
		
		SimTimeBlocker(final double duration)
		{
			this.duration = duration;
		}
		
		
		void start()
		{
			try
			{
				WorldInfoCollector wic = (WorldInfoCollector) SumatraModel.getInstance()
						.getModule(WorldInfoCollector.MODULE_ID);
				wic.addObserver(this);
			} catch (ModuleNotFoundException e)
			{
				fail("Could not find module.", e);
			}
		}
		
		
		void stop()
		{
			try
			{
				WorldInfoCollector wic = (WorldInfoCollector) SumatraModel.getInstance()
						.getModule(WorldInfoCollector.MODULE_ID);
				wic.removeObserver(this);
			} catch (ModuleNotFoundException e)
			{
				fail("Could not find module.", e);
			}
		}
		
		
		void await()
		{
			start();
			try
			{
				latch.await(1, TimeUnit.HOURS);
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}
		
		
		@Override
		public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
		{
			long timestamp = wFrameWrapper.getSimpleWorldFrame().getTimestamp();
			if (startTime == null)
			{
				startTime = timestamp;
			}
			if ((timestamp - startTime) / 1e9 > duration)
			{
				latch.countDown();
				stop();
			}
		}
	}
}
