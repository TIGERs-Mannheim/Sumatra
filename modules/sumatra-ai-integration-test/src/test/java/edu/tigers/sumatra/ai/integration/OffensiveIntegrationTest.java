/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsgBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveIntegrationTest extends AAiIntegrationTest
{
	
	// private static final Logger log = Logger.getLogger(OffensiveIntegrationTest.class.getName());
	
	
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
		BotID mainOffensive = (new ArrayList<>(
				getMetisAiFrame().getTacticalField().getOffensiveStrategy().getDesiredBots())).get(0);
		Assert.assertTrue(mainOffensive.equals(id));
		
		// also the main offensive bot should be in KickState
		Assert.assertTrue(
				strategy.getCurrentOffensivePlayConfiguration().get(id).equals(OffensiveStrategy.EOffensiveStrategy.KICK));
		
		// in this situation the current strategy should be a direct goal_shot
		OffensiveAction action = getMetisAiFrame().getTacticalField().getOffensiveActions().get(id);
		Assert.assertTrue(action.getType().equals(OffensiveAction.EOffensiveAction.GOAL_SHOT));
		
		setGameState(GameState.STOP);
		nextFrame();
		nextFrame();
		strategy = getMetisAiFrame().getTacticalField().getOffensiveStrategy();
		
		// after switching to stop the current mainBot strategy should be stop !
		Assert.assertTrue(
				strategy.getCurrentOffensivePlayConfiguration().get(id).equals(OffensiveStrategy.EOffensiveStrategy.STOP));
		
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
		BotID mainOffensive = (new ArrayList<>(
				getMetisAiFrame().getTacticalField().getOffensiveStrategy().getDesiredBots())).get(0);
		Assert.assertTrue(mainOffensive.equals(id));
		
		// also the main offensive bot should be in KickState
		// in this situation yellow bot id 1 should be the primary offensive bot.
		OffensiveStrategy strategy = getMetisAiFrame().getTacticalField().getOffensiveStrategy();
		Assert.assertTrue(
				strategy.getCurrentOffensivePlayConfiguration().get(id).equals(OffensiveStrategy.EOffensiveStrategy.KICK));
		
		// in this situation the current strategy should be a pass and the pass receiver should be yellow bot 2
		OffensiveAction action = getMetisAiFrame().getTacticalField().getOffensiveActions().get(id);
		Assert.assertTrue(action.getType().equals(OffensiveAction.EOffensiveAction.PASS));
		BotID passID = BotID.createBotId(2, ETeamColor.YELLOW);
		// Assert.assertTrue(action.getPassTarget().getBotId().equals(passID));
		assertThat(action.getPassTarget().getBotId()).isEqualTo(passID);
		
		// the scoring of the pass target should be greater 0.5, because the goal is free :D
		Assert.assertTrue(action.getPassTarget().getScore() > 0.5);
		
		// the best pass receiving robot should not try to pass to itself :P
		action = getMetisAiFrame().getTacticalField().getOffensiveActions().get(passID);
		Assert.assertFalse(action.getPassTarget().getBotId().equals(passID));
		
		assertNoErrorLog();
		assertNoWarnLog();
	}
}
