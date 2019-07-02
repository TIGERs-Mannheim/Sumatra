/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.metis.general.ChipKickReasonableDecider;
import edu.tigers.sumatra.ai.metis.offense.OffensiveRedirectorMath;
import edu.tigers.sumatra.ai.metis.support.passtarget.IRatedPassTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameState;
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
		
		Optional<IRatedPassTarget> receiver = calcReceiver();
		
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
		
		Optional<IRatedPassTarget> receiver = calcReceiver();
		
		assertThat(receiver.isPresent()).isTrue();
		receiver.ifPresent(
				iPassTarget -> assertThat(iPassTarget.getBotId()).isEqualTo(BotID.createBotId(3, ETeamColor.YELLOW)));
		
		assertNoErrorLog();
		assertNoWarnLog();
	}
	
	
	@Test
	public void testIsChipKickRequired() throws IOException
	{
		loadSnapshot("snapshots/offensiveIsChipKickRequired1.snap");
		preTest();
		
		IVector2 source = getAthenaAiFrame().getWorldFrame().getBall().getPos();
		IVector2 shootTarget = Vector2.fromXY(0, 0);
		ChipKickReasonableDecider chipDecider = new ChipKickReasonableDecider(source,
				shootTarget,
				getMetisAiFrame().getWorldFrame().getBots().values(),
				5.0);
		
		boolean isChipKickRequired = chipDecider.isChipKickReasonable();
		// 2 bots in the path, decent vel -> chip should be required
		assertThat(isChipKickRequired).isTrue();
		
		source = getAthenaAiFrame().getWorldFrame().getBall().getPos();
		shootTarget = Vector2.fromXY(0, 0);
		chipDecider = new ChipKickReasonableDecider(source,
				shootTarget,
				getMetisAiFrame().getWorldFrame().getBots().values(),
				2.0);
		isChipKickRequired = chipDecider.isChipKickReasonable();
		// 2 bots in the path, but speed to slow to overchip -> should be false
		assertThat(isChipKickRequired).isFalse();
		
		shootTarget = Vector2.fromXY(3500, 2000);
		source = getAthenaAiFrame().getWorldFrame().getBall().getPos();
		chipDecider = new ChipKickReasonableDecider(source,
				shootTarget,
				getMetisAiFrame().getWorldFrame().getBots().values(),
				5.0);
		isChipKickRequired = chipDecider.isChipKickReasonable();
		// path to target is free, no chip needed
		assertThat(isChipKickRequired).isFalse();
		
		shootTarget = Geometry.getGoalTheir().getCenter();
		source = getAthenaAiFrame().getWorldFrame().getBall().getPos();
		chipDecider = new ChipKickReasonableDecider(source,
				shootTarget,
				getMetisAiFrame().getWorldFrame().getBots().values(),
				5.0);
		isChipKickRequired = chipDecider.isChipKickReasonable();
		// path to target is free, no chip needed
		assertThat(isChipKickRequired).isFalse();
		
		assertNoErrorLog();
		assertNoWarnLog();
	}
	
	
	private Optional<IRatedPassTarget> calcReceiver()
	{
		ITrackedBot sender = getAthenaAiFrame().getWorldFrame().getTiger(BotID.createBotId(1, ETeamColor.YELLOW));
		Map<BotID, ITrackedBot> botMap = getAthenaAiFrame().getWorldFrame().getTigerBotsAvailable()
				.entrySet().stream()
				.filter(entry -> entry.getKey() != sender.getBotId())
				.filter(entry -> entry.getKey().isBot())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		return redirectorMath.calcBestRedirectPassTarget(getAthenaAiFrame().getWorldFrame(), botMap,
				sender, getAthenaAiFrame().getTacticalField(), null, getAthenaAiFrame());
	}
	
	
	private void preTest()
	{
		setGameState(GameState.RUNNING);
		for (int i = 0; i < 10; i++)
		{
			nextFrame();
		}
	}
}