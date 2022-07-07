/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;


/**
 * Force a pass during an indirect free kick.
 */
@RequiredArgsConstructor
public class ForcedPassActionMove extends AOffensiveActionMove
{
	private final Supplier<Map<KickOrigin, RatedPass>> selectedPasses;


	private OffensiveActionViability calcViability(RatedPass pass)
	{
		if (getAiFrame().getGameState().isFreeKickForUs() && pass != null)
		{
			return new OffensiveActionViability(EActionViability.TRUE, applyMultiplier(1.0));
		}

		return new OffensiveActionViability(EActionViability.FALSE, 0.0);
	}


	@Override
	public OffensiveAction calcAction(BotID botId)
	{
		RatedPass ratedPass = findPassForMe(selectedPasses.get(), botId).orElse(null);
		return OffensiveAction.builder()
				.move(EOffensiveActionMove.FORCED_PASS)
				.viability(calcViability(ratedPass))
				.pass(ratedPass == null ? null : ratedPass.getPass())
				.build();
	}
}
