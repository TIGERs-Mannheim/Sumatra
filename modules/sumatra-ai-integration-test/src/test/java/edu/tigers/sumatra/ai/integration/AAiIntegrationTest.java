/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import static edu.tigers.sumatra.wp.data.BallTrajectoryState.aBallState;
import static edu.tigers.sumatra.wp.data.BallTrajectoryState.aBallStateCopyOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.ares.AresData;
import edu.tigers.sumatra.ai.athena.Athena;
import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.athena.PlayStrategy;
import edu.tigers.sumatra.ai.metis.Metis;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AAiIntegrationTest
{
	private static final String MODULI_CONFIG = "moduli_integration_test.xml";
	
	private Metis metis;
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
	private ETeamColor teamColor = ETeamColor.YELLOW;
	
	private boolean metisCalled;
	private boolean athenaCalled;
	private boolean startCalled;
	
	protected static LogEventWatcher logEventWatcher;
	
	
	@BeforeClass
	public static void beforeClass()
	{
		ConfigRegistration.setDefPath("../../config/");
		logEventWatcher = new LogEventWatcher(Level.WARN, Level.ERROR);
		Logger.getRootLogger().addAppender(logEventWatcher);
		SumatraModel.getInstance().setTestMode(true);
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesSafe(MODULI_CONFIG);
		Geometry.refresh();
	}
	
	
	@AfterClass
	public static void afterClass()
	{
		Logger.getRootLogger().removeAppender(logEventWatcher);
		SumatraModel.getInstance().setTestMode(false);
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
		assertThat(logEventWatcher.getNumEvents(Level.ERROR)).isZero();
	}
	
	
	/**
	 * Check if there were warning logs during simulation
	 */
	protected void assertNoWarnLog()
	{
		assertThat(logEventWatcher.getNumEvents(Level.WARN)).isZero();
	}
	
	
	protected void startFrame()
	{
		assertThat(startCalled).isFalse();
		assertThat(metisCalled).isFalse();
		assertThat(athenaCalled).isFalse();
		
		wfw = new WorldFrameWrapper(swf, refereeMsg, gameState);
		if (prevFrame == null)
		{
			prevFrame = new AIInfoFrame(
					new AthenaAiFrame(
							new MetisAiFrame(
									new BaseAiFrame(wfw, newRefereeMsg, null, EAiTeam.primary(teamColor)),
									new TacticalField()),
							new PlayStrategy(
									new PlayStrategy.Builder())),
					new AresData());
		}
		
		baseAiFrame = new BaseAiFrame(wfw, newRefereeMsg, prevFrame, EAiTeam.primary(teamColor));
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
		prevFrame = new AIInfoFrame(athenaAiFrame, new AresData());
		
		long timestamp = wfw.getSimpleWorldFrame().getTimestamp() + 16_000_000;
		long frameId = wfw.getSimpleWorldFrame().getId() + 1;
		IBotIDMap<ITrackedBot> bots = new BotIDMap<>();
		wfw.getSimpleWorldFrame().getBots().values().stream()
				.map(tBot -> TrackedBot.newCopyBuilder(tBot).withTimestamp(timestamp).build())
				.forEach(tBot -> bots.put(tBot.getBotId(), tBot));
		ITrackedBall wBall = wfw.getSimpleWorldFrame().getBall();
		TrackedBall ball = TrackedBall.fromTrajectoryStateVisible(timestamp,
				aBallStateCopyOf(wBall.getState()).build());
		SimpleWorldFrame swf = new SimpleWorldFrame(bots, ball, null, null, frameId, timestamp);
		wfw = new WorldFrameWrapper(swf, refereeMsg, gameState);
		
		metisCalled = false;
		athenaCalled = false;
		startCalled = false;
		startFrame();
	}
	
	
	protected void loadSnapshot(final String snapshotFile) throws IOException
	{
		Snapshot snapshot = Snapshot.loadFromResources(snapshotFile);
		
		long timestamp = 0;
		IBotIDMap<ITrackedBot> bots = new BotIDMap<>();
		snapshot.getBots().entrySet().stream()
				.map(entry -> TrackedBot.newBuilder()
						.withBotId(entry.getKey())
						.withTimestamp(timestamp)
						.withPos(entry.getValue().getPos().getXYVector())
						.withVel(entry.getValue().getVel().getXYVector())
						.withOrientation(entry.getValue().getPos().z())
						.withAngularVel(entry.getValue().getVel().z())
						.withLastBallContact(0)
						.withBotInfo(RobotInfo.stubBuilder(entry.getKey(), timestamp).build())
						.build())
				.forEach(so -> bots.put(so.getBotId(), so));
		
		ITrackedBall ball = TrackedBall.fromTrajectoryStateVisible(timestamp, aBallState()
				.withPos(snapshot.getBall().getPos())
				.withVel(snapshot.getBall().getVel().multiplyNew(1000))
				.build());
		swf = new SimpleWorldFrame(bots, ball, null, null, 0, timestamp);
	}
	
	
	/**
	 * @param refereeMsg new referee command
	 */
	protected void setRefereeMsg(final RefereeMsg refereeMsg)
	{
		this.refereeMsg = refereeMsg;
		newRefereeMsg = true;
	}
	
	
	/**
	 * @param msg new referee command
	 */
	protected void setRefereeMsg(final Referee.SSL_Referee msg)
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
