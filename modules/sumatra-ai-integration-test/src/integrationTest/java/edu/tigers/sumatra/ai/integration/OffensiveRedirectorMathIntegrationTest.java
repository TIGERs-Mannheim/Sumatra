/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.metis.kicking.ChipKickFactory;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.pass.target.IPassTarget;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Testing redirects
 */
public class OffensiveRedirectorMathIntegrationTest extends AAiIntegrationTest
{

	@Test
	public void calcBestRedirectPassTarget1() throws IOException
	{
		loadSnapshot("snapshots/offensiveBestRedirectPass1.snap");
		preTest();

		Optional<IPassTarget> receiver = getMetisAiFrame().getTacticalField().getOffensiveActions().get(
				BotID.createBotId(1, ETeamColor.YELLOW)
		).getPassTarget();

		assertThat(receiver.isPresent()).isTrue();
		receiver.ifPresent(
				iPassTarget -> assertThat(iPassTarget.getBotId()).isEqualTo(BotID.createBotId(4, ETeamColor.YELLOW)));

		Optional<BotID> attacker = getMetisAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot();
		assertThat(attacker.isPresent()).isTrue();
		assertThat(attacker.get()).isEqualTo(BotID.createBotId(1, ETeamColor.YELLOW));

		assertNoErrorLog();
		assertNoWarnLog();
	}


	@Test
	public void calcBestRedirectPassTarget2() throws IOException
	{
		loadSnapshot("snapshots/offensiveBestRedirectPass2.snap");
		preTest();

		var expectedAttacker = BotID.createBotId(1, ETeamColor.YELLOW);
		var expectedReceiver = BotID.createBotId(3, ETeamColor.YELLOW);

		var attacker = getMetisAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot();
		assertThat(attacker).isPresent();
		assertThat(attacker.get()).isEqualTo(expectedAttacker);

		var attackerAction = getMetisAiFrame().getTacticalField().getOffensiveActions().get(expectedAttacker);
		assertThat(attackerAction.getAction()).isEqualTo(EOffensiveAction.PASS);

		var receiverPassTarget = attackerAction.getPassTarget();
		assertThat(receiverPassTarget).isPresent();
		assertThat(receiverPassTarget.get().getBotId()).isEqualTo(expectedReceiver);

		assertNoErrorLog();
		assertNoWarnLog();
	}


	@Test
	public void testIsChipKickRequired() throws IOException
	{
		loadSnapshot("snapshots/offensiveIsChipKickRequired1.snap");
		preTest();

		var chipKick = new ChipKickFactory();
		var allBots = getMetisAiFrame().getWorldFrame().getBots();
		List<ITrackedBot> bots = new ArrayList<>();
		bots.add(allBots.get(BotID.createBotId(3, ETeamColor.BLUE)));

		var source = getAthenaAiFrame().getWorldFrame().getBall().getPos();
		var shootTarget = Vector2f.fromXY(0, 0);
		var kickVel = chipKick.speedToVel(shootTarget.subtractNew(source).getAngle(), 5.0);

		// 2 bots in the path, decent vel -> chip should be required
		assertThat(chipKick.reasonable(source, kickVel, bots)).isTrue();

		kickVel = chipKick.speedToVel(shootTarget.subtractNew(source).getAngle(), 2.0);
		// 2 bots in the path, but speed to slow to overchip -> should be false
		assertThat(chipKick.reasonable(source, kickVel, bots)).isFalse();

		shootTarget = Vector2f.fromXY(3500, 2000);
		kickVel = chipKick.speedToVel(shootTarget.subtractNew(source).getAngle(), 5.0);
		// path to target is free, no chip needed
		assertThat(chipKick.reasonable(source, kickVel, bots)).isFalse();

		shootTarget = Geometry.getGoalTheir().getCenter();
		kickVel = chipKick.speedToVel(shootTarget.subtractNew(source).getAngle(), 5.0);
		// path to target is free, no chip needed
		assertThat(chipKick.reasonable(source, kickVel, bots)).isFalse();

		assertNoErrorLog();
		assertNoWarnLog();
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