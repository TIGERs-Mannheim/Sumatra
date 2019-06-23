/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Assert;
import org.junit.Test;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsgBuilder;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveIntegrationTest extends AAiIntegrationTest
{
	@Test
	public void testFreeGoalChanceAndStop() throws Exception
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
		Assert.assertEquals(mainOffensive, id);
		
		// also the main offensive bot should be in KickState
		Assert.assertEquals(strategy.getCurrentOffensivePlayConfiguration().get(id), EOffensiveStrategy.KICK);
		
		// in this situation the current strategy should be a direct goal_shot
		OffensiveAction action = getMetisAiFrame().getTacticalField().getOffensiveActions().get(id);
		Assert.assertEquals(action.getAction(), EOffensiveAction.GOAL_SHOT);
		
		setGameState(GameState.STOP);
		nextFrame();
		nextFrame();
		strategy = getMetisAiFrame().getTacticalField().getOffensiveStrategy();
		
		// after switching to stop the current mainBot strategy should be stop !
		Assert.assertEquals(strategy.getCurrentOffensivePlayConfiguration().get(id), EOffensiveStrategy.STOP);
		
		assertNoErrorLog();
		assertNoWarnLog();
	}
	
	
	@Test
	public void testPassingSituation() throws Exception
	{
		loadSnapshot("snapshots/offensiveIntegrationSituation2.snap");
		setGameState(GameState.STOP);
		nextFrame();
		setRefereeMsg(RefereeMsgBuilder.aRefereeMsg().withCommand(Referee.SSL_Referee.Command.NORMAL_START).build());
		setGameState(GameState.RUNNING);
		nextFrame();
		nextFrame();
		nextFrame();
		nextFrame();
		nextFrame();
		
		BotID id = BotID.createBotId(1, ETeamColor.YELLOW);
		BotID mainOffensive = getMetisAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.orElseThrow(AssertionError::new);
		Assert.assertEquals(mainOffensive, id);
		
		// also the main offensive bot should be in KickState
		// in this situation yellow bot id 1 should be the primary offensive bot.
		OffensiveStrategy strategy = getMetisAiFrame().getTacticalField().getOffensiveStrategy();
		Assert.assertEquals(strategy.getCurrentOffensivePlayConfiguration().get(id), EOffensiveStrategy.KICK);
		
		// in this situation the current strategy should be a pass and the pass receiver should be yellow bot 2
		OffensiveAction action = getMetisAiFrame().getTacticalField().getOffensiveActions().get(id);
		Assert.assertEquals(action.getAction(), EOffensiveAction.PASS);
		BotID passID = BotID.createBotId(2, ETeamColor.YELLOW);
		assertThat(action.getPassTarget().orElseThrow(IllegalStateException::new).getBotId()).isEqualTo(passID);
		
		// the scoring of the pass target should be greater 0.5, because the goal is free :D
		assertThat(action.getPassTarget().orElseThrow(IllegalStateException::new).getScore()).isGreaterThan(0.5);
		
		action = getMetisAiFrame().getTacticalField().getOffensiveActions().get(passID);
		Assert.assertNotNull(action);
		
		// the best pass receiving robot should not try to pass to itself
		// but having no passTarget is also okay !
		if (action.getPassTarget().isPresent())
		{
			Assert.assertNotEquals(action.getPassTarget().get().getBotId(), passID);
		}
		
		assertNoErrorLog();
		assertNoWarnLog();
	}
}
