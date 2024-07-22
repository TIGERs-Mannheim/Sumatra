/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.moves.AOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.FinisherActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.ForcedPassActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.GoalKickActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.LowChanceGoalKickActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.MoveBallToOpponentHalfActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.ProtectActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.ReceiveBallActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.RedirectGoalKickActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.StandardPassActionMove;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribblingInformation;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Calculates offensive Actions for the OffenseRole.
 */
@Log4j2
@RequiredArgsConstructor
public class OffensiveActionsCalc extends ACalculator
{
	private static final DecimalFormat DF = new DecimalFormat("0.00");
	private static final Color COLOR = Color.magenta;

	@Configurable(defValue = "1.0")
	private static double viabilityMultiplierDirectKick = 1.0;

	@Configurable(defValue = "0.8")
	private static double viabilityMultiplierStandardPass = 0.8;

	@Configurable(defValue = "1.0")
	private static double viabilityMultiplierGoToOtherHalf = 1.0;

	@Configurable(defValue = "0.35")
	private static double impactTimeActionLockThreshold = 0.35;

	@Configurable(defValue = "PROTECT_MOVE")
	private static EOffensiveActionMove forcedOffensiveActionMove = EOffensiveActionMove.PROTECT_MOVE;

	@Configurable(comment = "Forces OffensiveActionsCalc to always activate the actionMove configured in forcedOffensiveActionMove", defValue = "false")
	private static boolean activatedForcedOffensiveMove = false;

	static
	{
		for (EOffensiveActionMove actionMove : EOffensiveActionMove.values())
		{
			ConfigRegistration.registerClass("metis", actionMove.getInstanceableClass().getImpl());
		}
	}

	private final Supplier<EOffensiveStrategy> ballHandlingRobotsStrategy;
	private final Supplier<List<BotID>> ballHandlingBots;
	private final Supplier<Map<KickOrigin, RatedPass>> selectedPasses;
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<OffensiveStatisticsFrame> offensiveStatisticsFrameSupplier;
	private final Supplier<Map<BotID, GoalKick>> bestGoalKickPerBot;
	private final Supplier<GoalKick> bestGoalKick;
	private final Supplier<DribblingInformation> dribblingInformation;
	private final Map<EOffensiveActionMove, AOffensiveActionMove> actionMoves = new EnumMap<>(
			EOffensiveActionMove.class);
	private final Supplier<DribbleToPos> dribbleToPos;
	private final Supplier<Boolean> tigerDribblingBall;
	private final Supplier<BallPossession> ballPossession;
	private final Supplier<Pass> protectPass;

	@Getter
	private Map<BotID, RatedOffensiveAction> offensiveActions;


	@Override
	protected void start()
	{
		register(EOffensiveActionMove.FORCED_PASS, new ForcedPassActionMove(selectedPasses));
		register(EOffensiveActionMove.REDIRECT_GOAL_KICK, new RedirectGoalKickActionMove(bestGoalKickPerBot));
		register(EOffensiveActionMove.FINISHER,
				new FinisherActionMove(opponentClosestToBall, dribblingInformation, tigerDribblingBall));
		register(EOffensiveActionMove.GOAL_KICK, new GoalKickActionMove(bestGoalKick));
		register(EOffensiveActionMove.STANDARD_PASS, new StandardPassActionMove(selectedPasses, kickOrigins));
		register(EOffensiveActionMove.LOW_CHANCE_GOAL_KICK, new LowChanceGoalKickActionMove(bestGoalKickPerBot));
		register(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF,
				new MoveBallToOpponentHalfActionMove(opponentClosestToBall, kickOrigins, ballPossession));
		register(EOffensiveActionMove.RECEIVE_BALL, new ReceiveBallActionMove(selectedPasses, kickOrigins));
		register(EOffensiveActionMove.PROTECT_MOVE, new ProtectActionMove(kickOrigins, dribbleToPos, protectPass));
	}


	private void register(EOffensiveActionMove type, AOffensiveActionMove move)
	{
		actionMoves.put(type, move);
	}


	@Override
	protected void stop()
	{
		actionMoves.clear();
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return ballHandlingRobotsStrategy.get() == EOffensiveStrategy.KICK;
	}


	@Override
	protected void reset()
	{
		offensiveActions = Collections.emptyMap();
	}


	@Override
	public void doCalc()
	{
		var scoreMultipliers = calcScoreMultipliers();
		actionMoves.values().forEach(m -> m.update(getAiFrame()));
		actionMoves.forEach((moveType, actionMove) -> actionMove.setScoreMultiplier(scoreMultipliers.get(moveType)));

		Map<BotID, RatedOffensiveAction> newOffensiveActions = new HashMap<>();
		for (var botId : ballHandlingBots.get())
		{
			var bestAction = findOffensiveActions(botId);
			if (bestAction != null)
			{
				newOffensiveActions.put(botId, bestAction);
				drawAction(botId, bestAction);
			} else
			{
				log.warn("No OffensiveAction found!");
			}
		}
		offensiveActions = Collections.unmodifiableMap(newOffensiveActions);
	}


	private RatedOffensiveAction findOffensiveActions(BotID botId)
	{
		var kickOrigin = kickOrigins.get().get(botId);
		if (kickOrigin != null)
		{
			if (kickOrigin.getImpactTime() < impactTimeActionLockThreshold && offensiveActions.containsKey(botId)
					&& offensiveActions.get(botId).getMove() != EOffensiveActionMove.PROTECT_MOVE)
			{
				return getStableOffensiveAction(botId);
			}

			getShapes(EAiShapesLayer.OFFENSE_ACTION_DEBUG).add(
					getActionUpdateDebugAnnotation(botId, "accepting new action"));

			if (activatedForcedOffensiveMove)
			{
				return actionMoves.get(forcedOffensiveActionMove).calcAction(botId)
						.orElse(actionMoves.get(EOffensiveActionMove.PROTECT_MOVE).calcAction(botId).orElse(null));
			}

			var actions = actionMoves.values().parallelStream().map(m -> m.calcAction(botId)).filter(Optional::isPresent)
					.map(Optional::get).toList();
			drawViabilityMap(botId, actions);

			var bestAction = firstFullyViableAction(actions).or(() -> bestPartiallyViableAction(actions)).orElse(null);
			if (bestAction != null)
			{
				if (OffensiveConstants.isEnableOffensiveStatistics())
				{
					var offensiveStatisticsFrame = offensiveStatisticsFrameSupplier.get();
					var viabilityMap = actions.stream()
							.collect(Collectors.toMap(RatedOffensiveAction::getMove, RatedOffensiveAction::getViability));
					var offensiveBotFrame = offensiveStatisticsFrame.getBotFrames().get(botId);
					offensiveBotFrame.setMoveViabilityMap(viabilityMap);
				}

				return bestAction;
			}
			log.warn("No offensive action has been declared... desperation level > 9000");
		}

		// fallback in case that no action could be found (by intention or by accident)
		// This will happen if there is no kick origin and thus no known point to intercept the ball,
		// so we must first get control of the ball again
		if (getBall().getVel().getLength2() > 0.5)
		{
			// receive action cannot be null
			return actionMoves.get(EOffensiveActionMove.RECEIVE_BALL).calcAction(botId).orElseThrow();
		}

		// low chance kick cannot be null
		return actionMoves.get(EOffensiveActionMove.LOW_CHANCE_GOAL_KICK).calcAction(botId).orElse(null);
	}


	private RatedOffensiveAction getStableOffensiveAction(BotID botId)
	{
		var lastActionMove = offensiveActions.get(botId).getMove();
		var updatedAction = actionMoves.get(lastActionMove).calcAction(botId, true);
		if (updatedAction.isEmpty() || updatedAction.get().getViability().getType() == EActionViability.FALSE)
		{
			// keep old action, since an update is not possible
			getShapes(EAiShapesLayer.OFFENSE_ACTION_DEBUG).add(
					getActionUpdateDebugAnnotation(botId, "keeping old action"));
			return offensiveActions.get(botId);
		}

		// keep the current action, but keep updating it
		getShapes(EAiShapesLayer.OFFENSE_ACTION_DEBUG).add(getActionUpdateDebugAnnotation(botId, "updating old action"));
		return updatedAction.get();
	}


	private DrawableAnnotation getActionUpdateDebugAnnotation(BotID botId, String text)
	{
		return new DrawableAnnotation(getWFrame().getBot(botId).getPos(), text).withOffset(Vector2f.fromXY(-100, 350))
				.setColor(COLOR);
	}


	private void drawViabilityMap(BotID botID, List<RatedOffensiveAction> actions)
	{
		var pos = getWFrame().getBot(botID).getPos();
		var text = actions.stream().map(this::drawViability).collect(Collectors.joining("\n"));
		getShapes(EAiShapesLayer.OFFENSE_ACTION_DEBUG).add(
				new DrawableAnnotation(pos, text).withOffset(Vector2f.fromX(250)).setColor(COLOR));
	}


	private String drawViability(RatedOffensiveAction v)
	{
		var viabilityType = v.getViability().getType().name().charAt(0);
		var score = DF.format(v.getViability().getScore());
		return viabilityType + score + ":" + v.getMove();
	}


	private Map<EOffensiveActionMove, Double> calcScoreMultipliers()
	{
		Map<EOffensiveActionMove, Double> multiplierMap = new EnumMap<>(EOffensiveActionMove.class);
		// Fixed default multipliers
		multiplierMap.put(EOffensiveActionMove.GOAL_KICK, viabilityMultiplierDirectKick);
		multiplierMap.put(EOffensiveActionMove.FINISHER, viabilityMultiplierDirectKick);
		multiplierMap.put(EOffensiveActionMove.LOW_CHANCE_GOAL_KICK, viabilityMultiplierDirectKick);
		multiplierMap.put(EOffensiveActionMove.STANDARD_PASS, viabilityMultiplierStandardPass);
		multiplierMap.put(EOffensiveActionMove.MOVE_BALL_TO_OPPONENT_HALF, viabilityMultiplierGoToOtherHalf);

		Arrays.stream(EOffensiveActionMove.values()).forEach(v -> {
			// Set remaining values to 1.0
			multiplierMap.putIfAbsent(v, 1.0);
		});
		return multiplierMap;
	}


	private void drawAction(BotID botID, RatedOffensiveAction action)
	{
		var bot = getWFrame().getBot(botID);
		final String actionMetadata = action.getMove() + "\n";
		getShapes(EAiShapesLayer.OFFENSE_ACTION).add(
				new DrawableAnnotation(bot.getPos(), actionMetadata, COLOR).withOffset(Vector2f.fromY(150))
						.withCenterHorizontally(true));
	}


	private Optional<RatedOffensiveAction> firstFullyViableAction(List<RatedOffensiveAction> actions)
	{
		return actions.stream().filter(v -> v.getViability().getType() == EActionViability.TRUE)
				// this works because the EnumMap uses the natural ordering of the underlying Enum
				.findFirst();
	}


	private Optional<RatedOffensiveAction> bestPartiallyViableAction(List<RatedOffensiveAction> actions)
	{
		return actions.stream().filter(v -> v.getViability().getType() == EActionViability.PARTIALLY)
				.max(Comparator.comparingDouble(m -> m.getViability().getScore()));
	}
}
