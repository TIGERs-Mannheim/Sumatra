/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Protect The ball
 */
@RequiredArgsConstructor
public class ProtectActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "0.26")
	private static double minScore = 0.26;

	@Configurable(defValue = "0.05")
	private static double antiToggleBonus = 0.05;

	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	private final Supplier<DribbleToPos> dribbleToPos;

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
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		return Optional.of(RatedOffensiveAction.buildProtect(
				EOffensiveActionMove.PROTECT_MOVE,
				calcViability(botId),
				dribbleToPos.get()));
	}


	private double calcViabilityScore(BotID botId)
	{
		return applyMultiplier(minScore + getAntiToggleValue(botId, EOffensiveActionMove.PROTECT_MOVE, antiToggleBonus));
	}
}
