/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;


/**
 * Directly kick on the opponent goal even when the chance is low
 */
@RequiredArgsConstructor
public class LowChanceGoalKickActionMove extends AOffensiveActionMove
{
	private static final double MIN_LOW_SCORE_CHANCE = 0.02;

	private final Supplier<Map<BotID, GoalKick>> bestGoalKickTargets;


	private OffensiveActionViability calcViability(GoalKick goalKick)
	{
		if (goalKick == null)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		return new OffensiveActionViability(EActionViability.PARTIALLY, calcViabilityScore(goalKick));
	}


	@Override
	public OffensiveAction calcAction(BotID botId)
	{
		var goalKick = bestGoalKickTargets.get().get(botId);
		return OffensiveAction.builder()
				.move(EOffensiveActionMove.LOW_CHANCE_GOAL_KICK)
				.action(EOffensiveAction.GOAL_SHOT)
				.viability(calcViability(goalKick))
				.kick(goalKick == null ? null : goalKick.getKick())
				.build();
	}


	private double calcViabilityScore(GoalKick goalKick)
	{
		double score = goalKick.getRatedTarget().getScore();
		double margin = Geometry.getBotRadius() * 2 + Geometry.getBallRadius() * 2;
		if (Geometry.getPenaltyAreaTheir().withMargin(margin).isPointInShape(getBall().getPos()))
		{
			score = Math.max(MIN_LOW_SCORE_CHANCE, score);
		}
		return applyMultiplier(score);
	}
}
