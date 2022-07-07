/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.util.Map;
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


	private OffensiveActionViability calcViability(BotID botId, RatedPass pass)
	{
		if (pass == null)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		return new OffensiveActionViability(
				EActionViability.PARTIALLY,
				applyMultiplier(viabilityScoreFor(botId, pass))
		);
	}


	@Override
	public OffensiveAction calcAction(BotID botId)
	{
		RatedPass ratedPass = findPassForMe(selectedPasses.get(), botId).orElse(null);
		return OffensiveAction.builder()
				.move(EOffensiveActionMove.STANDARD_PASS)
				.viability(calcViability(botId, ratedPass))
				.pass(ratedPass == null ? null : ratedPass.getPass())
				.build();
	}


	private double viabilityScoreFor(BotID botId, RatedPass pass)
	{
		ITrackedBot bot = getWFrame().getBot(botId);
		if (pass.getPass().getKick().getSource().x() - maxPassBackwardsDist > bot.getPos().x())
		{
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
