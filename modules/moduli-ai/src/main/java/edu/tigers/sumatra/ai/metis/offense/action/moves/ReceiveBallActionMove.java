/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * This is needed as a bridge between a pass and other moves
 */
@RequiredArgsConstructor
public class ReceiveBallActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "0.21")
	private static double minScore = 0.21;

	@Configurable(defValue = "0.05")
	private static double antiToggleBonus = 0.05;

	private final Supplier<Map<KickOrigin, RatedPass>> selectedPasses;
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;


	private OffensiveActionViability calcViability(BotID botId, KickOrigin kickOrigin)
	{
		if (kickOrigin == null || kickOrigin.isReached())
		{
			// No origin means no interception, thus ball can/should not be received
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		var pass = selectedPasses.get().get(kickOrigin);
		if (pass == null)
		{
			// there is a kick origin, but no pass for it -> receive
			return new OffensiveActionViability(EActionViability.TRUE, 1.0);
		}
		return new OffensiveActionViability(EActionViability.PARTIALLY, calcViabilityScore(botId));
	}


	@Override
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		var kickOrigin = kickOrigins.get().get(botId);
		if (kickOrigin == null)
		{
			return Optional.of(RatedOffensiveAction.buildReceive(
					EOffensiveActionMove.RECEIVE_BALL,
					calcViability(botId, null),
					null));
		}

		return Optional.of(RatedOffensiveAction.buildReceive(
				EOffensiveActionMove.RECEIVE_BALL,
				calcViability(botId, kickOrigin),
				kickOrigin.getPos()));
	}


	private double calcViabilityScore(BotID botId)
	{
		return applyMultiplier(minScore + getAntiToggleValue(botId, EOffensiveActionMove.RECEIVE_BALL, antiToggleBonus));
	}
}
