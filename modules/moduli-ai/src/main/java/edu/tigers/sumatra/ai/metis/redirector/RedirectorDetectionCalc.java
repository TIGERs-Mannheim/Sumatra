/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.redirector;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


/**
 * @author MarkG
 */
@RequiredArgsConstructor
public class RedirectorDetectionCalc extends ACalculator
{
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	@Configurable(defValue = "0.3", comment = "[s]")
	private static double opponentRequiredSlackTime = 0.3;

	@Configurable(comment = "Enable double attacker", defValue = "true")
	private static boolean enableDoubleAttacker = true;

	@Getter
	private RedirectorDetectionInformation redirectorDetectionInformation;


	@Override
	protected boolean isCalculationNecessary()
	{
		// in case of slow balls this calculations are useless
		return getWFrame().getBall().getVel().getLength() > 1.5 &&
				getWFrame().getTigerBotsAvailable().values().stream()
						.noneMatch(e -> e.getBallContact().hasContactFromVisionOrBarrier());
	}


	@Override
	protected void reset()
	{
		redirectorDetectionInformation = new RedirectorDetectionInformation();
	}


	@Override
	public void doCalc()
	{
		redirectorDetectionInformation = new RedirectorDetectionInformation();

		var kickOrigin = kickOrigins.get().entrySet().stream()
				.filter(e -> Double.isFinite(e.getValue().getImpactTime()))
				.min(Comparator.comparingDouble(e -> e.getValue().getImpactTime()))
				.stream().findFirst();

		if (kickOrigin.isEmpty() || Geometry.getPenaltyAreaTheir().withMargin(1500)
				.isPointInShape(kickOrigin.get().getValue().getPos()))
		{
			return;
		}

		determineReceivers(kickOrigin.get());
		characterizeReceiverSituation();
		drawReceiverShapes();
	}


	private void determineReceivers(Map.Entry<BotID, KickOrigin> kickOrigin)
	{
		IVector2 receivePos = kickOrigin.getValue().getPos();
		redirectorDetectionInformation.setFriendlyReceiver(kickOrigin.getKey());
		redirectorDetectionInformation.setFriendlyReceiverPos(receivePos);
		redirectorDetectionInformation.setFriendlyBotReceiving(true);
		if (getBall().getTrajectory().closestPointTo(getWFrame().getBot(kickOrigin.getKey()).getBotKickerPos())
				.distanceTo(getWFrame().getBot(kickOrigin.getKey()).getBotKickerPos()) > Geometry.getBotRadius() * 4)
		{
			redirectorDetectionInformation.setFriendlyStillApproaching(true);
		}

		List<ITrackedBot> consideredOpponentBots = getWFrame().getOpponentBots()
				.values()
				.stream()
				.filter(e -> !e.getBotId().equals(getAiFrame().getKeeperOpponentId()))
				.filter(e -> getBall().getTrajectory().getTravelLineSegment().distanceTo(e.getPos()) < 4000)
				.toList();

		IVector2 startPos = getBall().getPos();
		IVector2 endPos = receivePos;
		var sampleLine = Lines.segmentFromPoints(startPos, endPos);
		List<IVector2> baseSamples = sampleLine.getSteps(300);
		Map<BotID, Pair<IVector2, Double>> bestBotInterceptions = new HashMap<>();
		for (var opponent : consideredOpponentBots)
		{
			List<IVector2> samples = new ArrayList<>(baseSamples);
			samples.add(sampleLine.closestPointOnPath(opponent.getPos()));
			sampleLine.intersect(Lines.halfLineFromDirection(opponent.getPos(), opponent.getVel()))
					.asOptional()
					.ifPresent(samples::add);

			var bestOpponentInterception
					= samples
					.stream()
					.map(e -> Pair.create(e, generateTrajectory(opponent, e).getTotalTime()))
					// check that opponent is faster than ball
					.filter(e -> opponentCanReceiveBall(opponent, e))
					.min(Comparator.comparingDouble(Pair::getSecond));

			bestOpponentInterception.ifPresent(
					iVector2DoublePair -> bestBotInterceptions.put(opponent.getBotId(), iVector2DoublePair));
		}

		var fastestOpponentReceiver = bestBotInterceptions.entrySet().stream()
				.min(Comparator.comparingDouble(e -> e.getValue().getSecond()))
				.stream().findFirst();

		if (fastestOpponentReceiver.isPresent())
		{
			redirectorDetectionInformation.setOpponentReceiverPos(fastestOpponentReceiver.get().getValue().getFirst());
			redirectorDetectionInformation.setOpponentReceiving(true);
			redirectorDetectionInformation.setOpponentReceiver(fastestOpponentReceiver.get().getKey());
		}
	}


	private boolean opponentCanReceiveBall(ITrackedBot opponent, Pair<IVector2, Double> interception)
	{
		if (opponent.getPos().distanceTo(interception.getFirst()) < Geometry.getBotRadius() + Geometry.getBallRadius()
				&& opponent.getVel().getLength() < 0.5)
		{
			// in this case the bot already reached its receive position. Ignore trajectory times.
			return true;
		}
		return getBall().getTrajectory()
				.getTimeByDist(getBall().getPos().distanceTo(interception.getFirst()))
				> interception.getSecond()
				+ opponentRequiredSlackTime; // we assume opponent needs a bit longer to avoid false positives
	}


	private ITrajectory<IVector2> generateTrajectory(ITrackedBot bot, IVector2 dest)
	{
		var moveConstraints = new MoveConstraints(bot.getMoveConstraints()).setPrimaryDirection(Vector2.zero());
		return TrajectoryGenerator.generatePositionTrajectory(moveConstraints, bot.getPos(), bot.getVel(), dest);
	}


	private void characterizeReceiverSituation()
	{
		if (redirectorDetectionInformation.isOpponentReceiving() && redirectorDetectionInformation
				.isFriendlyBotReceiving())
		{
			// both teams want to receive the ball, now characterize more deeply
			redirectorDetectionInformation.setRecommendedAction(determineAction(redirectorDetectionInformation));
		}

		calcCertaintyAndKeepOldActionOnDemand(redirectorDetectionInformation);

		if (redirectorDetectionInformation.isOpponentReceiving() && redirectorDetectionInformation
				.isFriendlyBotReceiving())
		{
			drawInfoShapes(redirectorDetectionInformation);
		}
	}


	private ERecommendedReceiverAction determineAction(RedirectorDetectionInformation information)
	{
		double distFromBallToOpponentReceivePos = getBall().getTrajectory().closestPointTo(
				information.getOpponentReceiverPos()).distanceTo(getBall().getPos());
		double distFromBallToFriendlyReceivePos = getBall().getTrajectory().closestPointTo(
				information.getFriendlyReceiverPos()).distanceTo(getBall().getPos());

		double timeToImpactOpponent = getBall().getTrajectory().getTimeByDist(distFromBallToOpponentReceivePos);
		double timeToImpactFriendly = getBall().getTrajectory().getTimeByDist(distFromBallToFriendlyReceivePos);
		information.setTimeToImpactToOpponent(timeToImpactOpponent);
		information.setTimeToImpactToFriendlyBot(timeToImpactFriendly);

		if (distFromBallToOpponentReceivePos - Geometry.getBotRadius() < distFromBallToFriendlyReceivePos)
		{
			if ((information.isFriendlyStillApproaching() || !opponentReachedPosition())
					&& information.getFriendlyReceiver() != null)
			{
				if (enableDoubleAttacker)
				{
					return ERecommendedReceiverAction.DOUBLE_ATTACKER;
				} else
				{
					return ERecommendedReceiverAction.NONE;
				}
			}
			return ERecommendedReceiverAction.DISRUPT_OPPONENT;
		}
		return ERecommendedReceiverAction.NONE;
	}


	private boolean opponentReachedPosition()
	{
		var opponent = getWFrame().getOpponentBot(redirectorDetectionInformation.getOpponentReceiver());
		var opponentReceivePos = redirectorDetectionInformation.getOpponentReceiverPos();
		return opponentReceivePos.distanceTo(opponent.getPos()) < Geometry.getBotRadius() * 2.0
				&& opponent.getVel().getLength() < 0.5;
	}


	private void drawInfoShapes(RedirectorDetectionInformation information)
	{
		DrawableAnnotation df = new DrawableAnnotation(
				information.getOpponentReceiverPos().addNew(information.getFriendlyReceiverPos()).multiplyNew(0.5),
				"TimeToImpactFriendly: " + information.getTimeToImpactToFriendlyBot() + "\n" +
						"TimeToImpactOpponent: " + information.getTimeToImpactToOpponent() + "\n" +
						"RecAction: " + information.getRecommendedAction() + "\n" +
						"certainty: " + information.getCertainty())
				.withOffset(Vector2.fromXY(-200, 0));
		getShapes(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(df);
	}


	private void calcCertaintyAndKeepOldActionOnDemand(RedirectorDetectionInformation information)
	{
		var prevInfo = getAiFrame().getPrevFrame().getTacticalField().getRedirectorDetectionInformation();
		var prevAction = prevInfo.getRecommendedAction();
		var certainty = calcCertainty(information.getRecommendedAction(), prevAction, prevInfo.getCertainty());
		if (certainty > 0.5)
		{
			keepOldStateIfNotSet(information, prevInfo);
		}

		information.setCertainty(certainty);

		drawReceiverPos(information, certainty);
	}


	private double calcCertainty(
			ERecommendedReceiverAction action,
			ERecommendedReceiverAction previousAction,
			double previousCertainty
	)
	{
		if (action == ERecommendedReceiverAction.NONE)
		{
			return SumatraMath.cap(previousCertainty - 0.05, 0, 1);
		}
		if (action == previousAction)
		{
			return SumatraMath.cap(previousCertainty * 1.1 + 0.1, 0, 1);
		}
		return previousCertainty * 0.9;
	}


	private void keepOldStateIfNotSet(
			RedirectorDetectionInformation information,
			RedirectorDetectionInformation prevInfoformation
	)
	{
		if (information.getRecommendedAction() == ERecommendedReceiverAction.NONE)
		{
			information.setRecommendedAction(prevInfoformation.getRecommendedAction());
		}

		if (!information.isFriendlyBotReceiving())
		{
			information.setFriendlyReceiver(prevInfoformation.getFriendlyReceiver());
			information.setFriendlyReceiverPos(prevInfoformation.getFriendlyReceiverPos());
			information.setFriendlyBotReceiving(true);
		}
		if (!information.isOpponentReceiving())
		{
			information.setOpponentReceiver(prevInfoformation.getOpponentReceiver());
			information.setOpponentReceiverPos(prevInfoformation.getOpponentReceiverPos());
			information.setOpponentReceiving(true);
		}
	}


	private void drawReceiverPos(final RedirectorDetectionInformation information, final double certainty)
	{
		DrawableCircle dc;
		if (information.isOpponentReceiving() && information.isFriendlyBotReceiving())
		{
			dc = new DrawableCircle(Circle.createCircle(
					information.getOpponentReceiverPos().addNew(information.getFriendlyReceiverPos()).multiplyNew(0.5),
					2 + 500 * certainty));
			dc.setColor(new Color((int) (255 * certainty), 57, 0, (int) (100 * certainty)));
		} else
		{

			dc = new DrawableCircle(Circle.createCircle(
					Vector2.fromXY(0, 0), 2 + 500 * certainty));
			dc.setColor(new Color(0, 255, 200, 100));
		}
		dc.setFill(true);
		getShapes(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(dc);
	}


	private void drawReceiverShapes()
	{
		if (redirectorDetectionInformation.isOpponentReceiving())
		{
			// draw receiving shapes
			DrawableCircle dc = new DrawableCircle(
					Circle.createCircle(redirectorDetectionInformation.getOpponentReceiverPos(), 150),
					new Color(0, 113, 214, 152));
			dc.setFill(true);
			DrawableAnnotation da = new DrawableAnnotation(redirectorDetectionInformation.getOpponentReceiverPos(),
					"Opponent is receiving");

			getShapes(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(dc);
			getShapes(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(da);
		}

		if (redirectorDetectionInformation.isFriendlyBotReceiving())
		{
			// draw receiving shapes
			DrawableCircle dc = new DrawableCircle(
					Circle.createCircle(redirectorDetectionInformation.getFriendlyReceiverPos(), 150),
					new Color(0, 113, 214, 152));
			dc.setFill(true);
			DrawableAnnotation da = new DrawableAnnotation(redirectorDetectionInformation.getFriendlyReceiverPos(),
					"Friendly is receiving");

			getShapes(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(dc);
			getShapes(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(da);
		}
	}
}
