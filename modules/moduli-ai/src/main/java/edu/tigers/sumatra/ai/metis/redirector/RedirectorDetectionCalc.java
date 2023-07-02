/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.redirector;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author MarkG
 */
public class RedirectorDetectionCalc extends ACalculator
{
	@Getter
	private RedirectorDetectionInformation redirectorDetectionInformation;


	@Override
	protected boolean isCalculationNecessary()
	{
		// in case of slow balls this calculations are useless
		return getWFrame().getBall().getVel().getLength() > 1.0;
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

		detectOpponentPassReceiver();
		detectFriendlyPassReceiver();
		characterizeReceiverSituation();
		drawReceiverShapes();
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
			if (information.isFriendlyStillApproaching() && information.getFriendlyReceiver() != null)
			{
				return ERecommendedReceiverAction.DOUBLE_ATTACKER;
			}
			return ERecommendedReceiverAction.DISRUPT_OPPONENT;
		}
		return ERecommendedReceiverAction.NONE;
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


	private void detectFriendlyPassReceiver()
	{
		// detect friendly pass receiver
		Optional<BotID> attacker = getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy().getAttackerBot();
		Optional<ARole> attackerRole = getAiFrame().getPrevFrame().getPlayStrategy()
				.getActiveRoles(ERole.ATTACKER, ERole.DISRUPT_OPPONENT)
				.stream()
				.findAny();

		if (attackerRole.isPresent() && attacker.isPresent() && attacker.get().equals(attackerRole.get().getBotID()))
		{
			// friendly pass receiver has been found
			attackerRole.get().getBot().getRobotInfo().getTrajectory().ifPresent(
					e -> {
						redirectorDetectionInformation.setFriendlyReceiver(attacker.get());
						redirectorDetectionInformation.setFriendlyReceiverPos(e.getFinalDestination().getXYVector());
						redirectorDetectionInformation.setFriendlyBotReceiving(true);
						if (getBall().getTrajectory().closestPointTo(attackerRole.get().getBot().getBotKickerPos())
								.distanceTo(attackerRole.get().getBot().getBotKickerPos()) > Geometry.getBotRadius() * 4)
						{
							redirectorDetectionInformation.setFriendlyStillApproaching(true);
						}
					});
		}
	}


	private void detectOpponentPassReceiver()
	{
		Map<BotID, ITrackedBot> opponents = getWFrame().getOpponentBots().entrySet().stream()
				.filter(e -> e.getKey() != getAiFrame().getKeeperOpponentId())
				.filter(e -> !Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() * 3.0)
						.isPointInShape(e.getValue()
								.getPos())).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

		BotID opponentReceiver = getBestRedirector(opponents);
		ITrackedBot opponentBot = getWFrame().getOpponentBot(opponentReceiver);
		if (opponentBot != null)
		{
			IVector2 opponentPos = opponentBot.getBotKickerPos();
			IVector2 opponentReceivePos = getBall().getTrajectory().closestPointTo(opponentPos);

			IVector2 velOffset = opponentReceivePos.addNew(opponentBot.getVel().multiplyNew(1000.0));
			IVector2 helper = getBall().getTrajectory().closestPointTo(velOffset);
			double relativeBotSpeed = helper.distanceTo(velOffset) / 1000.0;

			if (opponentPos.distanceTo(opponentReceivePos) < Geometry.getBotRadius()
					&& relativeBotSpeed < 0.5 && opponentPos.distanceTo(getBall().getPos()) > 600
					&& getBall().getHeight() < 50)
			{
				redirectorDetectionInformation.setOpponentReceiverPos(opponentReceivePos);
				redirectorDetectionInformation.setOpponentReceiving(true);
				redirectorDetectionInformation.setOpponentReceiver(opponentReceiver);
			}
		}
	}


	public BotID getBestRedirector(Map<BotID, ITrackedBot> bots)
	{
		IVector2 endPos = getBall().getTrajectory().getPosByVel(0).getXYVector();
		IVector2 ballPos = getBall().getPos();

		List<BotID> filteredBots = getPotentialRedirectors(bots, endPos);

		Optional<BotID> receiver = filteredBots.stream()
				.min((e1, e2) -> (int) (bots.get(e1).getPos().distanceTo(ballPos)
						- bots.get(e2).getPos().distanceTo(ballPos)));

		return receiver.orElse(BotID.noBot());
	}


	private List<BotID> getPotentialRedirectors(Map<BotID, ITrackedBot> bots, IVector2 endPos)
	{
		final double redirectTol = 350;
		IVector2 ballPos = getBall().getPos();

		// input: endpoint, ballVel.vel = endpoint - curPos.getAngle().
		IVector2 ballVel = endPos.subtractNew(ballPos);

		if (ballVel.getLength() < 0.4)
		{
			// no potential redirector
			return Collections.emptyList();
		}

		IVector2 left = Vector2.fromAngle(ballVel.getAngle() - 0.2).normalizeNew();
		IVector2 right = Vector2.fromAngle(ballVel.getAngle() + 0.2).normalizeNew();

		double dist = Math.max(VectorMath.distancePP(ballPos, endPos) - redirectTol, 10);

		IVector2 normal = ballVel.getNormalVector().normalizeNew();
		IVector2 tleft = ballPos.addNew(normal.scaleToNew(160));
		IVector2 tright = ballPos.addNew(normal.scaleToNew(-160));
		IVector2 uleft = tleft.addNew(left.scaleToNew(dist)).addNew(normal.scaleToNew(100));
		IVector2 uright = tright.addNew(right.scaleToNew(dist)).addNew(normal.scaleToNew(-100));

		var tri3 = Triangle.fromCorners(tleft, uleft, uright);
		var tri4 = Triangle.fromCorners(tleft, tright, uright);

		List<BotID> filteredBots = new ArrayList<>();
		for (Map.Entry<BotID, ITrackedBot> entry : bots.entrySet())
		{
			var botID = entry.getKey();
			var tBot = entry.getValue();
			var pos = tBot.getPos();
			if (tri3.isPointInShape(pos) || tri4.isPointInShape(pos))
			{
				filteredBots.add(botID);
			}
		}
		return filteredBots;
	}
}
