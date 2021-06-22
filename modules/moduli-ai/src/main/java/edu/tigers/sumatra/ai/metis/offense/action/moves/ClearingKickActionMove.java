/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.ChipKickFactory;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.ai.metis.targetrater.MaxAngleKickRater;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
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

	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<Map<BotID, GoalKick>> bestGoalKickTargets;
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	private final ChipKickFactory chipKickFactory = new ChipKickFactory();
	private final PassFactory passFactory = new PassFactory();


	private OffensiveActionViability calcViability(BotID botId)
	{
		if (getBall().getPos().x() > 0 || getBall().getVel().getLength2() > 0.3)
		{
			// No clearing kick far in opponents half, chance of shooting the ball out of field is to high.
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}
		if (isClearingShotNeeded(botId))
		{
			return new OffensiveActionViability(EActionViability.TRUE, 1.0);
		}
		// check probability score here.
		var score = calcDangerScore();
		var viaOut = applyMultiplier(score);
		if (score > 0.6)
		{
			return new OffensiveActionViability(EActionViability.TRUE, viaOut);
		}
		return new OffensiveActionViability(EActionViability.PARTIALLY, viaOut);
	}


	@Override
	public OffensiveAction calcAction(BotID botId)
	{
		var kickOrigin = kickOrigins.get().get(botId);
		if (kickOrigin == null)
		{
			// ball must be intercepted, but this bot cant
			return OffensiveAction.builder()
					.move(EOffensiveActionMove.CLEARING_KICK)
					.action(EOffensiveAction.CLEARING_KICK)
					.viability(new OffensiveActionViability(EActionViability.FALSE, 0.0))
					.build();
		}

		// calculate good target here...
		var bestKickTarget = Optional.ofNullable(bestGoalKickTargets.get().get(botId))
				.map(GoalKick::getKick)
				.map(Kick::getTarget)
				.orElse(Geometry.getGoalTheir().getCenter());

		double danger = calcDangerScore() * 0.3;
		IVector2 origin = kickOrigin.getPos();
		IVector2 clearingDir = origin.subtractNew(Geometry.getGoalOur().getCenter()).scaleToNew(100 * (danger));
		IVector2 targetDir = bestKickTarget.subtractNew(origin).scaleToNew(100 * (1 - danger));
		double distance = 3500;
		IVector2 midDir = clearingDir.addNew(targetDir).scaleToNew(distance);
		IVector2 midTarget = origin.addNew(midDir);
		IVector2 bestClearingTarget = origin.addNew(clearingDir.scaleToNew(distance));

		DrawableLine dlShoot = new DrawableLine(Line.fromPoints(origin, bestKickTarget), Color.orange);
		DrawableLine dlClear = new DrawableLine(Line.fromPoints(origin, bestClearingTarget), Color.orange);
		DrawableLine dlMid = new DrawableLine(Line.fromPoints(origin, midTarget), Color.RED);

		getShapes(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(dlShoot);
		getShapes(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(dlClear);
		getShapes(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(dlMid);

		var aimingTolerance = 0.6;
		var pass = chipOrStraightPass(botId, origin, midTarget, ballSpeedAtTarget, aimingTolerance);

		return OffensiveAction.builder()
				.move(EOffensiveActionMove.CLEARING_KICK)
				.action(EOffensiveAction.CLEARING_KICK)
				.viability(calcViability(botId))
				.pass(pass)
				.build();
	}


	private Pass chipOrStraightPass(BotID botId, IVector2 source, IVector2 target, double ballSpeedAtTarget,
			double aimingTolerance)
	{
		passFactory.update(getWFrame());
		passFactory.setMaxReceivingBallSpeed(ballSpeedAtTarget);
		passFactory.setAimingTolerance(aimingTolerance);
		var straightPass = passFactory.straight(source, target, botId, BotID.noBot());
		var chipPass = passFactory.chip(source, target, botId, BotID.noBot());
		var kickVel = chipPass.getKick().getKickVel();
		var bots = getWFrame().getAllBotsBut(botId).values();
		if (chipKickFactory.reasonable(source, kickVel, bots))
		{
			return chipPass;
		}
		return straightPass;
	}


	private double calcDangerScore()
	{
		// 1 is dangerous
		double opponentDistToBall = Math.max(0.001,
				1 - Math.min(opponentClosestToBall.get().getDist(), 500) / 500.0);

		// 1 is dangerous
		double distToGoal = 1 - Math.min(4000, Geometry.getGoalOur().getCenter()
				.distanceTo(getBall().getPos())) / 4000.0;

		// 1 is good chance for opponent to score goal
		double opponentScoreChance = MaxAngleKickRater.getOpponentScoreChanceWithDefender(
				getWFrame().getTigerBotsVisible().values(),
				getBall().getPos());

		return opponentScoreChance * 0.4 + opponentDistToBall * 0.5 + distToGoal * 0.1;
	}


	private boolean isClearingShotNeeded(BotID botId)
	{
		IVector2 target = Geometry.getGoalOur().getCenter();
		IVector2 botPos = getWFrame().getBot(botId).getPos();
		IVector2 ballPos = getBall().getPos();
		IVector2 ballToTarget = ballPos.subtractNew(target);
		IVector2 behindBall = ballPos.addNew(ballToTarget.normalizeNew().multiplyNew(-650));
		IVector2 normal = ballToTarget.getNormalVector().normalizeNew();
		IVector2 behindBallOff1 = behindBall.addNew(normal.multiplyNew(500));
		IVector2 behindBallOff2 = behindBall.addNew(normal.multiplyNew(-500));
		DrawableTriangle tria = new DrawableTriangle(ballPos, behindBallOff1, behindBallOff2,
				new Color(100, 200, 100, 20));
		tria.setFill(true);
		getShapes(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(tria);

		IVector2 behindBallOff11 = ballPos.addNew(ballPos.subtractNew(behindBallOff1));
		IVector2 behindBallOff12 = ballPos.addNew(ballPos.subtractNew(behindBallOff2));
		DrawableTriangle tria2 = new DrawableTriangle(ballPos, behindBallOff11, behindBallOff12,
				new Color(200, 100, 100, 20));
		tria2.setFill(true);
		getShapes(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(tria2);

		if (!tria.getTriangle().isPointInShape(botPos))
		{
			return false;
		}
		// bot in front of ball
		DrawableCircle dc = new DrawableCircle(Circle.createCircle(botPos, 120), Color.green);
		getShapes(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(dc);
		for (BotID opponent : getWFrame().getOpponentBots().keySet())
		{
			IVector2 enemeyPos = getWFrame().getOpponentBot(opponent).getPos();
			if (tria2.getTriangle().isPointInShape(enemeyPos))
			{
				DrawableCircle dc2 = new DrawableCircle(Circle.createCircle(enemeyPos, 120), Color.red);
				getShapes(EAiShapesLayer.OFFENSIVE_CLEARING_KICK).add(dc2);
				// one or more opponent bot is behind the ball.. maybe ready to shoot on our goal !
				if (ballPos.distanceTo(Geometry.getGoalOur().getCenter()) < 3000)
				{
					// ball on our half of the field
					return true;
				}
			}
		}
		return false;
	}
}
