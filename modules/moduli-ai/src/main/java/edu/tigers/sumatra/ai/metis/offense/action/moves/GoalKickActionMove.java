/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;


/**
 * Kick directly on the opponent goal
 */
@RequiredArgsConstructor
public class GoalKickActionMove extends AOffensiveActionMove
{
	@Configurable(comment = "This is the area around their penalty area where the ball is shot directly", defValue = "100")
	private static float defaultMarginAroundPenaltyAreaForDirectShot = 100;

	@Configurable(defValue = "0.1")
	private static double minGoalChanceForPartiallyViability = 0.1;

	private final Supplier<Map<BotID, GoalKick>> bestGoalKickTargets;


	private OffensiveActionViability calcViability(GoalKick goalKick)
	{
		if (goalKick == null)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}

		var ratedTargetScore = goalKick.getRatedTarget().getScore();
		var clearGoalShotChance = ratedTargetScore > OffensiveConstants.getMinBotShouldDoGoalShotScore();

		if (clearGoalShotChance || isBallVeryCloseToPenaltyArea())
		{
			return new OffensiveActionViability(EActionViability.TRUE, applyMultiplier(ratedTargetScore));
		}

		if (ratedTargetScore > minGoalChanceForPartiallyViability)
		{
			return new OffensiveActionViability(EActionViability.PARTIALLY, applyMultiplier(ratedTargetScore));
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
	public OffensiveAction calcAction(BotID botId)
	{
		var goalKick = bestGoalKickTargets.get().get(botId);
		IVector2 ballContactPos = null;
		Kick kick = null;
		if (goalKick != null)
		{
			ballContactPos = goalKick.getKickOrigin().isReached() ? null : goalKick.getKickOrigin().getPos();
			kick = goalKick.getKickOrigin().isReached() ? goalKick.getKick() : null;
		}

		return OffensiveAction.builder()
				.move(EOffensiveActionMove.GOAL_KICK)
				.action(EOffensiveAction.GOAL_SHOT)
				.viability(calcViability(goalKick))
				.ballContactPos(ballContactPos)
				.kick(kick)
				.build();
	}
}
