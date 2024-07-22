/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Hysteresis;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Protect The ball
 */
@RequiredArgsConstructor
public class ProtectActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "0.16")
	private static double minScore = 0.16;

	@Configurable(defValue = "0.28", comment = "Use different minScore is far from our Goal")
	private static double minScoreOffensiveArea = 0.28;

	@Configurable(defValue = "6000.0", comment = "[mm]")
	private static double distanceToOurGoalToBeConsideredOffensiveArea = 6000.0;

	@Configurable(defValue = "true", comment = "Enable protectKick")
	private static boolean enableProtectKick = true;

	@Configurable(defValue = "0.08")
	private static double antiToggleBonus = 0.08;
	private final Hysteresis ballDistanceHysteresis = new Hysteresis(distanceToOurGoalToBeConsideredOffensiveArea - 550,
			distanceToOurGoalToBeConsideredOffensiveArea + 550);

	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	private final Supplier<DribbleToPos> dribbleToPos;

	private final Supplier<Pass> protectPass;


	private OffensiveActionViability calcViability(BotID botId)
	{
		var kickOrigin = kickOrigins.get().get(botId);
		if (!getAiFrame().getGameState().isRunning() || kickOrigin != null && Double.isFinite(kickOrigin.getImpactTime()))
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		// only if running
		// only if the impact time is infinite (ball does not need to be received)
		return new OffensiveActionViability(EActionViability.PARTIALLY, calcViabilityScore(botId));
	}


	@Override
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		if (protectPass.get() != null && enableProtectKick)
		{
			return Optional.of(
					RatedOffensiveAction.buildPass(EOffensiveActionMove.PROTECT_MOVE, calcViability(botId),
							protectPass.get()));
		}
		return Optional.of(RatedOffensiveAction.buildProtect(
				EOffensiveActionMove.PROTECT_MOVE,
				calcViability(botId),
				dribbleToPos.get()));
	}


	private double calcViabilityScore(BotID botId)
	{
		double consideredMinScore = minScore;
		double dist = getWFrame().getBall().getPos().distanceTo(Geometry.getGoalOur().getCenter());
		ballDistanceHysteresis.update(dist);
		if (ballDistanceHysteresis.isUpper())
		{
			consideredMinScore = minScoreOffensiveArea;
		}
		double antiToggleValue =
				getAntiToggleValue(botId, EOffensiveActionMove.PROTECT_MOVE, antiToggleBonus);
		if (getWFrame().getBall().getPos().distanceTo(getWFrame().getBot(botId).getPos()) > Geometry.getBotRadius() * 2)
		{
			antiToggleValue = 0;
		}
		return applyMultiplier(consideredMinScore + antiToggleValue);
	}
}
