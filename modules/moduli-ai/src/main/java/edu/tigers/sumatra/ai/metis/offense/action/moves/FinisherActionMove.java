/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.KickFactory;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribblingInformation;
import edu.tigers.sumatra.ai.metis.offense.dribble.EDribblingCondition;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Directly kick on the opponent goal even when the chance is low
 */
@RequiredArgsConstructor
public class FinisherActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "0.35")
	private static double finisherPenaltyScoreAdjustment = 0.35;

	@Configurable(defValue = "0.20")
	private static double minScoreForPartialViability = 0.20;

	@Configurable(defValue = "0.4")
	private static double distancePenaltyFactor = 0.4;

	@Configurable(defValue = "true")
	private static boolean activateFinisherMoves = true;

	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();

	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<DribblingInformation> dribblingInformation;

	private final KickFactory kf = new KickFactory();

	private double finalScore;
	private DribbleToPos finalDribbleToPos;
	private IVector2 finalTarget;


	@Override
	public OffensiveAction calcAction(BotID botId)
	{

		finalDribbleToPos = null;
		finalScore = 0.0;
		finalTarget = null;

		EActionViability viability = EActionViability.FALSE;
		if (dribblingInformation.get().getDribblingCircle() != null && getAiFrame().getGameState().isRunning()
				&& activateFinisherMoves)
		{
			viability = doCalc(botId);
			if (forceTrueViability(botId, finalScore))
			{
				getShapes(EAiShapesLayer.OFFENSIVE_FINISHER).add(
						new DrawableAnnotation(getWFrame().getBot(botId).getPos(), "Forcing True Via"));
				viability = EActionViability.TRUE;
			} else if (finalScore < minScoreForPartialViability)
			{
				viability = EActionViability.FALSE;
			}
		}

		kf.update(getWFrame());
		return OffensiveAction.builder()
				.move(EOffensiveActionMove.FINISHER)
				.dribbleToPos(finalDribbleToPos)
				.kick(finalTarget != null && finalDribbleToPos != null ?
						kf.goalKick(finalDribbleToPos.getDribbleToDestination(), finalTarget) :
						null)
				.viability(new OffensiveActionViability(viability, finalScore))
				.build();
	}


	private EActionViability doCalc(BotID botId)
	{
		var rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setObstacles(getWFrame().getOpponentBots().values());
		var opponentID = opponentClosestToBall.get().getBotId();
		var opponentPos = getWFrame().getOpponentBots().containsKey(opponentID) ?
				getWFrame().getOpponentBot(opponentID).getPos() : Geometry.getGoalTheir().getCenter();
		var dribblingCircle = dribblingInformation.get().getDribblingCircle();
		var botPos = getWFrame().getBot(botId);

		IVector2 lineBase = getLineBase(botPos);
		IVector2 dir = getBotPosLineDirection();
		var botPosLine = Lines.lineFromDirection(lineBase, dir.getNormalVector());

		var intersections = dribblingCircle.lineIntersections(botPosLine);

		IVector2 bestOrigin = null;
		IRatedTarget bestRatedTarget = null;
		double bestAdjustedScore = 0;
		if (intersections.size() == 2)
		{
			var limitedBotLine = Lines.segmentFromPoints(intersections.get(0), intersections.get(1));
			getShapes(EAiShapesLayer.OFFENSIVE_FINISHER).add(new DrawableLine(limitedBotLine));

			List<IVector2> samples = calculateSamples(limitedBotLine);
			samples.add(botPos.getPos());

			for (var sampledPoint : samples)
			{
				var ratedTarget = rater.rate(sampledPoint);
				if (ratedTarget.isEmpty())
				{
					continue;
				}

				double adjustedScore = ratedTarget.map(
						e -> adjustScore(e, botPos.getPos(), botPos.getBotId(), sampledPoint)).orElse(0.0);
				getShapes(EAiShapesLayer.OFFENSIVE_FINISHER).add(
						new DrawableArrow(sampledPoint, ratedTarget.get().getTarget().subtractNew(sampledPoint)).setColor(
								colorPicker.getColor(adjustedScore)));

				if (bestRatedTarget == null || adjustedScore > bestAdjustedScore)
				{
					bestRatedTarget = ratedTarget.get();
					bestOrigin = sampledPoint;
					bestAdjustedScore = adjustedScore;
				}
			}

			if (bestRatedTarget != null)
			{
				getShapes(EAiShapesLayer.OFFENSIVE_FINISHER).add(
						new DrawableCircle(Circle.createCircle(bestOrigin, 50)).setColor(Color.CYAN).setFill(true));
				finalDribbleToPos = new DribbleToPos(opponentPos, bestOrigin, EDribblingCondition.DRIBBLING_KICK);
				finalScore = applyMultiplier(bestAdjustedScore);
				finalTarget = bestRatedTarget.getTarget();
				return EActionViability.PARTIALLY;
			}
		}
		return EActionViability.FALSE;
	}


	private IVector2 getLineBase(ITrackedBot botPos)
	{
		IVector2 lineBase = getWFrame().getBall().getPos();
		if (botPos.getPos().distanceTo(lineBase) < 200)
		{
			lineBase = botPos.getPos();
		}
		return lineBase;
	}


	private IVector2 getBotPosLineDirection()
	{
		IVector2 posCorner = Geometry.getPenaltyAreaTheir().getPosCorner();
		if (getBall().getPos().x() < posCorner.x())
		{
			return Vector2.fromX(1);
		}
		return Vector2.fromY(1);
	}


	private List<IVector2> calculateSamples(ILineSegment limitedBotLine)
	{
		return limitedBotLine.withMargin(-100).getSteps(150)
				.stream().filter(this::isPointLegal).collect(Collectors.toList());
	}


	private double adjustScore(IRatedTarget ratedTarget, IVector2 botPosPos, BotID botId,
			IVector2 pos)
	{
		var distToPoint = botPosPos.distanceTo(pos);
		var distPenalty = SumatraMath.relative(distToPoint, 0, 1000) * distancePenaltyFactor;
		return Math.min(1,
				Math.max(0, ratedTarget.getScore() - distPenalty - finisherPenaltyScoreAdjustment + getAntiToggleValue(
						botId, EOffensiveActionMove.FINISHER, 0.15)));
	}


	private boolean isPointLegal(IVector2 point)
	{
		var opponentBotBlocksTarget = getWFrame().getOpponentBots().values().stream()
				.anyMatch(e -> e.getPos().distanceTo(point) < Geometry.getBotRadius() * 2.0);
		return Geometry.getField().isPointInShape(point) && !Geometry.getPenaltyAreaTheir()
				.isPointInShape(point, Geometry.getBotRadius())
				&& point.x() > 0 && !opponentBotBlocksTarget;
	}


	private boolean forceTrueViability(BotID id, double score)
	{
		var lastAction = getAiFrame()
				.getPrevFrame()
				.getTacticalField()
				.getOffensiveActions()
				.get(id);
		var lastActionWasFinisher =
				lastAction != null && lastAction.getMove() == EOffensiveActionMove.FINISHER && dribblingInformation.get()
						.isDribblingInProgress();
		var attackers = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(ERole.ATTACKER);
		var prevAttacker = attackers.stream().filter(e -> e.getBotID().equals(id)).findFirst();
		var currentPlanIsViable = score > 0.25 && finalDribbleToPos != null;
		return prevAttacker.isPresent() && prevAttacker.get().getBot().getBallContact().hasContactFromVisionOrBarrier()
				&& currentPlanIsViable && lastActionWasFinisher;
	}
}
