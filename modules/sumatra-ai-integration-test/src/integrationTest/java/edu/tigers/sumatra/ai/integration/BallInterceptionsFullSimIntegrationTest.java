/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.integration.blocker.AiSimTimeBlocker;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Builder;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Run tests in a full simulation environment
 */
@Log4j2
class BallInterceptionsFullSimIntegrationTest extends AFullSimIntegrationTest implements IAIObserver
{
	@Value
	@Builder
	private static class TestCaseParameters
	{
		String name;
		String snapShotPath;
		BotID expectedCatcher;
		double maxBallReachedTime;


		@Override
		public String toString()
		{
			return name;
		}
	}


	@BeforeEach
	@Override
	public void before(TestInfo testInfo)
	{
		super.before(testInfo);

		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.MATCH_MODE);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.OFF);
		SumatraModel.getInstance().getModule(AAgent.class).addObserver(this);
	}


	private static Stream<TestCaseParameters> testInput()
	{
		List<TestCaseParameters> snapshots = new ArrayList<>();
		snapshots.add(TestCaseParameters.builder()
				.name("standard catch 1")
				.snapShotPath("snapshots/findBestPrimary1.snap")
				.expectedCatcher(BotID.createBotId(1, ETeamColor.YELLOW))
				.maxBallReachedTime(2.6)
				.build());

		snapshots.add(TestCaseParameters.builder()
				.name("move towards slow balls")
				.snapShotPath("snapshots/findBestPrimary2.snap")
				.expectedCatcher(BotID.createBotId(3, ETeamColor.YELLOW))
				.maxBallReachedTime(3.3)
				.build());

		snapshots.add(TestCaseParameters.builder()
				.name("standard catch 2")
				.snapShotPath("snapshots/findBestPrimary3.snap")
				.expectedCatcher(BotID.createBotId(3, ETeamColor.YELLOW))
				.maxBallReachedTime(4.5)
				.build());

		snapshots.add(TestCaseParameters.builder()
				.name("overtake")
				.snapShotPath("snapshots/findBestPrimary4.snap")
				.expectedCatcher(BotID.createBotId(3, ETeamColor.YELLOW))
				.maxBallReachedTime(3.5)
				.build());

		snapshots.add(TestCaseParameters.builder()
				.name("ball through pen area")
				.snapShotPath("snapshots/findBestPrimary5.snap")
				.expectedCatcher(BotID.createBotId(3, ETeamColor.YELLOW))
				.maxBallReachedTime(2.0)
				.build());

		snapshots.add(TestCaseParameters.builder()
				.name("choose bot in front")
				.snapShotPath("snapshots/findBestPrimary6.snap")
				.expectedCatcher(BotID.createBotId(3, ETeamColor.YELLOW))
				.maxBallReachedTime(1.3)
				.build());

		snapshots.add(TestCaseParameters.builder()
				.name("stationary situation 1")
				.snapShotPath("snapshots/findBestPrimary7.snap")
				.expectedCatcher(BotID.createBotId(6, ETeamColor.YELLOW))
				.maxBallReachedTime(2.2)
				.build());

		snapshots.add(TestCaseParameters.builder()
				.name("stationary situation 2")
				.snapShotPath("snapshots/findBestPrimary8.snap")
				.expectedCatcher(BotID.createBotId(6, ETeamColor.YELLOW))
				.maxBallReachedTime(2.2)
				.build());

		snapshots.add(TestCaseParameters.builder()
				.name("bot with wrong high vel should not be picked")
				.snapShotPath("snapshots/findBestPrimary9.snap")
				.expectedCatcher(BotID.createBotId(9, ETeamColor.YELLOW))
				.maxBallReachedTime(2.2)
				.build());

		snapshots.add(TestCaseParameters.builder()
				.name("bot with wrong high vel should not be picked, stationary")
				.snapShotPath("snapshots/findBestPrimary10.snap")
				.expectedCatcher(BotID.createBotId(10, ETeamColor.YELLOW))
				.maxBallReachedTime(2.2)
				.build());

		return snapshots.stream();
	}


	@ParameterizedTest
	@MethodSource("testInput")
	void findBestPrimary(TestCaseParameters testCaseParameters)
	{
		var snapshot = readSnapshot(testCaseParameters.snapShotPath)
				.toBuilder()
				.command(SslGcRefereeMessage.Referee.Command.FORCE_START)
				.build();
		initSimulation(snapshot);

		defaultSimTimeBlocker(0.1)
				.await();

		StabilityStatsCollector stats = new StabilityStatsCollector();
		double timeout = 5;
		double attackerReachedBallLineDuration = defaultSimTimeBlocker(timeout)
				.addStopCondition(new BallLeftFieldStopCondition())
				.addStopCondition(this::attackerReachedBallLine)
				.addHook(stats::updateStats)
				.addHook(this::checkBehaviorWhenNoBallInterceptionTargetSet)
				.addHook(aiFrame -> checkAttackerAssignment(aiFrame, testCaseParameters.expectedCatcher))
				.await()
				.getDuration();

		double ballReachedAttackerDuration = defaultSimTimeBlocker(timeout)
				.addStopCondition(new BallLeftFieldStopCondition())
				.addStopCondition(this::ballReachedAttacker)
				.addHook(stats::updateStats)
				.addHook(this::checkBehaviorWhenNoBallInterceptionTargetSet)
				.await()
				.getDuration();

		assertNoWarningsOrErrors();

		double botTargetStability = stats.botTargetDistanceSum / stats.numOfFrames;
		double orientStability = stats.orientDistanceSum / stats.numOfFrames;

		log.info("time until ball line reached: {}", attackerReachedBallLineDuration);
		log.info("time until ball reached: {}", ballReachedAttackerDuration);
		log.info("bot target stability: {}", botTargetStability);
		log.info("orient stability: {}", orientStability);

		assertThat(attackerReachedBallLineDuration).isLessThan(timeout);
		assertThat(ballReachedAttackerDuration).isLessThan(timeout);

		assertThat(ballReachedAttackerDuration)
				.as("needed time to reach ball: %ss, max is: %ss", ballReachedAttackerDuration,
						testCaseParameters.maxBallReachedTime)
				.isLessThan(testCaseParameters.maxBallReachedTime);

		if (stats.numOfFrames != 0)
		{
			assertThat(botTargetStability)
					.as("Destination should be stable")
					.isLessThan(20);
		}

		testCaseSucceeded = true;
	}


	private Optional<ITrackedBot> getAttacker(AIInfoFrame frame)
	{
		return frame.getTacticalField().getOffensiveStrategy().getAttackerBot()
				.map(id -> frame.getWorldFrame().getBot(id));
	}


	private void checkAttackerAssignment(AIInfoFrame frame, BotID expectedCatcher)
	{
		getAttacker(frame).ifPresent(b -> assertThat(b.getBotId())
				.as("Correct attacker should be selected")
				.isEqualTo(expectedCatcher));
	}


	private void checkBehaviorWhenNoBallInterceptionTargetSet(AIInfoFrame frame)
	{
		getAttacker(frame).ifPresent(bot -> {
			var ballInterceptionTarget = frame.getTacticalField().getBallInterceptions().get(bot.getBotId());
			if (ballInterceptionTarget == null)
			{
				assertThat(frame.getWorldFrame().getBall().getVel().getLength())
						.as("Ball should be slow when there is no ball interception target set")
						.isLessThan(0.55);
			}
		});
	}


	private boolean ballReachedAttacker(AIInfoFrame frame)
	{
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		return getAttacker(frame)
				.map(b -> b.getBotKickerPos().distanceTo(ballPos) < Geometry.getBallRadius() * 3.0)
				.orElse(false);
	}


	private boolean attackerReachedBallLine(AIInfoFrame frame)
	{
		ITrackedBall ball = frame.getWorldFrame().getBall();
		return getAttacker(frame)
				.map(bot -> ball.getTrajectory().closestPointToRolling(bot.getBotKickerPos())
						.distanceTo(bot.getBotKickerPos()) < 50)
				.orElse(false);
	}


	private AiSimTimeBlocker defaultSimTimeBlocker(double maxDuration)
	{
		return new AiSimTimeBlocker(maxDuration).addStopCondition(w -> stuck);
	}


	private class StabilityStatsCollector
	{

		IVector2 previousBotTarget;
		double previousBotOrientation;
		double botTargetDistanceSum;
		double orientDistanceSum;
		int numOfFrames;


		void updateStats(AIInfoFrame frame)
		{
			getAttacker(frame).ifPresent(attacker -> {
				var botTarget = getBotTarget(frame, attacker);
				if (previousBotTarget != null && botTarget.isPresent())
				{
					botTargetDistanceSum += botTarget.get().distanceTo(previousBotTarget);
					orientDistanceSum += Math.abs(previousBotOrientation - attacker.getOrientation());
					numOfFrames++;
				}
				botTarget.ifPresent(iVector2 -> previousBotTarget = iVector2);
				previousBotOrientation = attacker.getOrientation();
			});
		}


		private Optional<IVector2> getBotTarget(AIInfoFrame frame, ITrackedBot attacker)
		{
			return Optional.ofNullable(frame.getTacticalField().getBallInterceptions().get(attacker.getBotId()))
					.map(e -> e.getBallInterception().getPos());
		}
	}
}
