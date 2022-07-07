/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.dribble.BallDribbleToPosGenerator;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribblingInformation;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;


/**
 * Protect The ball
 */
@RequiredArgsConstructor
public class ProtectActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "0.21")
	private static double minScore = 0.21;

	@Configurable(defValue = "0.02")
	private static double antiToggleBonus = 0.02;

	private BallDribbleToPosGenerator dribbleGenerator = new BallDribbleToPosGenerator();

	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	private final Supplier<DribblingInformation> dribblingInformation;


	private OffensiveActionViability calcViability(BotID botId)
	{
		var kickOrigin = kickOrigins.get().get(botId);
		if (!getAiFrame().getGameState().isRunning() || kickOrigin != null && Double.isFinite(kickOrigin.getImpactTime()))
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		// only if running
		// only if the impact time is infinite (ball does not need to be received)
		return new OffensiveActionViability(EActionViability.PARTIALLY, calcViabilityScore(botId));
	}


	@Override
	public OffensiveAction calcAction(BotID botId)
	{
		var pos = dribbleGenerator
				.getDribbleToPos(getWFrame(),
						getAiFrame().getPrevFrame().getTacticalField().getOpponentClosestToBall().getBotId(),
						getWFrame().getBot(botId), dribblingInformation.get(),
						getAiFrame().getShapes(EAiShapesLayer.OFFENSIVE_DRIBBLE));
		return OffensiveAction.builder()
				.move(EOffensiveActionMove.PROTECT_MOVE)
				.dribbleToPos(pos)
				.viability(calcViability(botId))
				.build();
	}


	private double calcViabilityScore(BotID botId)
	{
		return applyMultiplier(minScore + getAntiToggleValue(botId, EOffensiveActionMove.PROTECT_MOVE, antiToggleBonus));
	}
}
