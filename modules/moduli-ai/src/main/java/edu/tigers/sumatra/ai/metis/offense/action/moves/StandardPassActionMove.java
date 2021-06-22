/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;


/**
 * Perform a standard pass
 */
@RequiredArgsConstructor
public class StandardPassActionMove extends AOffensiveActionMove
{
	private final Supplier<Map<KickOrigin, RatedPass>> selectedPasses;


	private OffensiveActionViability calcViability(RatedPass pass)
	{
		if (pass == null)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		return new OffensiveActionViability(EActionViability.PARTIALLY, viabilityScoreFor(pass));
	}


	@Override
	public OffensiveAction calcAction(BotID botId)
	{
		RatedPass ratedPass = findPassForMe(selectedPasses.get(), botId).orElse(null);
		return OffensiveAction.builder()
				.move(EOffensiveActionMove.STANDARD_PASS)
				.action(EOffensiveAction.PASS)
				.viability(calcViability(ratedPass))
				.pass(ratedPass == null ? null : ratedPass.getPass())
				.build();
	}


	private double viabilityScoreFor(RatedPass pass)
	{
		double goalKickScore = pass
				.getMaxScore(EPassRating.REFLECT_GOAL_KICK, EPassRating.GOAL_KICK, EPassRating.PRESSURE);
		return applyMultiplier(goalKickScore);
	}
}
