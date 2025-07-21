/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.integration.blocker.AiSimTimeBlocker;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
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
public class OffensiveGoalShotFullSimIntegrationTest extends AFullSimIntegrationTest implements IAIObserver
{
	@Value
	@Builder
	private static class TestCaseParameters
	{
		String name;
		String snapShotPath;
		BotID expectedShooter;


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
				.name("Close to goal, only keeper (1)")
				.snapShotPath("snapshots/goalShot1.snap")
				.expectedShooter(BotID.createBotId(4, ETeamColor.YELLOW))
				.build());
		snapshots.add(TestCaseParameters.builder()
				.name("Close to goal, only keeper (2)")
				.snapShotPath("snapshots/goalShot2.snap")
				.expectedShooter(BotID.createBotId(3, ETeamColor.YELLOW))
				.build());
		snapshots.add(TestCaseParameters.builder()
				.name("Close to goal, multiple attackers, multiple defenders (3)")
				.snapShotPath("snapshots/goalShot3.snap")
				.expectedShooter(BotID.createBotId(3, ETeamColor.YELLOW))
				.build());

		return snapshots.stream();
	}


	@ParameterizedTest
	@MethodSource("testInput")
	void testGoalKickActionIsSet(TestCaseParameters parameters)
	{
		var snapshot = readSnapshot(parameters.snapShotPath)
				.toBuilder()
				.command(SslGcRefereeMessage.Referee.Command.FORCE_START)
				.build();
		initSimulation(snapshot);

		defaultSimTimeBlocker(0.1)
				.await();

		double timeout = 5;
		double attackerReachedBallLineDuration = defaultSimTimeBlocker(timeout)
				.addStopCondition(new BallLeftFieldStopCondition())
				.addStopCondition(this::ballMoves)
				.addStopCondition(w -> stuck)
				.addHook(aiFrame -> checkAttackerAssignment(aiFrame, parameters.expectedShooter))
				.addHook(this::checkOffensiveActionIsGoalShot)
				.await()
				.getDuration();

		assertNoWarningsOrErrors();

		log.info("time until ball line reached: {}", attackerReachedBallLineDuration);

		testCaseSucceeded = true;
	}


	private boolean ballMoves(AIInfoFrame aiInfoFrame)
	{
		return aiInfoFrame.getWorldFrame().getBall().getVel().getLength() > 0.2;
	}


	private Optional<ITrackedBot> getAttacker(AIInfoFrame frame)
	{
		return frame.getTacticalField().getOffensiveStrategy().getAttackerBot()
				.map(id -> frame.getWorldFrame().getBot(id));
	}


	private void checkOffensiveActionIsGoalShot(AIInfoFrame frame)
	{
		getAttacker(frame).ifPresent(
				e -> assertThat(frame.getTacticalField().getOffensiveActions().get(e.getBotId()).getMove())
						.as("Correct offensive action move should be choosed")
						.isEqualTo(EOffensiveActionMove.GOAL_KICK));
	}


	private void checkAttackerAssignment(AIInfoFrame frame, BotID expectedShooter)
	{
		getAttacker(frame).ifPresent(b -> assertThat(b.getBotId())
				.as("Correct attacker should be selected")
				.isEqualTo(expectedShooter));
	}


	private AiSimTimeBlocker defaultSimTimeBlocker(double maxDuration)
	{
		return new AiSimTimeBlocker(maxDuration).addStopCondition(w -> stuck);
	}
}
