/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee;

import com.github.g3force.configurable.ConfigRegistration;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.autoreferee.proto.DesiredEventDescription;
import edu.tigers.sumatra.gamelog.GameLogPlayer;
import edu.tigers.sumatra.gamelog.GameLogReader;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.log.LogEventWatcher;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.EPersistenceKeyType;
import edu.tigers.sumatra.persistence.PersistenceAsyncRecorder;
import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.persistence.log.PersistenceLogEvent;
import edu.tigers.sumatra.persistence.log.PersistenceLogRecorder;
import edu.tigers.sumatra.referee.gameevent.GameEventFactory;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.SimilarityChecker;
import edu.tigers.sumatra.wp.PersistenceShapeMapFrame;
import edu.tigers.sumatra.wp.ShapeMapPersistenceRecorder;
import edu.tigers.sumatra.wp.WfwPersistenceRecorder;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;


@Log4j2
class AutoRefIntegrationTest
{
	private static final String MODULI_CONFIG = "integration_test.xml";
	private static final String TEST_CASE_DIR = "config/autoref-tests";

	private static SimilarityChecker similarityChecker;

	private PersistenceAsyncRecorder recorder;
	private final LogEventWatcher logEventWatcher = new LogEventWatcher(Level.WARN, Level.ERROR);
	private boolean testCaseSucceeded;


	@BeforeAll
	static void beforeClass()
	{
		ConfigRegistration.setDefPath("../../config/");
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesOfConfigSafe(MODULI_CONFIG);
		Geometry.setNegativeHalfTeam(ETeamColor.BLUE);

		similarityChecker = new SimilarityChecker().initAllGameEvents();
	}


	@SneakyThrows
	@BeforeEach
	void before(TestInfo testInfo)
	{
		logEventWatcher.clear();
		logEventWatcher.start();

		SumatraModel.getInstance().startModules();
		SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(EAutoRefMode.PASSIVE);

		String name = testInfo.getTestMethod().toString();
		PersistenceDb db = PersistenceDb.withCustomLocation(Paths.get("../../" + PersistenceDb.getDefaultBasePath(),
				PersistenceDb.getDefaultName("FRIENDLY", "NORMAL_FIRST_HALF", "yellow", "blue") + "_" + name));
		db.add(PersistenceLogEvent.class, EPersistenceKeyType.ARBITRARY);
		db.add(PersistenceShapeMapFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		db.add(WorldFrameWrapper.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);

		recorder = new PersistenceAsyncRecorder(db);
		recorder.add(new PersistenceLogRecorder(db));
		recorder.add(new WfwPersistenceRecorder(db));
		recorder.add(new ShapeMapPersistenceRecorder(db));
		recorder.start();
	}


	@SneakyThrows
	@AfterEach
	void after()
	{
		logEventWatcher.stop();
		recorder.stop();
		recorder.awaitStop();
		if (!testCaseSucceeded)
		{
			recorder.getDb().compress();
		}
		recorder.getDb().delete();

		SumatraModel.getInstance().stopModules();
	}


	@ParameterizedTest(allowZeroInvocations = true)
	@MethodSource("findTestCases")
	void runTestCase(TestCase testCase)
	{
		log.info("Start running test case {}", testCase.getName());
		GameLogReader logReader = new GameLogReader();
		logReader.loadFileBlocking(testCase.getLogfileLocation().toAbsolutePath().toString());
		assertThat(logReader.getMessages()).isNotEmpty();

		var desiredEvent = testCase.getDesiredEvent();
		var expectedEventProto = desiredEvent.hasExpectedEvent()
				? desiredEvent.getExpectedEvent()
				: null;

		var desiredGameEvent = Optional.ofNullable(expectedEventProto)
				.flatMap(GameEventFactory::fromProtobuf)
				.orElse(null);

		List<IGameEvent> gameEvents = new ArrayList<>();
		IAutoRefObserver autoRefObserver = gameEvents::add;
		SumatraModel.getInstance().getModule(AutoRefModule.class).addObserver(autoRefObserver);

		try
		{
			var visionCam = SumatraModel.getInstance().getModule(GameLogPlayer.class);
			visionCam.playlogFast(logReader);

			assertNoWarningsOrErrors();

			if (gameEvents.isEmpty() && desiredGameEvent != null)
			{
				fail("Expected game event: " + desiredGameEvent);
			}
			if (desiredGameEvent == null && !gameEvents.isEmpty())
			{
				fail("Expected no game events, but got: " + gameEvents);
			}

			for (var gameEvent : gameEvents)
			{
				if (!similarityChecker.isSimilar(gameEvent, desiredGameEvent))
				{
					fail("Game event mismatch.\nExpected: " + desiredGameEvent + "\n     Got: " + gameEvent);
				}
				if (desiredEvent.getStopAfterEvent())
				{
					break;
				}
			}
			testCaseSucceeded = true;
		} finally
		{
			SumatraModel.getInstance().getModule(AutoRefModule.class).removeObserver(autoRefObserver);
		}
	}


	private static Stream<TestCase> findTestCases()
	{
		Path testCaseDir = Path.of(TEST_CASE_DIR);
		if (!testCaseDir.toFile().exists())
		{
			return Stream.of();
		}
		try (Stream<Path> stream = Files.walk(testCaseDir))
		{
			return stream
					.filter(p -> p.getFileName().toString().endsWith(".json"))
					.map(AutoRefIntegrationTest::createTestCase)
					.sorted(Comparator.comparing(TestCase::getName))
					.toList()
					.stream();
		} catch (IOException e)
		{
			throw new IllegalStateException("Could not walk through test case folder.", e);
		}
	}


	@SneakyThrows
	private static TestCase createTestCase(Path testCaseFile)
	{
		String eventType = testCaseFile.getParent().getFileName().toString();
		String name = testCaseFile.getFileName().toString().replace(".json", "");
		Path logfileLocation = testCaseFile.getParent().resolve(name + ".log");

		var desiredEventBuilder = DesiredEventDescription.DesiredEvent.newBuilder();
		try
		{
			JsonFormat.parser().ignoringUnknownFields().merge(Files.readString(testCaseFile), desiredEventBuilder);
		} catch (InvalidProtocolBufferException e)
		{
			throw new IllegalStateException("Could not read " + testCaseFile, e);
		}

		return TestCase.builder()
				.name(eventType + "-" + name)
				.logfileLocation(logfileLocation)
				.desiredEvent(desiredEventBuilder.build())
				.build();
	}


	private void assertNoWarningsOrErrors()
	{
		assertThat(logEventWatcher.getEvents(Level.ERROR).stream()
				.map(LogEvent::getMessage)
				.map(Message::getFormattedMessage)
				.toList()).isEmpty();
		assertThat(logEventWatcher.getEvents(Level.WARN).stream()
				.map(LogEvent::getMessage)
				.map(Message::getFormattedMessage)
				.filter(this::notMissingCamFrame)
				.filter(this::notMissingKickEvent)
				.toList()).isEmpty();
	}


	private boolean notMissingCamFrame(String message)
	{
		// Missing cam frames is sort-of expected in some test cases
		return !message.contains("Non-consecutive cam frame");
	}


	private boolean notMissingKickEvent(String message)
	{
		// Bug: https://gitlab.tigers-mannheim.de/main/Sumatra/-/issues/1859
		return !message.contains("Goal detected, but no kick event found");
	}


	@Value
	@Builder
	static class TestCase
	{
		String name;
		Path logfileLocation;
		DesiredEventDescription.DesiredEvent desiredEvent;
	}
}
