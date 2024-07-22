/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballresponsibility;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.function.Supplier;


/**
 * Determine who is responsible for handling the ball.
 */
@RequiredArgsConstructor
public class BallResponsibilityCalc extends ACalculator
{
	@Configurable(defValue = "200.0")
	private static double marginForNoOffensiveWhenBallNearOurPen = 200;

	@Configurable(defValue = "150.0")
	private static double hysteresisForNoOffensiveWhenBallNearOurPen = 150;

	@Configurable(defValue = "20.0")
	private static double marginForNoOffensiveWhenBallNearOpponentPen = 20;

	@Configurable(defValue = "50.0")
	private static double hysteresisForNoOffensiveWhenBallNearOpponentPen = 50;

	@Configurable(defValue = "false")
	private static boolean noOffensiveWhenBallInOpponentPenArea = false;

	@Configurable(defValue = "1300.0")
	private static double minMarginToOurPenAreaForIntercept = 1300;

	@Configurable(comment = "Distance [mm] from opponent kicker to ball where it gets safe for the keeper to get the ball", defValue = "450")
	private static double opponentBotBallPossessionDistance = 450;

	@Configurable(comment = "Margin [mm] on penalty Area where keeper can still get the ball", defValue = "200")
	private static double getBallPenaltyAreaMargin = 200;

	@Configurable(comment = "Margin [mm] on penalty Area where keeper can safely get the ball", defValue = "-50")
	private static double safeBallPenaltyAreaMargin = -50;
	private final Supplier<BotDistance> opponentClosestToBall;
	@Getter
	private EBallResponsibility ballResponsibility;
	private boolean isDefensiveSituationOld = false;
	private boolean isNearOpponentPenOld = false;


	@Override
	public void doCalc()
	{
		ballResponsibility = newBallResponsibility();

		var hudColor = getWFrame().getTeamColor();
		double posX = 1.0;
		getShapes(EAiShapesLayer.AI_BALL_RESPONSIBILITY).add(
				new DrawableBorderText(Vector2.fromXY(posX, hudColor == ETeamColor.BLUE ? 10.3 : 12.0),
						"Ball Responsibility: " + ballResponsibility)
						.setColor(getAiFrame().getTeamColor().getColor()));
	}


	private EBallResponsibility newBallResponsibility()
	{
		if (isKeeperBall())
		{
			return EBallResponsibility.KEEPER;
		}
		if (ballIsOrWillStopInsidePenaltyArea() || noInterceptorRequired())
		{
			return EBallResponsibility.DEFENSE;
		}
		return EBallResponsibility.OFFENSE;
	}


	private boolean isKeeperBall()
	{

		return Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos())
				&& getWFrame().getBot(getAiFrame().getKeeperId()) != null
				&& ballCanBePassedOutOfPenaltyArea();
	}


	private boolean isBallOnKeeperDribbler()
	{
		var keeperBot = getWFrame().getBot(getAiFrame().getKeeperId());
		return keeperBot.getBallContact().getContactDurationFromVision() > 0.02 || keeperBot.getBallContact()
				.hadRecentContact();
	}


	private boolean ballCanBePassedOutOfPenaltyArea()
	{
		return isBallInPenaltyArea(getBallPenaltyAreaMargin)
				&& isOpponentSafe()
				&& isBallStillOrAtKeeper()
				&& isBotNotBetweenBallAndKeeper();
	}


	private boolean isBallSafe()
	{
		return isBallStillOrAtKeeper() && isBallInPenaltyArea(safeBallPenaltyAreaMargin);
	}


	private boolean isBallInPenaltyArea(final double getBallPenaltyAreaMargin)
	{
		return Geometry.getPenaltyAreaOur().getRectangle().withMargin(getBallPenaltyAreaMargin)
				.isPointInShape(getBall().getPos());
	}


	private boolean isBallStillOrAtKeeper()
	{
		return getBall().getVel().getLength() < 0.1 || isBallOnKeeperDribbler();
	}


	private boolean isBotNotBetweenBallAndKeeper()
	{
		var keeperBot = getWFrame().getBot(getAiFrame().getKeeperId());
		ILineSegment keeperBallLine = Lines.segmentFromPoints(keeperBot.getPos(), getBall().getPos());
		return getWFrame().getBots().values().stream()
				.filter(bot -> !bot.getBotId().equals(keeperBot.getBotId()))
				.map(ITrackedObject::getPos)
				.noneMatch(pos -> keeperBallLine.distanceTo(pos) < Geometry.getBotRadius());
	}


	private boolean isOpponentSafe()
	{
		return isBallSafeFromOpponent() || isOpponentBlockedByDefenders();
	}


	private boolean isBallSafeFromOpponent()
	{
		return isBallSafe() || opponentClosestToBall.get().getDist() > opponentBotBallPossessionDistance;
	}


	private boolean isOpponentBlockedByDefenders()
	{
		var opponentBot = opponentClosestToBall.get().getBotId();
		if (!opponentBot.isBot())
		{
			return false;
		}

		IVector2 closestOpponentKicker = getWFrame().getBot(opponentBot).getBotKickerPos();

		for (IVector2 goalLinePos : Geometry.getGoalOur().getLineSegment().getSteps(Geometry.getBallRadius() * 2))
		{
			ILineSegment testLine = Lines.segmentFromPoints(goalLinePos, closestOpponentKicker);
			boolean lineBlocked = getWFrame().getTigerBotsAvailable().values().stream()
					.filter(bot -> !bot.getBotId().equals(getAiFrame().getKeeperId()))
					.anyMatch(bot -> Circle.createCircle(bot.getPos(), Geometry.getBotRadius() + 5)
							.isIntersectingWithPath(testLine));

			getAiFrame().getShapeMap().get(EAiShapesLayer.KEEPER_BEHAVIOR)
					.add(new DrawableLine(testLine, lineBlocked ? Color.GREEN : Color.RED).setStrokeWidth(1));
			if (!lineBlocked)
			{
				return false;
			}
		}

		return true;
	}


	private boolean ballIsOrWillStopInsidePenaltyArea()
	{
		// check for our penArea
		boolean defSituation;
		double margin = marginForNoOffensiveWhenBallNearOurPen;
		if (isDefensiveSituationOld)
		{
			margin += hysteresisForNoOffensiveWhenBallNearOurPen;
		}
		defSituation = ballStaysInsidePenArea(Geometry.getPenaltyAreaOur().withMargin(margin));
		isDefensiveSituationOld = defSituation;

		// check for opponent penArea.
		boolean inOpponentPen = false;
		if (noOffensiveWhenBallInOpponentPenArea)
		{
			margin = marginForNoOffensiveWhenBallNearOpponentPen;
			if (isNearOpponentPenOld)
			{
				margin += hysteresisForNoOffensiveWhenBallNearOpponentPen;
			}
			inOpponentPen = ballStaysInsidePenArea(Geometry.getPenaltyAreaTheir().withMargin(margin));
			isNearOpponentPenOld = inOpponentPen;
		}

		return defSituation || inOpponentPen;
	}


	private boolean ballStaysInsidePenArea(IPenaltyArea area)
	{
		boolean isAndWillStopInsidePenArea = area.isPointInShapeOrBehind(getBall().getPos())
				&& area.isPointInShapeOrBehind(getBall().getTrajectory().getPosByVel(0).getXYVector());

		DrawableRectangle dr = new DrawableRectangle(area.getRectangle(), new Color(4, 100, 156, 100));
		dr.setFill(isAndWillStopInsidePenArea);
		getShapes(EAiShapesLayer.AI_BALL_RESPONSIBILITY).add(dr);
		return isAndWillStopInsidePenArea;
	}


	private boolean noInterceptorRequired()
	{
		return getAiFrame().getGameState().isStandardSituationForThem()
				&& tooNearToOurPenAreaForIntercept();
	}


	private boolean tooNearToOurPenAreaForIntercept()
	{
		return Geometry.getPenaltyAreaOur().withMargin(minMarginToOurPenAreaForIntercept)
				.isPointInShape(getBall().getPos());
	}
}
