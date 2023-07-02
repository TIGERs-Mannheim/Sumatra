/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Supplier;


/**
 * Get the ball to the opponent half by chipping it forward
 */
@RequiredArgsConstructor
public class MoveBallToOpponentHalfActionMove extends AOffensiveActionMove
{
	@Configurable(comment = "Viability for GoToOtherHalf", defValue = "0.27")
	private static double defaultGoToOtherHalfViability = 0.27;

	@Configurable(comment = "Factor to reduce GoToOtherHalf viability during own kickoff", defValue = "0.5")
	private static double kickoffGoToOtherHalfViabilityFactor = 0.5;

	@Configurable(comment = "X-Value at which this action is activated", defValue = "500.0")
	private static double decisionValueX = 500.0;

	static
	{
		ConfigRegistration.registerClass("metis", MoveBallToOpponentHalfActionMove.class);
	}

	private final Supplier<BotDistance> opponentClosestToBall;

	private final PassFactory passFactory = new PassFactory();


	private OffensiveActionViability calcViability()
	{
		if (getBall().getPos().x() > decisionValueX || getBall().getVel().getLength2() > 0.3)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		if (getAiFrame().getGameState().isKickoffForUs())
		{
			return new OffensiveActionViability(EActionViability.PARTIALLY,
					calcViabilityScore() * kickoffGoToOtherHalfViabilityFactor);
		}
		return new OffensiveActionViability(EActionViability.PARTIALLY, calcViabilityScore());
	}


	@Override
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		passFactory.update(getWFrame());
		passFactory.setAimingTolerance(0.6);
		var target = Geometry.getGoalTheir().getCenter();
		var pass = passFactory.chip(getBall().getPos(), target, botId, BotID.noBot());
		return Optional.of(RatedOffensiveAction.buildPass(
				EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF,
				calcViability(),
				pass));
	}


	private double calcViabilityScore()
	{
		if (opponentClosestToBall.get().getDist() > 1500)
		{
			// only do kick to other half if in panic... otherwise rather wait for a suitable strategy, the protect
			// should take over then
			return 0.01;
		}
		return applyMultiplier(defaultGoToOtherHalfViability);
	}
}
