/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsgBuilder;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveIntegrationTest extends AAiIntegrationTest
{
	@Test
	public void testFreeGoalChanceAndStop() throws IOException
	{
		loadSnapshot("snapshots/offensiveIntegrationSituation1.snap");
		setGameState(GameState.RUNNING);
		nextFrame();
		nextFrame();
		nextFrame();
		OffensiveStrategy strategy = getMetisAiFrame().getTacticalField().getOffensiveStrategy();

		// in this situation yellow bot id 1 should be the primary offensive bot.
		BotID id = BotID.createBotId(1, ETeamColor.YELLOW);
		BotID mainOffensive = getMetisAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.orElseThrow(AssertionError::new);
		assertThat(mainOffensive).isEqualTo(id);

		// also the main offensive bot should be in KickState
		assertThat(strategy.getCurrentOffensivePlayConfiguration().get(id)).isEqualTo(EOffensiveStrategy.KICK);

		// in this situation the current strategy should be a direct goal_shot
		RatedOffensiveAction action = getMetisAiFrame().getTacticalField().getOffensiveActions().get(id);
		assertThat(action.getMove()).isEqualTo(EOffensiveActionMove.GOAL_KICK);

		setGameState(GameState.STOP);
		nextFrame();
		nextFrame();
		strategy = getMetisAiFrame().getTacticalField().getOffensiveStrategy();

		// after switching to stop the current mainBot strategy should be stop !
		assertThat(strategy.getCurrentOffensivePlayConfiguration().get(id)).isEqualTo(EOffensiveStrategy.STOP);

		assertNoErrorLog();
		assertNoWarnLog();
	}


	@Test
	public void testPassingSituation() throws IOException
	{
		loadSnapshot("snapshots/offensiveIntegrationSituation2.snap");
		setGameState(GameState.STOP);
		nextFrame();
		setRefereeMsg(
				RefereeMsgBuilder.aRefereeMsg().withCommand(SslGcRefereeMessage.Referee.Command.NORMAL_START).build());
		setGameState(GameState.RUNNING);
		for (var i = 0; i < 10; i++)
		{
			nextFrame();
		}

		var id = BotID.createBotId(1, ETeamColor.YELLOW);
		BotID mainOffensive = getMetisAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.orElseThrow(AssertionError::new);
		assertThat(mainOffensive).isEqualTo(id);

		// also the main offensive bot should be in KickState
		// in this situation yellow bot id 1 should be the primary offensive bot.
		var strategy = getMetisAiFrame().getTacticalField().getOffensiveStrategy();
		assertThat(strategy.getCurrentOffensivePlayConfiguration().get(id)).isEqualTo(EOffensiveStrategy.KICK);

		// in this situation the current strategy should be a pass and the pass receiver should be yellow bot 2
		RatedOffensiveAction action = getMetisAiFrame().getTacticalField().getOffensiveActions().get(id);
		assertThat(action.getMove()).isEqualTo(EOffensiveActionMove.STANDARD_PASS);
		var passID = BotID.createBotId(2, ETeamColor.YELLOW);
		assertThat(action.getAction().getPassTarget().orElseThrow().getBotId()).isEqualTo(passID);

		// the scoring of the pass target should be greater 0.25, because the goal is free :D
		assertThat(action.getViability().getScore()).isGreaterThan(0.25);

		assertNoErrorLog();
		assertNoWarnLog();
	}
}
