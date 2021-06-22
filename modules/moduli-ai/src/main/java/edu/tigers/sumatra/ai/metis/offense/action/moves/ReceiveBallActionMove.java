/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;


/**
 * This is needed as a bridge between a pass and other moves
 */
@RequiredArgsConstructor
public class ReceiveBallActionMove extends AOffensiveActionMove
{
	private final Supplier<Map<KickOrigin, RatedPass>> selectedPasses;
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;


	private OffensiveActionViability calcViability(KickOrigin kickOrigin)
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
		return new OffensiveActionViability(EActionViability.PARTIALLY, 0.0);
	}


	@Override
	public OffensiveAction calcAction(BotID botId)
	{
		var kickOrigin = kickOrigins.get().get(botId);
		return OffensiveAction.builder()
				.move(EOffensiveActionMove.RECEIVE_BALL)
				.action(EOffensiveAction.RECEIVE)
				.viability(calcViability(kickOrigin))
				.ballContactPos(kickOrigin == null ? null : kickOrigin.getPos())
				.build();
	}
}
