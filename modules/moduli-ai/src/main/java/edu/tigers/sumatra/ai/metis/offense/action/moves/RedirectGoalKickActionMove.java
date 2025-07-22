/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Try scoring a goal with a one touch (redirect) kick
 */
@RequiredArgsConstructor
public class RedirectGoalKickActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "0.25")
	private static double minGoalShotChanceForTrueViability = 0.25;

	@Configurable(defValue = "0.05")
	private static double minGoalShotChanceForPartiallyViability = 0.05;

	@Configurable(defValue = "1.45")
	private static double bonusMultiplierForRedirectsOverDirectKicks = 1.45;

	private final Supplier<Map<BotID, GoalKick>> bestGoalKickTargets;


	private OffensiveActionViability calcViability(BotID botId, GoalKick goalKick)
	{
		if (goalKick == null)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}

		if (goalKick.canBeRedirected())
		{
			var ratedTarget = goalKick.getRatedTarget();
			var score = ratedTarget.getScore();
			var antiToggleScore = getAntiToggleValue(botId, EOffensiveActionMove.REDIRECT_GOAL_KICK, 0.2);
			if (score + antiToggleScore > minGoalShotChanceForTrueViability)
			{
				return new OffensiveActionViability(EActionViability.TRUE, calcViabilityScoreWithBonus(score));
			} else if (score + antiToggleScore > minGoalShotChanceForPartiallyViability)
			{
				return new OffensiveActionViability(EActionViability.PARTIALLY, calcViabilityScoreWithBonus(score));
			}
		}
		return new OffensiveActionViability(EActionViability.FALSE, 0.0);
	}


	@Override
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		var goalKick = bestGoalKickTargets.get().get(botId);
		if (goalKick == null)
		{
			return Optional.empty();
		}

		return Optional.of(RatedOffensiveAction.buildRedirectKick(
				EOffensiveActionMove.REDIRECT_GOAL_KICK,
				calcViability(botId, goalKick),
				goalKick.getKick()));
	}


	private double calcViabilityScoreWithBonus(double score)
	{
		// redirects are favored over directs, because they can be executed faster
		// and redirects do not need to consider rotation times
		return applyMultiplier(score * bonusMultiplierForRedirectsOverDirectKicks);
	}
}
