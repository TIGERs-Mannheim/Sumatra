/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public class SupportIntegrationTest extends AAiIntegrationTest
{
	@Test
	public void testMoveOutOfGoalSight() throws Exception
	{
		loadSnapshot("snapshots/SupportMoveOutOfGoalSight.snap");
		setGameState(GameState.RUNNING);
		nextFrame();
		nextFrame();
		nextFrame();
		nextFrame();
		nextFrame();
		nextFrame();
		ITrackedBot bot = getAthenaAiFrame().getWorldFrame().getBot(BotID.createBotId(7, ETeamColor.YELLOW));
		SupportRole role = (SupportRole) getAthenaAiFrame().getPlayStrategy().getActiveRoles(ERole.SUPPORT).stream()
				.filter(r -> r.getBotID() == bot.getBotId()).findFirst().orElseThrow(IllegalStateException::new);
		IVector2 destination = role.getCurrentSkill().getMoveCon().getDestination();
		
		
		assertThat(Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius()).isPointInShape(destination))
				.isFalse();
		
		
		BotID offensive = getMetisAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.orElse(BotID.noBot());
		
		Triangle goalTriangle = Triangle.fromCorners(getAthenaAiFrame().getWorldFrame().getBot(offensive).getPos(),
				Geometry.getGoalTheir().getRightPost(), Geometry.getGoalTheir().getLeftPost());
		
		assertThat(goalTriangle.withMargin(Geometry.getBotRadius()).isPointInShape(destination)).isFalse();
	}
}
