/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee;

import com.github.g3force.configurable.ConfigRegistration;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.autoreferee.proto.DesiredEventDescription;
import edu.tigers.sumatra.cam.LogfileAnalyzerVisionCam;
import edu.tigers.sumatra.gamelog.SSLGameLogReader;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyAccessor;
import edu.tigers.sumatra.persistence.BerkeleyAsyncRecorder;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.log.BerkeleyLogEvent;
import edu.tigers.sumatra.persistence.log.BerkeleyLogRecorder;
import edu.tigers.sumatra.referee.gameevent.GameEventFactory;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.SimilarityChecker;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import edu.tigers.sumatra.wp.ShapeMapBerkeleyRecorder;
import edu.tigers.sumatra.wp.WfwBerkeleyRecorder;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(Parameterized.class)
@Log4j2
@RequiredArgsConstructor
public class AutoRefIntegrationTest
{
	private static final String MODULI_CONFIG = "integration_test.xml";
	private static final String TEST_CASE_DIR = "autoref-tests";

	private static SimilarityChecker similarityChecker;

	private final String name;
	private final TestCase testCase;
	private BerkeleyAsyncRecorder recorder;
	private boolean testCaseSucceeded;


	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> data()
	{
		return findTestCases().stream().map(t -> new Object[] { t.getName(), t }).collect(Collectors.toList());
	}


	@BeforeClass
	public static void beforeClass()
	{
		ConfigRegistration.setDefPath("../../config/");
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesOfConfigSafe(MODULI_CONFIG);
		Geometry.setNegativeHalfTeam(ETeamColor.BLUE);

		similarityChecker = new SimilarityChecker().initAllGameEvents();
	}


	@SneakyThrows
	@Before
	public void before()
	{
		SumatraModel.getInstance().startModules();
		SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(EAutoRefMode.PASSIVE);


		BerkeleyDb db = BerkeleyDb.withCustomLocation(Paths.get("../../" + BerkeleyDb.getDefaultBasePath(),
				BerkeleyDb.getDefaultName() + "_" + name));
		db.add(BerkeleyLogEvent.class, new BerkeleyAccessor<>(BerkeleyLogEvent.class, false));
		db.add(BerkeleyShapeMapFrame.class, new BerkeleyAccessor<>(BerkeleyShapeMapFrame.class, true));
		db.add(WorldFrameWrapper.class, new BerkeleyAccessor<>(WorldFrameWrapper.class, true));

		recorder = new BerkeleyAsyncRecorder(db);
		recorder.add(new BerkeleyLogRecorder(db));
		recorder.add(new WfwBerkeleyRecorder(db));
		recorder.add(new ShapeMapBerkeleyRecorder(db));
		recorder.start();
	}


	@SneakyThrows
	@After
	public void after()
	{
		recorder.stop();
		recorder.awaitStop();
		if (!testCaseSucceeded)
		{
			recorder.getDb().compress();
		}
		recorder.getDb().delete();

		SumatraModel.getInstance().stopModules();
	}


	@Test
	public void runTestCase()
	{
		log.info("Start running test case {}", testCase.getName());
		SSLGameLogReader logReader = new SSLGameLogReader();
		logReader.loadFileBlocking(testCase.getLogfileLocation().toAbsolutePath().toString());
		assertThat(logReader.getPackets()).isNotEmpty();

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
			var visionCam = SumatraModel.getInstance().getModule(LogfileAnalyzerVisionCam.class);
			visionCam.playLog(logReader, frameId -> {
			});

			if (gameEvents.isEmpty() && desiredGameEvent != null)
			{
				Assert.fail("Expected game event: " + desiredGameEvent);
			}
			if (desiredGameEvent == null && !gameEvents.isEmpty())
			{
				Assert.fail("Expected no game events, but got: " + gameEvents);
			}

			for (var gameEvent : gameEvents)
			{
				if (!similarityChecker.isSimilar(gameEvent, desiredGameEvent))
				{
					Assert.fail("Game event mismatch.\nExpected: " + desiredGameEvent + "\n     Got: " + gameEvent);
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


	private static List<TestCase> findTestCases()
	{
		Path testCaseDir = Path.of(TEST_CASE_DIR);
		if (!testCaseDir.toFile().exists())
		{
			return Collections.emptyList();
		}
		try (Stream<Path> stream = Files.walk(testCaseDir))
		{
			return stream
					.filter(p -> p.getFileName().toString().endsWith(".json"))
					.map(AutoRefIntegrationTest::createTestCase)
					.sorted(Comparator.comparing(TestCase::getName))
					.collect(Collectors.toList());
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


	@Value
	@Builder
	private static class TestCase
	{
		String name;
		Path logfileLocation;
		DesiredEventDescription.DesiredEvent desiredEvent;
	}
}
