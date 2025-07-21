/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.AiPersistenceRecorder;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.PersistenceAiFrame;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.integration.blocker.AiSimTimeBlocker;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.log.LogEventWatcher;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.EPersistenceKeyType;
import edu.tigers.sumatra.persistence.PersistenceAsyncRecorder;
import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.persistence.log.PersistenceLogEvent;
import edu.tigers.sumatra.persistence.log.PersistenceLogRecorder;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.gameevent.EGameEventType;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.PersistenceShapeMapFrame;
import edu.tigers.sumatra.wp.ShapeMapPersistenceRecorder;
import edu.tigers.sumatra.wp.WfwPersistenceRecorder;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@Log4j2
@Timeout(value = 5, unit = TimeUnit.MINUTES)
public abstract class AFullSimIntegrationTest implements IWorldFrameObserver, IAIObserver
{
	private static final String MODULI_CONFIG = "integration_test.xml";
	private final LogEventWatcher logEventWatcher = new LogEventWatcher(Level.WARN, Level.ERROR);
	private final List<IGameEvent> gameEvents = new CopyOnWriteArrayList<>();
	protected WorldFrameWrapper lastWorldFrameWrapper;
	protected boolean testCaseSucceeded;
	protected boolean stuck;
	private PersistenceAsyncRecorder recorder;
	private RefereeMsg lastRefereeMsg = null;
	private boolean botMoved;

	private Map<EAiTeam, InformationPerTeam> informationPerTeam;


	@SneakyThrows
	@BeforeAll
	public static void beforeClass()
	{
		ConfigRegistration.setDefPath("../../config/");
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModules();
	}


	@SneakyThrows
	@BeforeEach
	public void before(TestInfo testInfo)
	{
		log.debug("Setting up test case {}", testInfo.getDisplayName());

		SumatraModel.getInstance().startModules();
		SimulationHelper.setSimulateWithMaxSpeed(true);
		SimulationHelper.setHandleBotCount(false);

		SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(EAutoRefMode.ACTIVE);

		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.MATCH_MODE);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.MATCH_MODE);

		lastRefereeMsg = null;
		lastWorldFrameWrapper = null;
		stuck = false;
		botMoved = false;
		testCaseSucceeded = false;
		informationPerTeam = Arrays.stream(EAiTeam.values())
				.collect(Collectors.toMap(t -> t, t -> new InformationPerTeam(false, false, BotID.noBot())));
		logEventWatcher.clear();
		logEventWatcher.start();
		gameEvents.clear();

		SimulationHelper.resetSimulation();

		PersistenceDb db = PersistenceDb.withCustomLocation(Paths.get("../../" + PersistenceDb.getDefaultBasePath(),
				PersistenceDb.getDefaultName("FRIENDLY", "NORMAL_FIRST_HALF", "yellow", "blue") + "_"
						+ testInfo.getDisplayName()));
		db.add(PersistenceAiFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		db.add(PersistenceLogEvent.class, EPersistenceKeyType.ARBITRARY);
		db.add(PersistenceShapeMapFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		db.add(WorldFrameWrapper.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);

		recorder = new PersistenceAsyncRecorder(db);
		recorder.add(new AiPersistenceRecorder(db));
		recorder.add(new PersistenceLogRecorder(db));
		recorder.add(new WfwPersistenceRecorder(db));
		recorder.add(new ShapeMapPersistenceRecorder(db));
		recorder.start();

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
		SumatraModel.getInstance().getModule(AAgent.class).addObserver(this);
		log.debug("Test case setup done for {}", testInfo.getDisplayName());
	}


	@SneakyThrows
	@AfterEach
	public void after(TestInfo testInfo)
	{
		log.debug("Cleaning up after test case {}", testInfo.getDisplayName());
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
		SumatraModel.getInstance().getModule(AAgent.class).removeObserver(this);

		logEventWatcher.stop();
		recorder.stop();
		recorder.awaitStop();
		if (!testCaseSucceeded)
		{
			recorder.getDb().compress();
			Path dbPath = Paths.get(recorder.getDb().getDbPath());
			Path targetFolder = dbPath.getParent();
			Path stateStorePath = Paths.get("build/state-store.json.stream");
			if (Files.exists(stateStorePath))
			{
				Files.copy(stateStorePath,
						targetFolder.resolve(dbPath.getFileName() + "_state-store.json.stream"),
						StandardCopyOption.REPLACE_EXISTING);
			}
		}
		recorder.getDb().delete();

		SumatraModel.getInstance().stopModules();

		log.debug("Test case cleanup done for {}", testInfo.getDisplayName());
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		final ArrayList<IGameEvent> newGameEvents = new ArrayList<>(
				wFrameWrapper.getRefereeMsg().getGameEvents());
		if (lastRefereeMsg != null)
		{
			newGameEvents.removeAll(lastRefereeMsg.getGameEvents());
		}
		this.gameEvents.addAll(newGameEvents);

		if (lastRefereeMsg != null
				&& lastRefereeMsg.getCommand() != SslGcRefereeMessage.Referee.Command.HALT
				&& wFrameWrapper.getRefereeMsg().getCommand() == SslGcRefereeMessage.Referee.Command.HALT)
		{
			stuck = true;
		}
		botMoved = botMoved || wFrameWrapper.getSimpleWorldFrame().getBots().values().stream()
				.anyMatch(bot -> bot.getVel().getLength2() > 0.1);

		informationPerTeam.keySet().forEach(team -> updateInformationPerTeam(wFrameWrapper, team));

		lastWorldFrameWrapper = wFrameWrapper;
		lastRefereeMsg = wFrameWrapper.getRefereeMsg();
	}


	private void updateInformationPerTeam(WorldFrameWrapper wFrameWrapper, EAiTeam team)
	{
		var lastInformation = informationPerTeam.get(team);
		var wFrane = wFrameWrapper.getWorldFrame(team);
		var robotsTouching = wFrane.getTigerBotsAvailable().values().stream()
				.filter(bot -> bot.getBallContact().hadRecentContact() || bot.getBallContact().hadRecentContactFromVision())
				.toList();

		var ballTouched = lastInformation.ballTouched || !robotsTouching.isEmpty();
		BotID firstRobotWithContact;
		if (lastInformation.firstRobotWithContact.equals(BotID.noBot()))
		{
			firstRobotWithContact = robotsTouching.stream()
					.min(Comparator.comparingDouble(bot -> wFrane.getBall().getPos().distanceToSqr(bot.getPos())))
					.map(ITrackedBot::getBotId)
					.orElseGet(BotID::noBot);
		} else
		{
			firstRobotWithContact = lastInformation.firstRobotWithContact;
		}
		var firstPassSuccess = lastInformation.firstPassSuccess || (
				!informationPerTeam.get(team.opposite()).ballTouched
						&& robotsTouching.stream().noneMatch(bot -> firstRobotWithContact.equals(bot.getBotId()))
						&& robotsTouching.stream().anyMatch(bot -> !firstRobotWithContact.equals(bot.getBotId()))
		);
		informationPerTeam.put(team, new InformationPerTeam(firstPassSuccess, ballTouched, firstRobotWithContact));
	}


	protected Snapshot readSnapshot(final String snapshotFile)
	{
		try
		{
			return Snapshot.loadFromResources(snapshotFile);
		} catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}


	protected void initSimulation(String snapshotFile)
	{
		SimulationHelper.initSimulation(readSnapshot(snapshotFile));
	}


	protected void initSimulation(Snapshot snapshot)
	{
		SimulationHelper.initSimulation(snapshot);
	}


	protected void sendRefereeCommand(SslGcRefereeMessage.Referee.Command command)
	{
		SumatraModel.getInstance().getModule(AReferee.class).sendGameControllerEvent(GcEventFactory.command(command));
	}


	protected void assertNoWarningsOrErrors()
	{
		assertThat(logEventWatcher.getEvents(Level.ERROR).stream()
				.map(LogEvent::getMessage)
				.map(Message::getFormattedMessage)
				.toList()).isEmpty();
		assertThat(logEventWatcher.getEvents(Level.WARN).stream()
				.map(LogEvent::getMessage)
				.map(Message::getFormattedMessage)
				.toList()).isEmpty();
	}


	protected void assertBotsHaveMoved()
	{
		assertThat(botMoved).withFailMessage("No robot moved during test").isTrue();
	}


	protected void assertSuccessfulFirstPass(EAiTeam team)
	{
		assertThat(informationPerTeam.get(team).firstPassSuccess)
				.withFailMessage("First pass was not successful").isTrue();
	}


	protected void assertBallUntouchedByOpponent(EAiTeam team)
	{
		assertThat(informationPerTeam.get(team.opposite()).ballTouched)
				.withFailMessage("Opponent team touched ball").isFalse();
	}


	protected void assertNoAvoidableViolations()
	{
		Set<EGameEvent> avoidableViolations = new HashSet<>();
		avoidableViolations.add(EGameEvent.PLACEMENT_FAILED);

		avoidableViolations.add(EGameEvent.KEEPER_HELD_BALL);
		avoidableViolations.add(EGameEvent.ATTACKER_DOUBLE_TOUCHED_BALL);
		avoidableViolations.add(EGameEvent.ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA);
		avoidableViolations.add(EGameEvent.BOT_KICKED_BALL_TOO_FAST);

		avoidableViolations.add(EGameEvent.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA);
		avoidableViolations.add(EGameEvent.BOT_INTERFERED_PLACEMENT);
		avoidableViolations.add(EGameEvent.BOT_CRASH_DRAWN);
		avoidableViolations.add(EGameEvent.BOT_CRASH_UNIQUE);
		avoidableViolations.add(EGameEvent.BOT_PUSHED_BOT);
		avoidableViolations.add(EGameEvent.BOT_HELD_BALL_DELIBERATELY);
		avoidableViolations.add(EGameEvent.BOT_TIPPED_OVER);
		avoidableViolations.add(EGameEvent.BOT_TOO_FAST_IN_STOP);
		avoidableViolations.add(EGameEvent.DEFENDER_TOO_CLOSE_TO_KICK_POINT);
		avoidableViolations.add(EGameEvent.DEFENDER_IN_DEFENSE_AREA);

		avoidableViolations.add(EGameEvent.MULTIPLE_CARDS);
		avoidableViolations.add(EGameEvent.MULTIPLE_FOULS);

		List<IGameEvent> avoidableGameEvents = gameEvents.stream()
				.filter(e -> avoidableViolations.contains(e.getType()))
				.toList();
		assertThat(avoidableGameEvents).isEmpty();
	}


	protected void assertGameEvent(EGameEvent gameEvent)
	{
		assertThat(gameEvents.stream().map(IGameEvent::getType)).contains(gameEvent);
	}


	protected void assertNotGameEvent(EGameEvent gameEvent)
	{
		assertThat(gameEvents.stream().map(IGameEvent::getType)).doesNotContain(gameEvent);
	}


	protected void assertGameState(EGameState gameState)
	{
		assertThat(lastWorldFrameWrapper.getGameState().getState()).isEqualTo(gameState);
	}


	protected void success()
	{
		testCaseSucceeded = true;
	}


	private record InformationPerTeam(boolean firstPassSuccess, boolean ballTouched, BotID firstRobotWithContact)
	{
	}

	@RequiredArgsConstructor
	protected static class GameStateStopCondition implements AiSimTimeBlocker.IStopCondition
	{
		private final EGameState gameState;


		@Override
		public boolean stopSimulation(final AIInfoFrame frame)
		{
			return frame.getGameState().getState() == gameState;
		}


		@Override
		public String name()
		{
			return AiSimTimeBlocker.IStopCondition.super.name() + "(" + gameState + ")";
		}
	}

	protected static class BallLeftFieldStopCondition implements AiSimTimeBlocker.IStopCondition
	{
		@Override
		public boolean stopSimulation(final AIInfoFrame frame)
		{
			return frame.getRefereeMsg().getGameEvents().stream()
					.map(IGameEvent::getType)
					.map(EGameEvent::getType)
					.anyMatch(e -> e == EGameEventType.BALL_LEFT_FIELD);
		}


		@Override
		public String hint(final AIInfoFrame frame)
		{
			return "Game events: " + frame.getRefereeMsg().getGameEvents().stream()
					.map(IGameEvent::getType)
					.map(EGameEvent::getType).toList();
		}
	}

	@RequiredArgsConstructor
	protected static class BallMovedStopCondition implements AiSimTimeBlocker.IStopCondition
	{
		final double moveThreshold;
		final double duration;
		long firstTimeBallMoving = 0;
		IVector2 initPos = null;


		@Override
		public boolean stopSimulation(final AIInfoFrame frame)
		{
			IVector2 ballPos = frame.getSimpleWorldFrame().getBall().getPos();
			if (initPos == null)
			{
				initPos = ballPos;
				return false;
			}

			double moveDist = initPos.distanceTo(ballPos);

			if (firstTimeBallMoving == 0 && moveDist > moveThreshold)
			{
				firstTimeBallMoving = frame.getSimpleWorldFrame().getTimestamp();
			}
			double dt = (frame.getSimpleWorldFrame().getTimestamp() - firstTimeBallMoving) / 1e9;
			return firstTimeBallMoving != 0 && dt > duration;
		}
	}
}
