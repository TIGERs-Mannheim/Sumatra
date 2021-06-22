/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.ares.AresData;
import edu.tigers.sumatra.ai.athena.Athena;
import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.Metis;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.wp.data.BallContact;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AAiIntegrationTest
{
	private static final String MODULI_CONFIG = "integration_test.xml";

	protected Metis metis;
	private Athena athena;

	private SimpleWorldFrame swf;
	private BaseAiFrame baseAiFrame;
	private MetisAiFrame metisAiFrame;
	private AthenaAiFrame athenaAiFrame;
	private AIInfoFrame prevFrame;

	private WorldFrameWrapper wfw;
	private RefereeMsg refereeMsg = new RefereeMsg();
	private boolean newRefereeMsg = false;
	private GameState gameState = GameState.HALT;
	private static final ETeamColor TEAM_COLOR = ETeamColor.YELLOW;

	private boolean metisCalled;
	private boolean athenaCalled;
	private boolean startCalled;

	protected static LogEventWatcher logEventWatcher = new LogEventWatcher(Level.WARN, Level.ERROR);


	@BeforeClass
	public static void beforeClass()
	{
		ConfigRegistration.setDefPath("../../config/");
		logEventWatcher.clear();
		logEventWatcher.start();
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesOfConfigSafe(MODULI_CONFIG);
		Geometry.refresh();
		Geometry.setNegativeHalfTeam(ETeamColor.BLUE);
	}


	@AfterClass
	public static void afterClass()
	{
		logEventWatcher.stop();
	}


	@Before
	public void init()
	{
		metisAiFrame = null;
		athenaAiFrame = null;
		metis = new Metis();
		athena = new Athena();
		metisCalled = false;
		athenaCalled = false;
		startCalled = false;
	}


	@After
	public void after()
	{
		logEventWatcher.clear();
	}


	/**
	 * Check if there were error logs during simulation
	 */
	protected void assertNoErrorLog()
	{
		assertThat(logEventWatcher.getEvents(Level.ERROR)).isEmpty();
	}


	/**
	 * Check if there were warning logs during simulation
	 */
	protected void assertNoWarnLog()
	{
		assertThat(logEventWatcher.getEvents(Level.WARN)).isEmpty();
	}


	private void startFrame()
	{
		assertThat(startCalled).isFalse();
		assertThat(metisCalled).isFalse();
		assertThat(athenaCalled).isFalse();

		wfw = new WorldFrameWrapper(swf, refereeMsg, gameState);
		if (prevFrame == null)
		{
			BaseAiFrame bFrame = new BaseAiFrame(wfw, newRefereeMsg, null, EAiTeam.primary(TEAM_COLOR));
			prevFrame = AIInfoFrame.fromBaseAiFrame(bFrame);
		}

		baseAiFrame = new BaseAiFrame(wfw, newRefereeMsg, prevFrame, EAiTeam.primary(TEAM_COLOR));
		newRefereeMsg = false;
		metisCalled = false;
		athenaCalled = false;
		startCalled = true;
	}


	protected void processMetis()
	{
		if (!startCalled)
		{
			startFrame();
		}
		assertThat(startCalled).isTrue();
		assertThat(metisCalled).isFalse();
		assertThat(baseAiFrame).isNotNull();
		metisAiFrame = metis.process(baseAiFrame);
		metisCalled = true;
	}


	protected void processAthena()
	{
		if (!metisCalled)
		{
			processMetis();
		}
		assertThat(startCalled).isTrue();
		assertThat(metisCalled).isTrue();
		assertThat(athenaCalled).isFalse();
		assertThat(baseAiFrame).isNotNull();
		athenaAiFrame = athena.process(metisAiFrame);
		athenaCalled = true;
	}


	protected void finishFrame()
	{
		if (!athenaCalled)
		{
			processAthena();
		}
	}


	protected void nextFrame()
	{
		finishFrame();

		prevFrame.cleanUp();
		prevFrame = AIInfoFrame.builder()
				.baseAiFrame(baseAiFrame)
				.athenaAiFrame(athenaAiFrame)
				.aresData(new AresData())
				.build();

		long timestamp = wfw.getSimpleWorldFrame().getTimestamp() + 16_000_000;
		long frameId = wfw.getSimpleWorldFrame().getFrameNumber() + 1;
		Map<BotID, ITrackedBot> bots = new IdentityHashMap<>();
		wfw.getSimpleWorldFrame().getBots().values().stream()
				.map(tBot -> TrackedBot.newCopyBuilder(tBot).withTimestamp(timestamp).build())
				.forEach(tBot -> bots.put(tBot.getBotId(), tBot));
		ITrackedBall wBall = wfw.getSimpleWorldFrame().getBall();
		TrackedBall ball = TrackedBall.fromBallStateVisible(timestamp, wBall.getState());
		SimpleWorldFrame nextFrame = new SimpleWorldFrame(frameId, timestamp, bots, ball, null, null);
		wfw = new WorldFrameWrapper(nextFrame, refereeMsg, gameState);

		metisCalled = false;
		athenaCalled = false;
		startCalled = false;
		startFrame();
	}


	protected void loadSnapshot(final String snapshotFile) throws IOException
	{
		Snapshot snapshot = Snapshot.loadFromResources(snapshotFile);

		long timestamp = 0;
		Map<BotID, ITrackedBot> bots = new IdentityHashMap<>();
		snapshot.getBots().entrySet().stream()
				.map(entry -> TrackedBot.newBuilder()
						.withBotId(entry.getKey())
						.withTimestamp(timestamp)
						.withPos(entry.getValue().getPos().getXYVector())
						.withVel(entry.getValue().getVel().getXYVector())
						.withOrientation(entry.getValue().getPos().z())
						.withAngularVel(entry.getValue().getVel().z())
						.withLastBallContact(BallContact.def(timestamp))
						.withBotInfo(RobotInfo.stubBuilder(entry.getKey(), timestamp).build())
						.build())
				.forEach(so -> bots.put(so.getBotId(), so));

		IBallTrajectory traj = Geometry.getBallFactory().createTrajectoryFromKickedBallWithoutSpin(
				snapshot.getBall().getPos().getXYVector(),
				snapshot.getBall().getVel().multiplyNew(1000));
		ITrackedBall ball = TrackedBall.fromBallStateVisible(timestamp, traj.getMilliStateAtTime(0));
		swf = new SimpleWorldFrame(0, timestamp, bots, ball, null, null);
	}


	/**
	 * @param refereeMsg new referee command
	 */
	private void setRefereeMsg(final RefereeMsg refereeMsg)
	{
		this.refereeMsg = refereeMsg;
		newRefereeMsg = true;
	}


	/**
	 * @param msg new referee command
	 */
	protected void setRefereeMsg(final SslGcRefereeMessage.Referee msg)
	{
		setRefereeMsg(new RefereeMsg(swf.getTimestamp(), msg));
	}


	protected MetisAiFrame getMetisAiFrame()
	{
		return metisAiFrame;
	}


	protected AthenaAiFrame getAthenaAiFrame()
	{
		return athenaAiFrame;
	}


	protected void setGameState(final GameState gameState)
	{
		this.gameState = gameState;
	}
}
