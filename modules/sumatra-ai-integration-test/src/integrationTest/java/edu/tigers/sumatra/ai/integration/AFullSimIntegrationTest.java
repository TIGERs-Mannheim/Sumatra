/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
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
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@Log4j2
public abstract class AFullSimIntegrationTest implements IWorldFrameObserver, IAIObserver
{
	private static final String MODULI_CONFIG = "integration_test.xml";

	@Rule
	public TestName testName = new TestName();

	private BerkeleyAsyncRecorder recorder;
	private final LogEventWatcher logEventWatcher = new LogEventWatcher(Level.WARN, Level.ERROR);

	protected WorldFrameWrapper lastWorldFrameWrapper = null;
	protected Map<EAiTeam, AIInfoFrame> lastAiFrames = new EnumMap<>(EAiTeam.class);
	protected RefereeMsg lastRefereeMsg = null;
	protected boolean testCaseSucceeded;

	protected boolean stuck;
	private boolean botMoved;


	@SneakyThrows
	@BeforeClass
	public static void beforeClass()
	{
		ConfigRegistration.setDefPath("../../config/");
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesOfConfig(MODULI_CONFIG);
		Geometry.setNegativeHalfTeam(ETeamColor.BLUE);
	}


	@SneakyThrows
	@Before
	public void before()
	{
		log.debug("Setting up test case {}", testName.getMethodName());
		lastRefereeMsg = null;
		lastWorldFrameWrapper = null;
		lastAiFrames.clear();
		stuck = false;
		botMoved = false;
		testCaseSucceeded = false;
		logEventWatcher.clear();
		logEventWatcher.start();

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

		SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(EAutoRefMode.ACTIVE);

		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.MATCH_MODE);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.MATCH_MODE);

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
			Files.move(Paths.get("build/state-store.json.stream"),
					targetFolder.resolve(dbPath.getFileName() + "_state-store.json.stream"),
					StandardCopyOption.REPLACE_EXISTING);
		}
		recorder.getDb().delete();

		SumatraModel.getInstance().stopModules();
		log.debug("Test case cleanup done for {}", testName.getMethodName());
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		if (lastRefereeMsg != null
				&& lastRefereeMsg.getCommand() != SslGcRefereeMessage.Referee.Command.HALT
				&& wFrameWrapper.getRefereeMsg().getCommand() == SslGcRefereeMessage.Referee.Command.HALT)
		{
			stuck = true;
		}
		botMoved = botMoved || wFrameWrapper.getSimpleWorldFrame().getBots().values().stream()
				.anyMatch(bot -> bot.getVel().getLength2() > 0.1);

		lastWorldFrameWrapper = wFrameWrapper;
		lastRefereeMsg = wFrameWrapper.getRefereeMsg();
	}


	@Override
	public void onNewAIInfoFrame(final AIInfoFrame lastAIInfoframe)
	{
		lastAiFrames.put(lastAIInfoframe.getAiTeam(), lastAIInfoframe);
	}


	protected Snapshot loadSimParamsFromSnapshot(final String snapshotFile)
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
		initSimulation(loadSimParamsFromSnapshot(snapshotFile));
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
				.collect(Collectors.toList())).isEmpty();
		assertThat(logEventWatcher.getEvents(Level.WARN).stream()
				.map(LogEvent::getMessage)
				.map(Message::getFormattedMessage)
				.collect(Collectors.toList())).isEmpty();
	}


	protected void assertBotsHaveMoved()
	{
		assertThat(botMoved).withFailMessage("No robot moved during test").isTrue();
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
