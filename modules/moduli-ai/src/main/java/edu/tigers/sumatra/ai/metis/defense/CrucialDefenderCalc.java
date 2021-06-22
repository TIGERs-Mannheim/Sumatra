/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.animated.AnimatedCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Calculates crucial defenders.
 */
@RequiredArgsConstructor
public class CrucialDefenderCalc extends ACalculator
{
	@Configurable(comment = "Radius, in which an opponent next to a ball is considered very dangerous regardless of tiger bots", defValue = "200.0")
	private static double dangerousRadius = 200.0;

	@Configurable(comment = "Radius around dangerous opponent in which tigers bots are considered blocking", defValue = "150.0")
	private static double opponentToBotDistance = 150.0;

	@Configurable(comment = "Hysteresis for bot to ball distance comparison", defValue = "200.0")
	private static double opponentToBallHysteresis = 200.0;

	@Configurable(comment = "Lower minimum distance from ball to goal center to actually assign crucial defenders", defValue = "4000.0")
	private static double minDistanceToGoalCenterLower = 4000.0;
	@Configurable(comment = "Upper minimum distance from ball to goal center to actually assign crucial defenders", defValue = "5000.0")
	private static double minDistanceToGoalCenterUpper = 5000.0;

	@Configurable(comment = "If the BallReceiver threat rating is higher than this, activate crucial defender", defValue = "0.5")
	private static double opponentBallReceiverDangerousRating = 0.5;

	private final DesiredDefendersCalcUtil util = new DesiredDefendersCalcUtil();
	private final Hysteresis minDistanceToGoalHysteresis = new Hysteresis(minDistanceToGoalCenterLower,
			minDistanceToGoalCenterUpper);

	private final Supplier<DefenseBallThreat> defenseBallThreat;
	private final Supplier<Integer> numDefenderForBall;
	private final Supplier<Integer> numDefender;
	private final Supplier<List<BotID>> botsToInterchange;
	private final Supplier<Set<BotID>> crucialOffenders;
	private final Supplier<BotDistance> tigerClosestToBall;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<List<DefenseBotThreat>> defenseBotThreats;

	private boolean opponentCloseToBall = false;
	@Getter
	private Set<BotID> crucialDefenders;


	@Override
	protected boolean isCalculationNecessary()
	{
		// avoid conflicts with ball placement during stop
		return getAiFrame().getGameState().isGameRunning();
	}


	@Override
	protected void reset()
	{
		crucialDefenders = Collections.emptySet();
	}


	@Override
	protected void doCalc()
	{
		util.update(getAiFrame());
		minDistanceToGoalHysteresis.setLowerThreshold(minDistanceToGoalCenterLower);
		minDistanceToGoalHysteresis.setUpperThreshold(minDistanceToGoalCenterUpper);
		minDistanceToGoalHysteresis.update(Geometry.getGoalOur().getCenter().distanceTo(getBall().getPos()));

		boolean isOpponentCloseToBall = isOpponentCloseToBall() && !minDistanceToGoalHysteresis.isUpper();

		// Check if no other bots than defense are blocking the opponent's bot

		drawMinDistanceToGoal();

		if (isOpponentStandardSituation()
				|| isOpponentCloseToBall
				|| isOnlyDefenderBlocking()
				|| isOpponentBallReceiverHighThreat())
		{
			List<BotID> defenderCandidates = crucialDefenderCandidates();
			crucialDefenders = util.nextBestDefenders(defenseBallThreat.get(), defenderCandidates,
					Math.min(numDefenderForBall.get(), numDefender.get()));
		} else
		{
			crucialDefenders = Collections.emptySet();
		}

		drawCrucialDefenders(crucialDefenders);
	}


	private boolean isOpponentStandardSituation()
	{
		return getAiFrame().getGameState().isStandardSituationForThem();
	}


	private boolean isOnlyDefenderBlocking()
	{
		var dangerousOpponentBot = Optional
				.ofNullable(getWFrame().getOpponentBot(opponentClosestToBall.get().getBotId()));

		boolean onlyDefendersBlocking = false;

		if (dangerousOpponentBot.isPresent())
		{
			Set<BotID> lastFrameDefenders = new HashSet<>(Optional
					.ofNullable(getAiFrame().getPrevFrame().getTacticalField().getDesiredBotMap().get(EPlay.DEFENSIVE))
					.orElse(Collections.emptySet()));

			Optional<BotID> lastFrameKeeper = Optional.ofNullable(getAiFrame().getPrevFrame().getKeeperId());
			lastFrameKeeper.ifPresent(lastFrameDefenders::add);

			final double opponentX = dangerousOpponentBot.get().getPos().x();
			onlyDefendersBlocking = getWFrame().getTigerBotsAvailable().values().stream()
					.filter(bot -> !lastFrameDefenders.contains(bot.getBotId()))
					.allMatch(bot -> bot.getPos().x() + opponentToBotDistance > opponentX);


			if (onlyDefendersBlocking)
			{
				drawBotBlockingAlarm(dangerousOpponentBot.get());
			}
		}
		return onlyDefendersBlocking;
	}


	private boolean isOpponentBallReceiverHighThreat()
	{
		var opponentReceiver = defenseBallThreat.get().getPassReceiver();
		if (opponentReceiver.isPresent())
		{
			var opponentReceiverThreat = defenseBotThreats.get().stream()
					.filter(threat -> threat.getBotID() == opponentReceiver.get().getBotId()).findAny();
			return opponentReceiverThreat.isPresent()
					&& opponentReceiverThreat.get().getThreatRating() > opponentBallReceiverDangerousRating;
		}
		return false;
	}


	private void drawBotBlockingAlarm(final ITrackedBot nearestOpponentBot)
	{
		final DrawableCircle dangerousBotShape = new DrawableCircle(
				Circle.createCircle(nearestOpponentBot.getPos(), Geometry.getBotRadius() + 10),
				new Color(255, 60, 60, 200));
		dangerousBotShape.setFill(true);
		getShapes(EAiShapesLayer.DEFENSE_CRUCIAL_DEFENDERS).add(dangerousBotShape);

		IVector2 leftPoint = Vector2.fromXY(nearestOpponentBot.getPos().x() - opponentToBotDistance,
				-Geometry.getFieldWidth() / 2);
		IVector2 rightPoint = Vector2.fromXY(nearestOpponentBot.getPos().x() - opponentToBotDistance,
				Geometry.getFieldWidth() / 2);
		final DrawableLine line = new DrawableLine(Lines.segmentFromPoints(leftPoint, rightPoint),
				new Color(255, 60, 60, 255));
		getShapes(EAiShapesLayer.DEFENSE_CRUCIAL_DEFENDERS).add(line);
	}


	private void drawMinDistanceToGoal()
	{
		double radius = minDistanceToGoalHysteresis.isLower() ? minDistanceToGoalCenterUpper
				: minDistanceToGoalCenterLower;
		IArc arc = Arc.createArc(Geometry.getGoalOur().getCenter(), radius, -AngleMath.DEG_090_IN_RAD,
				AngleMath.DEG_180_IN_RAD);
		final DrawableArc drawableArc = new DrawableArc(arc, new Color(255, 0, 0, 80));
		drawableArc.setFill(true);
		getShapes(EAiShapesLayer.DEFENSE_CRUCIAL_DEFENDERS).add(drawableArc);
	}


	private List<BotID> crucialDefenderCandidates()
	{
		return getAiFrame().getWorldFrame().getTigerBotsAvailable().keySet().stream()
				.filter(bot -> !getAiFrame().getKeeperId().equals(bot))
				.filter(bot -> !crucialOffenders.get().contains(bot))
				.filter(bot -> !botsToInterchange.get().contains(bot))
				.collect(Collectors.toList());
	}


	private boolean isOpponentCloseToBall()
	{
		BotDistance tigersToBallDist = tigerClosestToBall.get();
		BotDistance opponentsToBallDist = opponentClosestToBall.get();
		if (!opponentsToBallDist.getBotId().isBot() || !tigersToBallDist.getBotId().isBot())
		{
			return false;
		}
		double hysteresis = opponentCloseToBall ? opponentToBallHysteresis : 0;
		double minOur = tigersToBallDist.getDist();
		double minTheir = opponentsToBallDist.getDist();
		opponentCloseToBall = minTheir < minOur + hysteresis || minTheir < dangerousRadius + hysteresis;
		return opponentCloseToBall;
	}


	private void drawCrucialDefenders(final Set<BotID> desiredDefenders)
	{
		for (BotID id : desiredDefenders)
		{
			ITrackedBot bot = getWFrame().getBot(id);
			getShapes(EAiShapesLayer.DEFENSE_CRUCIAL_DEFENDERS).add(
					AnimatedCircle.aFilledCircleWithShrinkingSize(bot.getPos(), 100, 150, 1.0f, new Color(125, 255, 50),
							new Color(125, 255, 50, 100)));
		}
	}
}
