/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.AiBerkeleyRecorder;
import edu.tigers.sumatra.ai.BerkeleyAiFrame;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.persistence.BerkeleyAccessor;
import edu.tigers.sumatra.persistence.BerkeleyAsyncRecorder;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.log.BerkeleyLogEvent;
import edu.tigers.sumatra.persistence.log.BerkeleyLogRecorder;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.data.RefBoxRemoteControlFactory;
import edu.tigers.sumatra.sim.SimTimeBlocker;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.sim.SimulationObject;
import edu.tigers.sumatra.sim.SimulationParameters;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.ShapeMapBerkeleyRecorder;
import edu.tigers.sumatra.wp.WfwBerkeleyRecorder;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FullSimIntegrationTest implements IWorldFrameObserver
{
	private static final String MODULI_CONFIG = "moduli_integration_test.xml";
	
	private static LogEventWatcher logEventWatcher;
	
	@Rule
	public TestName testName = new TestName();
	
	private BerkeleyAsyncRecorder recorder;
	
	private boolean testCaseSucceeded;
	
	private WorldFrameWrapper lastWorldFrameWrapper = null;
	
	
	@BeforeClass
	public static void beforeClass() throws Exception
	{
		ConfigRegistration.setDefPath("../../config/");
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesSafe(MODULI_CONFIG);
		SumatraModel.getInstance().setTestMode(true);
		
		SumatraModel.getInstance().startModules();
		SimulationHelper.setSimulateWithMaxSpeed(true);
		SimulationHelper.stopSimulation();
	}
	
	
	@AfterClass
	public static void afterClass()
	{
		SumatraModel.getInstance().stopModules();
		SumatraModel.getInstance().setTestMode(false);
		SimulationHelper.setSimulateWithMaxSpeed(false);
		
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
		
		BerkeleyDb db = BerkeleyDb.withCustomLocation(Paths.get("../../" + BerkeleyDb.getDefaultBasePath(),
				BerkeleyDb.getDefaultName() + "_" + testName.getMethodName()));
		db.add(BerkeleyAiFrame.class, new BerkeleyAccessor<>(BerkeleyAiFrame.class, true));
		db.add(BerkeleyLogEvent.class, new BerkeleyAccessor<>(BerkeleyLogEvent.class, false));
		db.add(BerkeleyShapeMapFrame.class, new BerkeleyAccessor<>(BerkeleyShapeMapFrame.class, true));
		db.add(WorldFrameWrapper.class, new BerkeleyAccessor<>(WorldFrameWrapper.class, true));
		
		recorder = new BerkeleyAsyncRecorder(db);
		recorder.add(new AiBerkeleyRecorder(db));
		recorder.add(new BerkeleyLogRecorder(db));
		recorder.add(new WfwBerkeleyRecorder(db));
		recorder.add(new ShapeMapBerkeleyRecorder(db));
		recorder.start();
		
		AutoRefModule autoRefModule = SumatraModel.getInstance().getModule(AutoRefModule.class);
		autoRefModule.start(IAutoRefEngine.AutoRefMode.ACTIVE);
		
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.MATCH_MODE);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.MATCH_MODE);
		
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
		
		testCaseSucceeded = false;
	}
	
	
	@After
	public void after() throws Exception
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
		AutoRefModule autoRefModule = SumatraModel.getInstance().getModule(AutoRefModule.class);
		autoRefModule.stop();
		SimulationHelper.stopSimulation();
		
		Logger.getRootLogger().removeAppender(logEventWatcher);
		
		recorder.stop();
		recorder.awaitStop();
		if (!testCaseSucceeded)
		{
			recorder.getDb().compress();
		}
		recorder.getDb().delete();
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
		startTestByRefereeMsg("snapshots/NoFoes.snap", Command.PREPARE_KICKOFF_YELLOW, 15);
	}
	
	
	@Test
	public void forceStartWithoutFoes() throws Exception
	{
		startTestByRefereeMsg("snapshots/NoFoes.snap", Command.FORCE_START, 15);
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
		System.out.println("testBallPlacement");
		Snapshot snapshot = Snapshot.loadFromResources("snapshots/maintenance.snap");
		SimulationParameters params = new SimulationParameters(snapshot);
		params.setBallPlacementPos(Vector2.fromXY(2900, -900));
		params.setRefereeCommand(Command.NORMAL_START);
		SimulationHelper.loadSimulation(params);
		SimulationHelper.startSimulation();
		
		SimTimeBlocker simTimeBlocker = new SimTimeBlocker(3);
		simTimeBlocker.await();
		
		AReferee ref = SumatraModel.getInstance().getModule(AReferee.class);
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
	
	
	@Test
	public void testComplicatedPathPlanning() throws Exception
	{
		System.out.println("testComplicatedPathPlanning");
		Snapshot snapshot = Snapshot.loadFromResources("snapshots/pathplanningSituationPenArea.snap");
		SimulationParameters params = new SimulationParameters(snapshot);
		params.setRefereeCommand(Command.HALT);
		SimulationHelper.loadSimulation(params);
		SimulationHelper.startSimulation();
		
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAIControlState.OFF);
		
		SimTimeBlocker simTimeBlocker = new SimTimeBlocker(0.1);
		simTimeBlocker.await();
		
		BotID botId = BotID.createBotId(0, ETeamColor.YELLOW);
		IVector2 destination = Vector2.fromXY(-5900, -1400);
		AMoveSkill skill = AMoveToSkill.createMoveToSkill();
		MovementCon moveCon = skill.getMoveCon();
		moveCon.setPenaltyAreaAllowedOur(false);
		moveCon.setPenaltyAreaAllowedTheir(false);
		moveCon.setIgnoreGameStateObstacles(true);
		moveCon.updateDestination(destination);
		SumatraModel.getInstance().getModule(ASkillSystem.class).execute(botId, skill);
		
		simTimeBlocker = new SimTimeBlocker(20);
		simTimeBlocker.await();
		
		ITrackedBot tBot = lastWorldFrameWrapper.getWorldFrame(EAiTeam.YELLOW).getBot(botId);
		assertThat(tBot.getPos().distanceTo(destination))
				.withFailMessage("The robot did not reach the destination: " + tBot.getPos())
				.isLessThan(1);
		
		assertThat(logEventWatcher.getNumEvents(Level.ERROR)).isZero();
		assertThat(logEventWatcher.getNumEvents(Level.WARN)).isZero();
		testCaseSucceeded = true;
	}
	
	
	private void startTestByRefereeMsg(String snapshotFile, Command command, double duration) throws Exception
	{
		System.out.println("startTestByRefereeMsg: " + command + " (" + snapshotFile + ")");
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
		for (Map.Entry<BotID, SimulationObject> simBotEntry : params.getInitBots().entrySet())
		{
			ITrackedBot tBot = lastWorldFrameWrapper.getSimpleWorldFrame().getBot(simBotEntry.getKey());
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
		avoidableViolations.add(EGameEvent.BALL_HOLDING);
		avoidableViolations.add(EGameEvent.KICK_TIMEOUT);
		avoidableViolations.add(EGameEvent.ROBOT_STOP_SPEED);
		// MULTIPLE_DEFENDER_PARTIALLY -> this is currently often caused by pushing and would need better detection
		// ATTACKER_IN_DEFENSE_AREA -> we do not try to avoid this
		// BALL_SPEED -> accidental double kicking between two bots often cause a higher ball speed. Ignore this for
		// now
		// BOT_STOP_SPEED -> unreliable
		AutoRefModule autoRefModule = SumatraModel.getInstance().getModule(AutoRefModule.class);
		List<GameLogEntry> allGameEvents = new ArrayList<>(autoRefModule.getEngine().getGameLog().getEntries());
		List<IGameEvent> avoidableGameEvents = allGameEvents.stream()
				.filter(e -> e.getType() == GameLogEntry.ELogEntryType.GAME_EVENT)
				.map(GameLogEntry::getGameEvent)
				.filter(e -> avoidableViolations.contains(e.getType()))
				.collect(Collectors.toList());
		assertThat(avoidableGameEvents).isEmpty();
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		lastWorldFrameWrapper = wFrameWrapper;
	}
	
	
}
