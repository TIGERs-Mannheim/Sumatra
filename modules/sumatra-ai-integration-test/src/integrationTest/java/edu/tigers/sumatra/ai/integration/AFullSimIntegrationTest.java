/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.AiBerkeleyRecorder;
import edu.tigers.sumatra.ai.BerkeleyAiFrame;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.integration.blocker.AiSimTimeBlocker;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.log.LogEventWatcher;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyAccessor;
import edu.tigers.sumatra.persistence.BerkeleyAsyncRecorder;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.log.BerkeleyLogEvent;
import edu.tigers.sumatra.persistence.log.BerkeleyLogRecorder;
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
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.ShapeMapBerkeleyRecorder;
import edu.tigers.sumatra.wp.WfwBerkeleyRecorder;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@Log4j2
public abstract class AFullSimIntegrationTest implements IWorldFrameObserver, IAIObserver
{
	private static final String MODULI_CONFIG = "integration_test.xml";
	private final LogEventWatcher logEventWatcher = new LogEventWatcher(Level.WARN, Level.ERROR);
	private final List<IGameEvent> gameEvents = new CopyOnWriteArrayList<>();
	@Rule
	public TestName testName = new TestName();
	protected WorldFrameWrapper lastWorldFrameWrapper;
	protected boolean testCaseSucceeded;
	protected boolean stuck;
	private BerkeleyAsyncRecorder recorder;
	private RefereeMsg lastRefereeMsg = null;
	private boolean botMoved;

	private Map<EAiTeam, InformationPerTeam> informationPerTeam;


	@SneakyThrows
	@BeforeClass
	public static void beforeClass()
	{
		ConfigRegistration.setDefPath("../../config/");
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesOfConfig(MODULI_CONFIG);

		SumatraModel.getInstance().startModules();
		SimulationHelper.setSimulateWithMaxSpeed(true);
		SimulationHelper.setHandleBotCount(false);

		SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(EAutoRefMode.ACTIVE);

		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.MATCH_MODE);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.MATCH_MODE);
	}


	@AfterClass
	public static void afterClass()
	{
		SumatraModel.getInstance().stopModules();
	}


	@SneakyThrows
	@Before
	public void before()
	{
		log.debug("Setting up test case {}", testName.getMethodName());
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

		BerkeleyDb db = BerkeleyDb.withCustomLocation(Paths.get("../../" + BerkeleyDb.getDefaultBasePath(),
				BerkeleyDb.getDefaultName("FRIENDLY", "NORMAL_FIRST_HALF", "yellow", "blue") + "_"
						+ testName.getMethodName()));
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

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
		SumatraModel.getInstance().getModule(AAgent.class).addObserver(this);
		log.debug("Test case setup done for {}", testName.getMethodName());
	}


	@SneakyThrows
	@After
	public void after()
	{
		log.debug("Cleaning up after test case {}", testName.getMethodName());
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

		log.debug("Test case cleanup done for {}", testName.getMethodName());
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


	protected boolean ballLeftField(AIInfoFrame frame)
	{
		return frame.getRefereeMsg().getGameEvents().stream()
				.map(IGameEvent::getType)
				.map(EGameEvent::getType)
				.anyMatch(e -> e == EGameEventType.BALL_LEFT_FIELD);
	}


	protected boolean gameRunning(AIInfoFrame frame)
	{
		return frame.getGameState().isRunning();
	}


	protected boolean gameStopped(AIInfoFrame frame)
	{
		return frame.getGameState().getState() == EGameState.STOP;
	}


	protected boolean gameBallPlacement(AIInfoFrame frame)
	{
		return frame.getGameState().getState() == EGameState.BALL_PLACEMENT;
	}


	private record InformationPerTeam(boolean firstPassSuccess, boolean ballTouched, BotID firstRobotWithContact)
	{
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
