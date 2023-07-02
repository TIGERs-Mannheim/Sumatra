/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Kick directly on the opponent goal
 */
@RequiredArgsConstructor
public class GoalKickActionMove extends AOffensiveActionMove
{
	@Configurable(comment = "This is the area around their penalty area where the ball is shot directly", defValue = "50.0")
	private static double defaultMarginAroundPenaltyAreaForDirectShot = 50.0;

	@Configurable(defValue = "0.1")
	private static double minGoalChanceForPartiallyViability = 0.1;

	private final Supplier<Map<BotID, GoalKick>> bestGoalKickPerBot;


	private OffensiveActionViability calcViability(GoalKick goalKick, BotID botId)
	{
		if (goalKick == null)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}

		var ratedTargetScore = Math.min(1,
				goalKick.getRatedTarget().getScore() + getAntiToggleValue(botId, EOffensiveActionMove.GOAL_KICK, 0.1));
		var clearGoalShotChance = ratedTargetScore > OffensiveConstants.getMinBotShouldDoGoalShotScore();

		if (clearGoalShotChance || (isBallVeryCloseToPenaltyArea() && getBall().getVel().getLength2() < 0.5))
		{
			return new OffensiveActionViability(EActionViability.TRUE, applyMultiplier(ratedTargetScore));
		}

		if (ratedTargetScore > minGoalChanceForPartiallyViability)
		{
			return new OffensiveActionViability(EActionViability.PARTIALLY,
					Math.max(0, applyMultiplier(ratedTargetScore) - 1e-3));
		}
		return new OffensiveActionViability(EActionViability.FALSE, 0.0);
	}


	private boolean isBallVeryCloseToPenaltyArea()
	{
		IPenaltyArea penaltyAreaWithMargin = Geometry.getPenaltyAreaTheir()
				.withMargin(defaultMarginAroundPenaltyAreaForDirectShot);
		return penaltyAreaWithMargin.isPointInShape(getBall().getPos());
	}


	@Override
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		var goalKick = bestGoalKickPerBot.get().get(botId);
		if (goalKick == null)
		{
			return Optional.empty();
		}

		Kick kick = goalKick.getKick();
		return Optional.of(RatedOffensiveAction.buildKick(
				EOffensiveActionMove.GOAL_KICK,
				calcViability(goalKick, botId),
				kick));
	}
}
