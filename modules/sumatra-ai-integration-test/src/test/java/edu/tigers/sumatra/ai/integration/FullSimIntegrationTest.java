/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import edu.tigers.autoreferee.engine.EAutoRefMode;
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
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.gameevent.EGameEventType;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.sim.SimTimeBlocker;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.sim.SimulationParameters;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import edu.tigers.sumatra.wp.ShapeMapBerkeleyRecorder;
import edu.tigers.sumatra.wp.WfwBerkeleyRecorder;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Run tests in a full simulation environment
 */
public class FullSimIntegrationTest extends AFullSimIntegrationTest
{
	private static LogEventWatcher logEventWatcher;

	@Rule
	public TestName testName = new TestName();

	private BerkeleyAsyncRecorder recorder;
	private boolean testCaseSucceeded;
	private List<IGameEvent> gameEvents = new ArrayList<>();
	private boolean stuck;

	private boolean requireAllBotsToMove;
	private boolean botMoved;


	@Before
	@Override
	public void before() throws Exception
	{
		logEventWatcher = new LogEventWatcher(Level.WARN, Level.ERROR);
		Logger.getRootLogger().addAppender(logEventWatcher);

		SumatraModel.getInstance().startModules();
		SimulationHelper.setSimulateWithMaxSpeed(true);
		SimulationHelper.setHandleBotCount(false);

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

		gameEvents.clear();
		stuck = false;

		SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(EAutoRefMode.ACTIVE);

		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.MATCH_MODE);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.MATCH_MODE);

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);

		testCaseSucceeded = false;
		requireAllBotsToMove = true;
		botMoved = false;
	}


	@After
	public void after() throws Exception
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);

		Logger.getRootLogger().removeAppender(logEventWatcher);

		recorder.stop();
		recorder.awaitStop();
		if (testCaseSucceeded)
		{
			Files.delete(Paths.get("gc-state.json"));
			Files.delete(Paths.get("gc-state.backup.gz"));
		} else
		{
			recorder.getDb().compress();
			Files.move(Paths.get("gc-state.json"),
					Paths.get("target/logs/" + testName.getMethodName() + "_gc-state.json"),
					StandardCopyOption.REPLACE_EXISTING);
			Files.move(Paths.get("gc-state.backup.gz"),
					Paths.get("target/logs/" + testName.getMethodName() + "_gc-state.backup.gz"),
					StandardCopyOption.REPLACE_EXISTING);
		}
		recorder.getDb().delete();

		SumatraModel.getInstance().stopModules();
	}


	/**
	 * Check if we can handle a force start: Robots should move, no rules should be violated. That's it.
	 */
	@Test
	public void testForceStart() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/stoppedGame.snap");
		params.setRefereeCommand(Command.FORCE_START);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(5)
				.addStopCondition(this::ballLeftField)
				.await();

		SimulationHelper.pauseSimulation();

		assertConditions();
	}


	/**
	 * Check if we shoot a goal on force_start, when there are no opponents
	 */
	@Test
	public void testForceStartWithoutOpponents() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/noOpponents.snap");
		params.setRefereeCommand(Command.FORCE_START);
		SimulationHelper.initSimulation(params);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.OFF);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(15)
				.addStopCondition(this::ballLeftField)
				.await();

		SimulationHelper.pauseSimulation();

		assertGameEvent(EGameEvent.GOAL);
		assertConditions();
	}


	/**
	 * Check if we can perform a kickoff. If the game state switches to running without any other events in between,
	 * we are good.
	 */
	@Test
	public void testKickoff() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/maintenance.snap");
		params.setRefereeCommand(Command.PREPARE_KICKOFF_BLUE);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(25)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		// let the simulation resume a little bit more to make sure,
		// no delayed events like ball speeding are called.
		defaultSimTimeBlocker(5)
				.await();

		SimulationHelper.pauseSimulation();

		assertConditions();
	}


	/**
	 * Check if we shoot a goal on kick_off without opponents
	 */
	@Test
	public void testKickoffWithoutOpponents() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/noOpponents.snap");
		params.setRefereeCommand(Command.PREPARE_KICKOFF_YELLOW);
		SimulationHelper.initSimulation(params);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.OFF);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(25)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		// let the simulation resume a little bit more to make sure,
		// no delayed events like ball speeding are called.
		defaultSimTimeBlocker(5)
				.await();

		SimulationHelper.pauseSimulation();

		assertGameEvent(EGameEvent.GOAL);
		assertConditions();
	}


	/**
	 * Check if we can do an indirect free kick from our own corner
	 */
	@Test
	public void testIndirectFreeKickOwnCorner() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/cornerBlueSide.snap");
		params.setRefereeCommand(Command.INDIRECT_FREE_BLUE);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(10)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		// let the simulation resume a little bit more to make sure,
		// no delayed events like ball speeding are called.
		defaultSimTimeBlocker(5)
				.await();

		SimulationHelper.pauseSimulation();

		assertConditions();
	}


	/**
	 * Check if we can do an indirect free kick from the opponent corner
	 */
	@Test
	public void testIndirectFreeKickOpponentCorner() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/cornerBlueSide.snap");
		params.setRefereeCommand(Command.INDIRECT_FREE_YELLOW);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(10)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		// let the simulation resume a little bit more to make sure,
		// no delayed events like ball speeding are called.
		defaultSimTimeBlocker(5)
				.await();

		SimulationHelper.pauseSimulation();

		assertConditions();
	}


	/**
	 * Check if we can do an direct free kick from our own corner
	 */
	@Test
	public void testDirectFreeKickOwnCorner() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/cornerBlueSide.snap");
		params.setRefereeCommand(Command.DIRECT_FREE_BLUE);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(10)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		// let the simulation resume a little bit more to make sure,
		// no delayed events like ball speeding are called.
		defaultSimTimeBlocker(5)
				.await();

		SimulationHelper.pauseSimulation();

		assertConditions();
	}


	/**
	 * Check if we can do an direct free kick from the opponent corner
	 */
	@Test
	public void testDirectFreeKickOpponentCorner() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/cornerBlueSide.snap");
		params.setRefereeCommand(Command.DIRECT_FREE_YELLOW);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(10)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		// let the simulation resume a little bit more to make sure,
		// no delayed events like ball speeding are called.
		defaultSimTimeBlocker(5)
				.await();

		SimulationHelper.pauseSimulation();

		assertConditions();
	}


	/**
	 * Check if penalty kicks work
	 */
	@Test
	public void testPenalty() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/maintenanceBallOnPenaltyMarkYellow.snap");
		params.setRefereeCommand(Command.PREPARE_PENALTY_BLUE);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(60)
				.addStopCondition(this::gameRunning)
				.await();

		assertGameState(EGameState.RUNNING);

		// let the simulation resume a little bit more to make sure,
		// no delayed events like ball speeding are called.
		defaultSimTimeBlocker(5)
				.await();

		SimulationHelper.pauseSimulation();

		assertConditions();
	}


	/**
	 * Check if robots move correctly during stop, without violating rules
	 */
	@Test
	public void testStopBallOnCenter() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/maintenance.snap");
		params.setRefereeCommand(Command.STOP);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(7)
				.await();
		defaultSimTimeBlocker(5)
				.addStopCondition(this::prepared)
				.await();

		SimulationHelper.pauseSimulation();

		assertConditions();
	}


	/**
	 * Check if robots move correctly during stop, without violating rules
	 */
	@Test
	public void testStopBallOnPenaltyMark() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/maintenanceBallOnPenaltyMarkYellow.snap");
		params.setRefereeCommand(Command.STOP);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(7)
				.await();
		defaultSimTimeBlocker(5)
				.addStopCondition(this::prepared)
				.await();

		SimulationHelper.pauseSimulation();

		requireAllBotsToMove = false;
		assertConditions();
	}


	/**
	 * Check if robots move correctly during stop, without violating rules
	 */
	@Test
	public void testStopBallInCorner() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/maintenanceBallInCorner.snap");
		params.setRefereeCommand(Command.STOP);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(7)
				.await();
		defaultSimTimeBlocker(5)
				.addStopCondition(this::prepared)
				.await();

		SimulationHelper.pauseSimulation();

		assertConditions();
	}


	/**
	 * Check if robots move correctly during stop, without violating rules
	 */
	@Test
	public void testStopBallOutsideField() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/maintenanceBallOutsideField.snap");
		params.setRefereeCommand(Command.STOP);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(7)
				.await();
		defaultSimTimeBlocker(5)
				.addStopCondition(this::prepared)
				.await();

		SimulationHelper.pauseSimulation();

		assertConditions();
	}


	@Test
	public void testBallPlacement() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/maintenanceBallInCorner.snap");
		params.setBallPlacementPos(Vector2.fromXY(2900, -900));
		params.setRefereeCommand(Command.BALL_PLACEMENT_BLUE);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		defaultSimTimeBlocker(5).await();
		defaultSimTimeBlocker(20)
				.addStopCondition(this::gameHalted)
				.await();

		SimulationHelper.pauseSimulation();

		assertGameEvent(EGameEvent.PLACEMENT_SUCCEEDED);
		assertGameState(EGameState.HALT);
		assertConditions();
	}


	@Test
	public void testComplicatedPathPlanning() throws Exception
	{
		SimulationParameters params = loadSimParamsFromSnapshot("snapshots/pathplanningSituationPenArea.snap");
		params.setRefereeCommand(Command.FORCE_START);
		SimulationHelper.initSimulation(params);
		SimulationHelper.startSimulation();

		// turn of autoRef
		SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(EAutoRefMode.OFF);

		// turn of AIs
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAIControlState.OFF);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAIControlState.OFF);

		// initialize the `lastWorldFrameWrapper` field
		defaultSimTimeBlocker(0.1).await();

		BotID botId = BotID.createBotId(0, ETeamColor.YELLOW);
		IVector2 initPos = lastWorldFrameWrapper.getWorldFrame(EAiTeam.YELLOW).getBot(botId).getPos();
		IVector2 destination = Vector2.fromXY(-5900, -1400);
		AMoveSkill skill = AMoveToSkill.createMoveToSkill();
		MovementCon moveCon = skill.getMoveCon();
		moveCon.setPenaltyAreaAllowedOur(false);
		moveCon.setPenaltyAreaAllowedTheir(false);
		moveCon.setIgnoreGameStateObstacles(true);
		moveCon.updateDestination(destination);
		SumatraModel.getInstance().getModule(ASkillSystem.class).execute(botId, skill);

		defaultSimTimeBlocker(20)
				.addStopCondition(w -> w.getSimpleWorldFrame().getBot(botId).getPos().distanceTo(destination) < 0.1)
				.await();

		SimulationHelper.pauseSimulation();

		ITrackedBot tBot = lastWorldFrameWrapper.getWorldFrame(EAiTeam.YELLOW).getBot(botId);
		assertThat(tBot.getPos().distanceTo(destination))
				.withFailMessage("The robot did not move from %s to %s in time. Last here: %s", initPos, destination,
						tBot.getPos())
				.isLessThan(1);

		requireAllBotsToMove = false;
		assertConditions();
	}


	private SimTimeBlocker defaultSimTimeBlocker(double maxDuration)
	{
		final SimTimeBlocker simTimeBlocker;
		simTimeBlocker = new SimTimeBlocker(maxDuration);
		simTimeBlocker.addStopCondition(this::stuck);
		return simTimeBlocker;
	}


	private SimulationParameters loadSimParamsFromSnapshot(final String snapshotFile) throws IOException
	{
		Snapshot snapshot = Snapshot.loadFromResources(snapshotFile);
		return new SimulationParameters(snapshot);
	}


	private void assertConditions()
	{
		assertNoWarningsOrErrors();
		assertBotsHaveMoved();
		assertNoAvoidableViolations();
		testCaseSucceeded = true;
	}


	private void assertNoWarningsOrErrors()
	{
		assertThat(logEventWatcher.getEvents(Level.ERROR).stream().map(LoggingEvent::getRenderedMessage)
				.collect(Collectors.toList())).isEmpty();
		assertThat(logEventWatcher.getEvents(Level.WARN).stream().map(LoggingEvent::getRenderedMessage)
				.collect(Collectors.toList())).isEmpty();
	}


	private void assertBotsHaveMoved()
	{
		if (!requireAllBotsToMove)
		{
			return;
		}
		assertThat(botMoved).withFailMessage("No robot moved during test").isTrue();
	}


	private void assertNoAvoidableViolations() throws ModuleNotFoundException
	{
		Set<EGameEvent> avoidableViolations = new HashSet<>();
		avoidableViolations.add(EGameEvent.PLACEMENT_FAILED);

		avoidableViolations.add(EGameEvent.INDIRECT_GOAL);
		avoidableViolations.add(EGameEvent.CHIP_ON_GOAL);

		// avoidableViolations.add(EGameEvent.AIMLESS_KICK);
		avoidableViolations.add(EGameEvent.KICK_TIMEOUT);
		avoidableViolations.add(EGameEvent.KEEPER_HELD_BALL);
		avoidableViolations.add(EGameEvent.ATTACKER_DOUBLE_TOUCHED_BALL);
		avoidableViolations.add(EGameEvent.ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA);
		avoidableViolations.add(EGameEvent.BOT_DRIBBLED_BALL_TOO_FAR);
		avoidableViolations.add(EGameEvent.BOT_KICKED_BALL_TOO_FAST);

		avoidableViolations.add(EGameEvent.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA);
		avoidableViolations.add(EGameEvent.BOT_INTERFERED_PLACEMENT);
		avoidableViolations.add(EGameEvent.BOT_CRASH_DRAWN);
		avoidableViolations.add(EGameEvent.BOT_CRASH_UNIQUE);
		avoidableViolations.add(EGameEvent.BOT_CRASH_UNIQUE_SKIPPED);
		avoidableViolations.add(EGameEvent.BOT_PUSHED_BOT);
		avoidableViolations.add(EGameEvent.BOT_PUSHED_BOT_SKIPPED);
		avoidableViolations.add(EGameEvent.BOT_HELD_BALL_DELIBERATELY);
		avoidableViolations.add(EGameEvent.BOT_TIPPED_OVER);
		avoidableViolations.add(EGameEvent.BOT_TOO_FAST_IN_STOP);
		avoidableViolations.add(EGameEvent.DEFENDER_TOO_CLOSE_TO_KICK_POINT);
		avoidableViolations.add(EGameEvent.DEFENDER_IN_DEFENSE_AREA_PARTIALLY);
		avoidableViolations.add(EGameEvent.DEFENDER_IN_DEFENSE_AREA);
		avoidableViolations.add(EGameEvent.ATTACKER_TOUCHED_OPPONENT_IN_DEFENSE_AREA);
		avoidableViolations.add(EGameEvent.ATTACKER_TOUCHED_OPPONENT_IN_DEFENSE_AREA_SKIPPED);

		avoidableViolations.add(EGameEvent.MULTIPLE_CARDS);
		avoidableViolations.add(EGameEvent.MULTIPLE_PLACEMENT_FAILURES);
		avoidableViolations.add(EGameEvent.MULTIPLE_FOULS);

		List<IGameEvent> avoidableGameEvents = gameEvents.stream()
				.filter(e -> avoidableViolations.contains(e.getType()))
				.collect(Collectors.toList());
		assertThat(avoidableGameEvents).isEmpty();
	}


	private void assertGameEvent(EGameEvent gameEvent)
	{
		assertThat(gameEvents.stream().map(IGameEvent::getType)).contains(gameEvent);
	}


	private void assertGameState(EGameState gameState)
	{
		assertThat(lastWorldFrameWrapper.getGameState().getState()).isEqualTo(gameState);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		final ArrayList<IGameEvent> newGameEvents = new ArrayList<>(
				wFrameWrapper.getRefereeMsg().getGameEvents());
		if (lastRefereeMsg != null)
		{
			newGameEvents.removeAll(lastRefereeMsg.getGameEvents());

			if (lastRefereeMsg.getCommand() != Command.HALT && wFrameWrapper.getRefereeMsg().getCommand() == Command.HALT)
			{
				stuck = true;
			}
		}
		this.gameEvents.addAll(newGameEvents);

		botMoved = botMoved || wFrameWrapper.getSimpleWorldFrame().getBots().values().stream()
				.anyMatch(bot -> bot.getVel().getLength2() > 0.1);

		super.onNewWorldFrame(wFrameWrapper);
	}


	private boolean ballLeftField(WorldFrameWrapper wfw)
	{
		return wfw.getRefereeMsg().getGameEvents().stream()
				.map(IGameEvent::getType)
				.map(EGameEvent::getType)
				.anyMatch(e -> e == EGameEventType.BALL_LEFT_FIELD);
	}


	private boolean prepared(WorldFrameWrapper wfw)
	{
		return wfw.getRefereeMsg().getGameEvents().stream()
				.map(IGameEvent::getType)
				.anyMatch(e -> e == EGameEvent.PREPARED);
	}


	private boolean gameRunning(WorldFrameWrapper wfw)
	{
		return wfw.getGameState().isRunning();
	}


	private boolean gameHalted(WorldFrameWrapper wfw)
	{
		return wfw.getGameState().getState() == EGameState.HALT;
	}


	private boolean stuck(@SuppressWarnings("unused") WorldFrameWrapper wfw)
	{
		return stuck;
	}
}
