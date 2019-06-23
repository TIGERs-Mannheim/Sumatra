/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.math.OffensiveRedirectorMath;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate.ERedirectAction;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Testing OffensiveRedirectorMath
 */
public class OffensiveRedirectorMathIntegrationTest extends AAiIntegrationTest
{
	
	OffensiveRedirectorMath redirectorMath = new OffensiveRedirectorMath();
	
	
	@Test
	public void calcBestRedirectPassTarget1() throws IOException
	{
		loadSnapshot("snapshots/offensiveBestRedirectPass1.snap");
		preTest();
		
		Optional<IPassTarget> receiver = calcReceiver();
		
		assertThat(receiver.isPresent()).isTrue();
		receiver.ifPresent(
				iPassTarget -> assertThat(iPassTarget.getBotId()).isEqualTo(BotID.createBotId(4, ETeamColor.YELLOW)));
		
		assertNoErrorLog();
		assertNoWarnLog();
	}
	
	
	@Test
	public void calcBestRedirectPassTarget2() throws IOException
	{
		loadSnapshot("snapshots/offensiveBestRedirectPass2.snap");
		preTest();
		
		Optional<IPassTarget> receiver = calcReceiver();
		
		assertThat(receiver.isPresent()).isTrue();
		receiver.ifPresent(
				iPassTarget -> assertThat(iPassTarget.getBotId()).isEqualTo(BotID.createBotId(3, ETeamColor.YELLOW)));
		
		assertNoErrorLog();
		assertNoWarnLog();
	}
	
	
	private Optional<IPassTarget> calcReceiver()
	{
		ITrackedBot sender = getAthenaAiFrame().getWorldFrame().getTiger(BotID.createBotId(1, ETeamColor.YELLOW));
		Map<BotID, ITrackedBot> botMap = getAthenaAiFrame().getWorldFrame().getTigerBotsAvailable()
				.entrySet().stream()
				.filter(entry -> entry.getKey() != sender.getBotId())
				.filter(entry -> entry.getKey().isBot())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		return redirectorMath.calcBestRedirectPassTarget(getAthenaAiFrame().getWorldFrame(), botMap,
				sender, getAthenaAiFrame().getTacticalField(), null);
	}
	
	
	private void preTest()
	{
		setGameState(GameState.RUNNING);
		nextFrame();
		nextFrame();
		nextFrame();
	}
	
	
	@Test
	public void checkStateSwitches() throws IOException
	{
		loadSnapshot("snapshots/offensiveStateSwitches1.snap");
		preTest();
		
		ITrackedBot myBot = getAthenaAiFrame().getWorldFrame().getTiger(BotID.createBotId(1, ETeamColor.YELLOW));
		Optional<IPassTarget> passTarget = calcReceiver();
		
		IPassTarget nonOptPassTarget = null;
		if (passTarget.isPresent())
		{
			nonOptPassTarget = passTarget.get();
		}

		ERedirectAction action = redirectorMath.checkStateSwitches(getAthenaAiFrame(),
				ERedirectAction.CATCH, myBot,
				myBot.getPos(), new DynamicPosition(Geometry.getGoalTheir().getCenter()),
				nonOptPassTarget, AVector2.ZERO_VECTOR);

		// check for toggling in this situation, receiver has to CATCH in this situation
		assertThat(action).isEqualTo(ERedirectAction.CATCH);
		nextFrame();
		assertThat(action).isEqualTo(ERedirectAction.CATCH);
		nextFrame();
		assertThat(action).isEqualTo(ERedirectAction.CATCH);
		nextFrame();
		assertThat(action).isEqualTo(ERedirectAction.CATCH);
	}
	
}