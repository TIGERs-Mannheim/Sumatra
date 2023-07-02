/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Kick the ball away fast to clear a dangerous situation
 */
@RequiredArgsConstructor
public class ClearingKickActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "2.5")
	private static double ballSpeedAtTarget = 2.5;
	@Configurable(defValue = "0.6")
	private static double aimingTolerance = 0.6;
	@Configurable(comment = "[mm] No opponent closer than this to the ball -> it is not dangerous", defValue = "500.0")
	private static double opponentIsCloseDistance = 500.0;
	@Configurable(comment = "[mm] Ball must be closer to our goal than this", defValue = "5000.0")
	private static double ballDangerousDistance = 5000.0;
	@Configurable(comment = "[mm] Distance to kickTarget ", defValue = "3000.0")
	private static double kickDistance = 3000.0;
	private final Hysteresis ballDistanceHysteresis = new Hysteresis(ballDangerousDistance - 250,
			ballDangerousDistance + 250);

	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	private final PassFactory passFactory = new PassFactory();

	private final Supplier<Boolean> ballDefenseIsReady;


	private OffensiveActionViability calcViability()
	{
		if (getBall().getPos().x() > 0 || getBall().getVel().getLength2() > 0.3)
		{
			// No clearing kick in opponents half, chance of shooting the ball out of field is too high.
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		if (isClearingShotNeeded())
		{
			return new OffensiveActionViability(EActionViability.TRUE, 1.0);
		}
		return new OffensiveActionViability(EActionViability.FALSE, 0.0);
	}


	@Override
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		var kickOrigin = kickOrigins.get().get(botId);
		if (kickOrigin == null)
		{
			// ball must be intercepted, but this bot cant
			return Optional.empty();
		}

		IVector2 origin = kickOrigin.getPos();
		IVector2 clearingDir = origin.subtractNew(Geometry.getGoalOur().getCenter()).scaleToNew(kickDistance);
		IVector2 midTarget = origin.addNew(clearingDir);

		var pass = generateChipKick(botId, origin, midTarget, ballSpeedAtTarget, aimingTolerance);

		return Optional.of(RatedOffensiveAction.buildPass(
				EOffensiveActionMove.CLEARING_KICK,
				calcViability(),
				pass));
	}


	private Pass generateChipKick(BotID botId, IVector2 source, IVector2 target, double ballSpeedAtTarget,
			double aimingTolerance)
	{
		passFactory.update(getWFrame());
		passFactory.setMaxReceivingBallSpeed(ballSpeedAtTarget);
		passFactory.setAimingTolerance(aimingTolerance);
		return passFactory.chip(source, target, botId, BotID.noBot());
	}


	private boolean isClearingShotNeeded()
	{
		return !ballDefenseIsReady.get() && isBallDangerous();
	}


	private boolean isBallCloseToGoal()
	{
		ballDistanceHysteresis.setLowerThreshold(ballDangerousDistance - 250);
		ballDistanceHysteresis.setUpperThreshold(ballDangerousDistance + 250);
		ballDistanceHysteresis.update(Geometry.getGoalOur().getGoalLine().distanceTo(getBall().getPos()));
		return ballDistanceHysteresis.isLower();
	}


	private boolean isBallDangerous()
	{
		return isOpponentCloseToBall() && isBallCloseToGoal();
	}


	private boolean isOpponentCloseToBall()
	{
		return opponentClosestToBall.get().getDist() < opponentIsCloseDistance;
	}
}
