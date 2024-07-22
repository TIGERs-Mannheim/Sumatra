/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallControl;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Get the ball to the opponent half by chipping it forward
 */
@RequiredArgsConstructor
public class MoveBallToOpponentHalfActionMove extends AOffensiveActionMove
{
	@Configurable(comment = "Viability for GoToOtherHalf, this value should always he higher than the minScore from ProtectActionMove", defValue = "0.28")
	private static double defaultGoToOtherHalfViability = 0.28;

	@Configurable(comment = "Factor to reduce GoToOtherHalf viability during own kickoff", defValue = "0.5")
	private static double kickoffGoToOtherHalfViabilityFactor = 0.5;

	@Configurable(comment = "X-Value at which this action is activated", defValue = "-500.0")
	private static double decisionValueX = -500.0;

	@Configurable(comment = "[mm] No opponent closer than this to the ball -> it is not dangerous", defValue = "2000.0")
	private static double opponentIsCloseDistance = 2000.0;

	static
	{
		ConfigRegistration.registerClass("metis", MoveBallToOpponentHalfActionMove.class);
	}

	private final Supplier<BotDistance> opponentClosestToBall;

	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	private final Supplier<BallPossession> ballPossession;

	private final PassFactory passFactory = new PassFactory();


	private OffensiveActionViability calcViability(IVector2 kickSource, boolean isRollingBall, IVector2 passReceiver)
	{
		if (isRollingBall && OffensiveMath.getRedirectAngle(getBall().getPos(), passReceiver,
				Geometry.getGoalTheir().getCenter()) > OffensiveConstants.getMaximumReasonableRedirectAngle())
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}

		if (ballPossession.get().getOpponentBallControl() == EBallControl.STRONG)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}

		if (kickSource.x() > decisionValueX)
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
		var kickOrigin = kickOrigins.get().values().stream().findFirst();
		IVector2 kickSource = kickOrigin.map(KickOrigin::getPos).orElse(getBall().getPos());

		passFactory.update(getWFrame());
		passFactory.setAimingTolerance(0.6);

		var target = Geometry.getGoalTheir().getCenter();
		var pass = passFactory.chip(kickSource, target, botId, BotID.noBot(), EBallReceiveMode.DONT_CARE);
		return Optional.of(RatedOffensiveAction.buildPass(
				EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF,
				calcViability(kickSource,
						getBall().getVel().getLength() > OffensiveConstants.getBallIsRollingThreshold(),
						getWFrame().getBot(botId).getPos()), pass));
	}


	private double calcViabilityScore()
	{
		if (opponentClosestToBall.get().getDist() > opponentIsCloseDistance)
		{
			// only do kick to other half if in panic... otherwise rather wait for a suitable strategy, the protect
			// should take over then
			return 0.01;
		}
		return applyMultiplier(defaultGoToOtherHalfViability);
	}
}
