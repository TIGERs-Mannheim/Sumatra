/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
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

	@Configurable(defValue = "0.17")
	private static double antiToggleBonus = 0.17;

	@Configurable(defValue = "10.0", comment = "[deg] orientation considered small enough to force pass")
	private static double orientationThresholdAngle = 10;

	private final Supplier<Map<KickOrigin, RatedPass>> selectedPasses;
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;


	private boolean ballMoves(BotID botID)
	{
		var kickOrigin = kickOrigins.get().get(botID);
		if (kickOrigin == null || kickOrigin.isReached())
		{
			return false;
		}
		return getBall().getVel().getLength2() > OffensiveConstants.getBallIsRollingThreshold();
	}


	private OffensiveActionViability calcViability(RatedPass pass, BotID botId)
	{
		if (pass == null)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		return new OffensiveActionViability(
				EActionViability.PARTIALLY, applyMultiplier(
				viabilityScoreFor(pass) + getAntiToggleValue(botId, EOffensiveActionMove.STANDARD_PASS, antiToggleBonus))
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

		var viability = calcViability(ratedPass, botId);
		if (ballMoves(botId) && ratedPass.getPass().getReceiveMode() == EBallReceiveMode.RECEIVE)
		{
			return Optional.of(
					RatedOffensiveAction.buildReceive(
							EOffensiveActionMove.STANDARD_PASS, viability,
							ratedPass.getPass().getKick().getSource()
					));
		}

		var bot = getWFrame().getBot(botId);
		var target = ratedPass.getPass().getKick().getTarget();

		double angleDiff = AngleMath.diffAbs(bot.getOrientation(), target.subtractNew(bot.getBotKickerPos()).getAngle());
		boolean correctOrientation = angleDiff < AngleMath.deg2rad(orientationThresholdAngle);
		double score = viabilityScoreFor(ratedPass);
		if (correctOrientation && bot.getBallContact().hasContact() && score >= 1.0)
		{
			viability = new OffensiveActionViability(EActionViability.TRUE, score);
		}

		return Optional.of(
				RatedOffensiveAction.buildPass(
						EOffensiveActionMove.STANDARD_PASS, viability,
						ratedPass.getPass()
				));
	}


	private double viabilityScoreFor(RatedPass pass)
	{
		return pass.getMaxScore(
				EPassRating.REFLECT_GOAL_KICK, EPassRating.GOAL_KICK, EPassRating.PRESSURE, EPassRating.FINISHER);
	}
}
