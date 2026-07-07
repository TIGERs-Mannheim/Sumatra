package edu.tigers.sumatra.ai.metis.defenseoffensecoordination;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.DefenseThreatRater;
import edu.tigers.sumatra.ai.metis.defense.DesiredDefendersCalcUtil;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePassDisruptionAssignment;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.animated.AnimatedCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Calculates crucial defenders.
 */
@RequiredArgsConstructor
public class CrucialDefenderCalc extends ACalculator
{
	@Configurable(comment = "Radius, in which an opponent next to a ball is considered very dangerous regardless of tiger bots", defValue = "200.0", unit = EConfigUnit.DISTANCE_MM)
	private static double dangerousRadius = 200.0;

	@Configurable(comment = "Hysteresis for bot to ball distance comparison", defValue = "200.0", unit = EConfigUnit.DISTANCE_MM)
	private static double opponentToBallHysteresis = 200.0;

	@Configurable(comment = "Lower minimum distance from ball to goal center to actually assign crucial defenders", defValue = "4500.0", unit = EConfigUnit.DISTANCE_MM)
	private static double minDistanceToGoalCenterLower = 4500.0;
	@Configurable(comment = "Upper minimum distance from ball to goal center to actually assign crucial defenders", defValue = "5500.0", unit = EConfigUnit.DISTANCE_MM)
	private static double minDistanceToGoalCenterUpper = 5500.0;

	@Configurable(comment = "If the BallReceiver threat rating is higher than this, activate crucial defender", defValue = "0.5", unit = EConfigUnit.PERCENTAGE)
	private static double opponentBallReceiverDangerousRating = 0.5;
	@Configurable(comment = "If the BallReceiver threat rating is higher than this and it is close to our PenArea, activate crucial defender", defValue = "0.3", unit = EConfigUnit.PERCENTAGE)
	private static double opponentBallReceiverDangerousRatingWhenClose = 0.25;
	@Configurable(comment = "Distance to our PenArea to switch between normal and close rating to determine if rating is high", defValue = "500.0", unit = EConfigUnit.DISTANCE_MM)
	private static double opponentBallReceiverDangerousDistance = 500.0;

	@Configurable(defValue = "true", comment = "Allow pass disruptor to be crucial defender", unit = EConfigUnit.BOOLEAN)
	private static boolean allowCrucialDisruptor = true;

	private final DesiredDefendersCalcUtil util = new DesiredDefendersCalcUtil();
	private final Hysteresis minDistanceToGoalHysteresis = new Hysteresis(
			minDistanceToGoalCenterLower,
			minDistanceToGoalCenterUpper
	);

	private final Supplier<DefenseBallThreat> defenseBallThreat;
	private final Supplier<Integer> numDefenderForBall;
	private final Supplier<Integer> numDefender;
	private final Supplier<List<BotID>> botsToInterchange;
	private final Supplier<Set<BotID>> crucialOffenders;
	private final Supplier<BotDistance> tigerClosestToBall;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<DefensePassDisruptionAssignment> passDisruptionAssignment;

	private boolean opponentCloseToBall = false;
	@Getter
	private Set<BotID> crucialDefenders;


	@Override
	protected boolean isCalculationNecessary()
	{
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
		drawMinDistanceToGoal();
		var passDisruption = Optional.ofNullable(passDisruptionAssignment.get());

		var cause = getCrucialDefenderCause();
		if (cause == ECrucialDefenderCause.NONE)
		{
			crucialDefenders = Collections.emptySet();
		} else
		{
			getShapes(EAiShapesLayer.DO_COORD_CRUCIAL_DEFENDERS)
					.add(new DrawableAnnotation(
							getBall().getPos().addNew(Vector2.fromY(300)),
							String.format("Crucial Defender cause:%n%s", cause.getCause())
					));

			crucialDefenders = new LinkedHashSet<>();
			if (allowCrucialDisruptor)
			{
				passDisruption
						.filter(DefensePassDisruptionAssignment::isDisruptPreferredOverBlocking)
						.map(DefensePassDisruptionAssignment::getDefenderId)
						.ifPresent(crucialDefenders::add);
			}

			List<BotID> defenderCandidates = crucialDefenderCandidates();
			crucialDefenders.addAll(util.nextBestDefenders(
					defenseBallThreat.get(), defenderCandidates,
					Math.min(numDefenderForBall.get(), numDefender.get())
			));
		}

		crucialDefenders = Collections.unmodifiableSet(crucialDefenders);

		drawCrucialDefenders(crucialDefenders);
	}


	private ECrucialDefenderCause getCrucialDefenderCause()
	{


		if (isOpponentStandardSituation())
		{
			return ECrucialDefenderCause.OPPONENT_STANDARD_SITUATION;
		} else if (isOpponentCloseToBall() && isOpponentCloseToGoal())
		{
			return ECrucialDefenderCause.OPPONENT_CLOSE_TO_BALL_AND_GOAL;
		} else if (isOnlyDefenderBlocking() && isOpponentCloseToGoal())
		{
			return ECrucialDefenderCause.DEFENDER_BLOCKING;
		} else if (isOpponentBallReceiverHighThreat())
		{
			return ECrucialDefenderCause.OPPONENT_BALL_RECEIVER_HIGH_THREAT;
		} else
		{
			return ECrucialDefenderCause.NONE;
		}
	}


	private boolean isOpponentStandardSituation()
	{
		return getAiFrame().getGameState().isStandardSituationForThem() || getAiFrame().getGameState()
				.isNextStandardSituationForThem();
	}


	private boolean isOpponentCloseToGoal()
	{
		double distance = defenseBallThreat.get().getPassReceiver()
				.map(receiver -> Geometry.getGoalOur().getCenter().distanceTo(receiver.getPos()))
				.orElseGet(() -> Geometry.getGoalOur().getCenter().distanceTo(getBall().getPos()));

		minDistanceToGoalHysteresis.setLowerThreshold(minDistanceToGoalCenterLower);
		minDistanceToGoalHysteresis.setUpperThreshold(minDistanceToGoalCenterUpper);
		minDistanceToGoalHysteresis.update(distance);

		return minDistanceToGoalHysteresis.isLower();

	}


	private boolean isOpponentCloseToBall()
	{
		if (defenseBallThreat.get().getPassReceiver().isPresent())
		{
			return true;
		}
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


	private boolean isOnlyDefenderBlocking()
	{
		var dangerousOpponentBot = Optional
				.ofNullable(getWFrame().getOpponentBot(opponentClosestToBall.get().getBotId()));

		if (dangerousOpponentBot.isEmpty())
		{
			return false;
		}

		Set<BotID> lastFrameDefenders = new HashSet<>(Optional
				.ofNullable(getAiFrame().getPrevFrame().getTacticalField().getDesiredBotMap().get(EPlay.DEFENSIVE))
				.orElseGet(Collections::emptySet));

		Optional<BotID> lastFrameKeeper = Optional.ofNullable(getAiFrame().getPrevFrame().getKeeperId());
		lastFrameKeeper.ifPresent(lastFrameDefenders::add);

		var offensePosition = getWFrame().getTigerBotsAvailable().values().stream()
				.filter(bot -> !lastFrameDefenders.contains(bot.getBotId()))
				.map(this::getOffenseBallContactPos)
				.flatMap(Optional::stream)
				.min(Comparator.comparingDouble(pos -> getBall().getPos().distanceToSqr(pos.contactPos)));

		if (offensePosition.isEmpty())
		{
			return true;
		}

		var dangerousOpponentBotBallDistance = dangerousOpponentBot.get().getPos()
				.distanceTo(offensePosition.get().contactPos);
		var tigerToBallDistance = offensePosition.get().botPos.distanceTo(offensePosition.get().contactPos);

		getShapes(EAiShapesLayer.DO_COORD_CRUCIAL_DEFENDERS)
				.add(new DrawableLine(dangerousOpponentBot.get().getPos(), offensePosition.get().contactPos(), Color.blue));

		getShapes(EAiShapesLayer.DO_COORD_CRUCIAL_DEFENDERS)
				.add(new DrawableLine(offensePosition.get().botPos(), offensePosition.get().contactPos(), Color.CYAN));
		return dangerousOpponentBotBallDistance < tigerToBallDistance;
	}


	private Optional<BallContactPos> getOffenseBallContactPos(ITrackedBot bot)
	{
		var ratedAction = getAiFrame().getPrevFrame().getTacticalField().getOffensiveActions().get(bot.getBotId());
		if (ratedAction == null)
		{
			return Optional.empty();
		}
		var action = ratedAction.getAction();
		if (action == null)
		{
			return Optional.empty();
		}
		var ballContactPos = action.getBallContactPos();
		if (ballContactPos == null)
		{
			return Optional.empty();
		}
		return Optional.of(new BallContactPos(ballContactPos, bot.getPos()));
	}


	private boolean isOpponentBallReceiverHighThreat()
	{
		var opponentReceiver = defenseBallThreat.get().getPassReceiver();
		if (opponentReceiver.isPresent())
		{
			var rater = new DefenseThreatRater();
			// Do not use ballThreatPos here, as this is the opponentReceiver pos anyway
			var rating = rater.getThreatRatingOfRobot(getBall().getPos(), opponentReceiver.get(), List.of());
			getShapes(EAiShapesLayer.DO_COORD_CRUCIAL_DEFENDERS)
					.add(new DrawableAnnotation(
							opponentReceiver.get().getPos(),
							String.format("-> Ball <-%nRating: %.2f", rating), Vector2.fromY(200)
					));
			return rating > opponentReceiverMinThreatRating(defenseBallThreat.get().getPos());
		}
		return false;
	}


	private double opponentReceiverMinThreatRating(IVector2 receiverPos)
	{
		var distance = Geometry.getPenaltyAreaOur().withMargin(opponentBallReceiverDangerousDistance)
				.distanceTo(receiverPos);
		if (distance <= 0)
		{
			return opponentBallReceiverDangerousRatingWhenClose;
		} else if (distance >= opponentBallReceiverDangerousDistance)
		{
			return opponentBallReceiverDangerousRating;
		} else
		{
			var ratingDiff = opponentBallReceiverDangerousRatingWhenClose - opponentBallReceiverDangerousRating;
			var factor = 1 - (distance / opponentBallReceiverDangerousDistance);
			return opponentBallReceiverDangerousRating + factor * ratingDiff;

		}
	}


	private void drawMinDistanceToGoal()
	{
		double radius = minDistanceToGoalHysteresis.isLower() ? minDistanceToGoalCenterUpper
				: minDistanceToGoalCenterLower;
		IArc arc = Arc.createArc(
				Geometry.getGoalOur().getCenter(), radius, -AngleMath.DEG_090_IN_RAD,
				AngleMath.DEG_180_IN_RAD
		);
		final DrawableArc drawableArc = new DrawableArc(arc, new Color(255, 0, 0, 80));
		drawableArc.setFill(true);
		getShapes(EAiShapesLayer.DO_COORD_CRUCIAL_DEFENDERS).add(drawableArc);
	}


	private List<BotID> crucialDefenderCandidates()
	{
		return getAiFrame().getWorldFrame().getTigerBotsAvailable().keySet().stream()
				.filter(bot -> !crucialDefenders.contains(bot))
				.filter(bot -> !getAiFrame().getKeeperId().equals(bot))
				.filter(bot -> !crucialOffenders.get().contains(bot))
				.filter(bot -> !botsToInterchange.get().contains(bot))
				.toList();
	}


	private void drawCrucialDefenders(final Set<BotID> desiredDefenders)
	{
		for (BotID id : desiredDefenders)
		{
			ITrackedBot bot = getWFrame().getBot(id);
			getShapes(EAiShapesLayer.DO_COORD_CRUCIAL_DEFENDERS).add(
					AnimatedCircle.aFilledCircleWithShrinkingSize(
							bot.getPos(), 100, 150, 1.0f, new Color(125, 255, 50),
							new Color(125, 255, 50, 100)
					));
		}
	}


	private enum ECrucialDefenderCause
	{
		NONE("None"),
		OPPONENT_CLOSE_TO_BALL_AND_GOAL("Opponent close to the ball and the goal"),
		OPPONENT_STANDARD_SITUATION("Opponent standard situation"),
		DEFENDER_BLOCKING("Defender blocking"),
		OPPONENT_BALL_RECEIVER_HIGH_THREAT("Opponent ball receiver high threat"),
		;
		private final String cause;


		ECrucialDefenderCause(final String cause)
		{
			this.cause = cause;
		}


		public String getCause()
		{
			return cause;
		}
	}

	record BallContactPos(IVector2 contactPos, IVector2 botPos)
	{
	}
}
