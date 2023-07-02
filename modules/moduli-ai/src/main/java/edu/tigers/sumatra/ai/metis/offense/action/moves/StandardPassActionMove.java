/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Perform a standard pass
 */
@RequiredArgsConstructor
public class StandardPassActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "1000")
	private static double maxPassBackwardsDist = 1000;

	private final Supplier<Map<KickOrigin, RatedPass>> selectedPasses;


	private OffensiveActionViability calcViability(RatedPass pass)
	{
		if (pass == null)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		return new OffensiveActionViability(
				EActionViability.PARTIALLY,
				applyMultiplier(viabilityScoreFor(pass))
		);
	}


	@Override
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		RatedPass ratedPass = findPassForMe(selectedPasses.get(), botId).orElse(null);
		if (ratedPass == null)
		{
			return Optional.empty();
		}

		return Optional.of(RatedOffensiveAction
				.buildPass(EOffensiveActionMove.STANDARD_PASS,
						calcViability(ratedPass),
						ratedPass.getPass()));
	}


	private double viabilityScoreFor(RatedPass pass)
	{
		double sourceX = pass.getPass().getKick().getSource().x();
		double targetX = pass.getPass().getKick().getTarget().x();
		if (sourceX - targetX < maxPassBackwardsDist)
		{
			// only consider INTERCEPTION rating no back pass longer than maxPassBackwardsDist
			return pass.getMaxScore(
					EPassRating.INTERCEPTION,
					EPassRating.REFLECT_GOAL_KICK,
					EPassRating.GOAL_KICK,
					EPassRating.PRESSURE
			);
		}

		return pass.getMaxScore(
				EPassRating.REFLECT_GOAL_KICK,
				EPassRating.GOAL_KICK,
				EPassRating.PRESSURE
		);
	}
}
