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
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleKickData;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribblingInformation;
import edu.tigers.sumatra.ai.metis.offense.dribble.EDribblingCondition;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.metis.targetrater.RotationTimeHelper;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableFinisherMoveShape;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.animated.AnimatedArrowsOnFinisherMoveShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.penarea.EDribbleKickMoveDirection;
import edu.tigers.sumatra.math.penarea.FinisherMoveShape;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * Directly kick on the opponent goal with a dribbling move
 */
@RequiredArgsConstructor
public class FinisherActionMove extends AOffensiveActionMove
{
	@Configurable(defValue = "0.25")
	private static double finisherPenaltyScoreAdjustment = 0.25;

	@Configurable(defValue = "0.35")
	private static double wrongDirectionPenalty = 0.35;

	@Configurable(defValue = "0.20")
	private static double minScoreForPartialViability = 0.20;

	@Configurable(defValue = "0.20")
	private static double minScoreToKeepForcingTrueViability = 0.20;

	@Configurable(defValue = "200.0")
	private static double samplingStepDistanceOnFinisherMoveShape = 200;

	@Configurable(defValue = "600.0")
	private static double finisherMoveShapeCircleRadius = 600;

	@Configurable(defValue = "160.0")
	private static double moveBorderPointsInsideDist = 160;

	@Configurable(defValue = "true")
	private static boolean activateFinisherMoves = true;

	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();

	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<DribblingInformation> dribblingInformation;
	private final Supplier<Boolean> tigerDribblingBall;

	private final KickFactory kf = new KickFactory();

	private double finalScore;
	private DribbleToPos finalDribbleToPos;
	private IVector2 finalTarget;

	private FinisherMoveShape finisherMoveShape;


	@Override
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		finalDribbleToPos = null;
		finalScore = 0.0;
		finalTarget = null;

		EActionViability viability = EActionViability.FALSE;
		if (dribblingInformation.get().getDribblingCircle() != null && getAiFrame().getGameState().isRunning()
				&& activateFinisherMoves && Boolean.TRUE.equals(tigerDribblingBall.get())
				&& getBall().getPos().x() > 0)
		{
			viability = doCalc(botId);
			if (forceTrueViability(botId, finalScore))
			{
				getShapes(EAiShapesLayer.OFFENSE_FINISHER).add(
						new DrawableAnnotation(getWFrame().getBot(botId).getPos(), "Forcing True Via"));
				viability = EActionViability.TRUE;
			} else if (finalScore < minScoreForPartialViability)
			{
				viability = EActionViability.FALSE;
			}
		}

		if (finalTarget == null || finalDribbleToPos == null)
		{
			return Optional.empty();
		}

		kf.update(getWFrame());
		return Optional.of(RatedOffensiveAction.buildDribbleKick(
				EOffensiveActionMove.FINISHER,
				new OffensiveActionViability(viability, finalScore),
				kf.goalKick(finalDribbleToPos.getDribbleToDestination(), finalTarget),
				finalDribbleToPos));
	}


	private EActionViability doCalc(BotID botId)
	{
		var rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setObstacles(
				Stream.concat(getWFrame().getOpponentBots().values().stream(),
						getWFrame().getTigerBotsAvailable().values().stream()
								.filter(e -> e.getBotId() != botId)
								.filter(e -> e.getBotId() != getAiFrame().getKeeperId())
				).toList());
		var opponentID = opponentClosestToBall.get().getBotId();
		var opponentPos = getWFrame().getOpponentBots().containsKey(opponentID) ?
				getWFrame().getOpponentBot(opponentID).getPos() : Geometry.getGoalTheir().getCenter();
		var dribblingCircle = dribblingInformation.get().getDribblingCircle();
		var botPos = getWFrame().getBot(botId).getPos();

		IVector2 lineBase = getLineBase(botPos);
		if (recalculateFinisherMoveShapeNeeded(botPos))
		{
			finisherMoveShape = generateFinisherMoveMovementShape(lineBase);
		}

		if (finisherMoveShape.stepOnShape(finisherMoveShape.getStepPositionOnShape(getBall().getPos()))
				.distanceTo(getBall().getPos())
				> Geometry.getBotRadius() * 2)
		{
			// too far away from Finisher shape to do a nice finisher
			return EActionViability.FALSE;
		}

		getShapes(EAiShapesLayer.OFFENSE_FINISHER).add(
				new DrawableFinisherMoveShape(finisherMoveShape).setColor(Color.PINK));

		IVector2 bestOrigin = null;
		IRatedTarget bestRatedTarget = null;
		double bestAdjustedScore = 0;
		for (var sampledPoint : sampleAlongFinisherMoveShape(dribblingCircle))
		{
			var ratedTarget = rater.rate(sampledPoint);
			if (ratedTarget.isEmpty())
			{
				continue;
			}

			double startStep = finisherMoveShape.getStepPositionOnShape(lineBase);
			double endStep = finisherMoveShape.getStepPositionOnShape(sampledPoint);
			EDribbleKickMoveDirection direction =
					endStep > startStep ? EDribbleKickMoveDirection.POSITIVE : EDribbleKickMoveDirection.NEGATIVE;
			double adjustedScore = ratedTarget.map(e -> adjustScore(e, botId, direction)).orElse(0.0);
			ratedTarget.ifPresent(e -> drawRatedTarget(sampledPoint, ratedTarget.get(), adjustedScore));

			if (bestRatedTarget == null || adjustedScore > bestAdjustedScore)
			{
				bestRatedTarget = ratedTarget.get();
				bestOrigin = sampledPoint;
				bestAdjustedScore = adjustedScore;
			}
		}

		if (bestRatedTarget != null)
		{
			double startStep = finisherMoveShape.getStepPositionOnShape(lineBase);
			double endStep = finisherMoveShape.getStepPositionOnShape(bestOrigin);
			EDribbleKickMoveDirection direction =
					endStep > startStep ? EDribbleKickMoveDirection.POSITIVE : EDribbleKickMoveDirection.NEGATIVE;
			var moveToDestination = determineFinalMoveToPos(bestOrigin, startStep, direction);

			// calculate fake move pos
			IVector2 fakePoint = calculateFakePoint(dribblingCircle, moveToDestination);

			finalScore = applyMultiplier(bestAdjustedScore);
			finalTarget = bestRatedTarget.getTarget();
			finalDribbleToPos = new DribbleToPos(opponentPos, moveToDestination, EDribblingCondition.DRIBBLING_KICK,
					new DribbleKickData(finisherMoveShape, direction,
							isViolationUnavoidable(botId, finalTarget),
							dribblingInformation.get().isViolationImminent(),
							fakePoint));

			return EActionViability.PARTIALLY;
		}
		return EActionViability.FALSE;
	}


	private IVector2 calculateFakePoint(ICircle dribblingCircle, IVector2 moveToDestination)
	{
		var intersections = finisherMoveShape.intersectCircle(dribblingCircle);
		if (intersections.size() == 2)
		{
			// everything else is a rare edge case not worth caring about.
			var p1 = intersections.get(0);
			var p2 = intersections.get(1);

			double p1v = finisherMoveShape.getStepPositionOnShape(p1);
			double p2v = finisherMoveShape.getStepPositionOnShape(p2);

			double lowerP = Math.min(p1v, p2v);
			double upperP = Math.max(p2v, p1v);

			var lowerOuterPoint = finisherMoveShape.stepOnShape(lowerP + moveBorderPointsInsideDist);
			var upperOuterPoint = finisherMoveShape.stepOnShape(upperP - moveBorderPointsInsideDist);

			IVector2 fakePoint = Stream.of(lowerOuterPoint, upperOuterPoint)
					.max(Comparator.comparingDouble(e -> e.distanceTo(moveToDestination)))
					.orElseThrow();

			getShapes(EAiShapesLayer.OFFENSE_FINISHER).add(
					new DrawableCircle(Circle.createCircle(fakePoint, 70)).setFill(true).setColor(Color.YELLOW));

			return fakePoint;
		}
		return null;
	}


	private IVector2 determineFinalMoveToPos(IVector2 bestOrigin,
			double startStep,
			EDribbleKickMoveDirection direction)
	{
		getShapes(EAiShapesLayer.OFFENSE_FINISHER).add(
				new DrawableCircle(Circle.createCircle(bestOrigin, 70)).setColor(Color.CYAN));
		double adjustedTargetStep = getAdjustedTargetStep(startStep, direction);
		return finisherMoveShape.stepOnShape(adjustedTargetStep);
	}


	private double getAdjustedTargetStep(double startStep, EDribbleKickMoveDirection direction)
	{
		double adjustedTargetStep;
		if (direction == EDribbleKickMoveDirection.POSITIVE)
		{
			adjustedTargetStep = Math.min(startStep + 1000, finisherMoveShape.getMaxLength());
			getShapes(EAiShapesLayer.OFFENSE_FINISHER).add(
					AnimatedArrowsOnFinisherMoveShape.createArrowsOnFinisherMoveShape(finisherMoveShape, startStep,
							startStep + 1000).setColor(Color.GREEN.darker()));
		} else
		{
			adjustedTargetStep = Math.max(0, startStep - 1000);
			getShapes(EAiShapesLayer.OFFENSE_FINISHER).add(
					AnimatedArrowsOnFinisherMoveShape.createArrowsOnFinisherMoveShape(finisherMoveShape, startStep,
							startStep - 1000).setColor(Color.GREEN.darker()));
		}
		return adjustedTargetStep;
	}


	private boolean isViolationUnavoidable(BotID botId, IVector2 target)
	{
		if (!dribblingInformation.get().isDribblingInProgress())
			return false;
		if (Boolean.TRUE.equals(dribblingInformation.get().isViolationImminent()))
			return true;
		var bot = getWFrame().getBot(botId);
		var botPos = bot.getPos();
		var v0 = bot.getVel().getLength();
		double a = -bot.getMoveConstraints().getAccMax();
		if (v0 < 1e-3)
		{
			return false;
		}

		double sMax = dribblingInformation.get().getIntersectionPoint().distanceTo(botPos) / 1000.0;
		double t = RotationTimeHelper.calcRotationTime(
				bot.getAngularVel(),
				bot.getAngleByTime(0),
				target.subtractNew(botPos).getAngle(),
				bot.getMoveConstraints().getVelMaxW(),
				10
		);
		double sNeeded = 0.5 * a * t * t + v0 * t;
		return sNeeded > sMax;
	}


	private List<IVector2> sampleAlongFinisherMoveShape(ICircle dribblingCircle)
	{
		List<IVector2> samples = new ArrayList<>();
		for (double i = 0; i < finisherMoveShape.getMaxLength(); i += samplingStepDistanceOnFinisherMoveShape)
		{
			var sample = finisherMoveShape.stepOnShape(i);
			if (dribblingCircle.isPointInShape(sample))
			{
				samples.add(sample);
			}
		}
		return samples;
	}


	private void drawRatedTarget(IVector2 sampledPoint, IRatedTarget ratedTarget, double adjustedScore)
	{
		Color color = colorPicker.getColor(adjustedScore);
		getShapes(EAiShapesLayer.OFFENSE_FINISHER).add(
				new DrawableArrow(sampledPoint, ratedTarget.getTarget().subtractNew(sampledPoint)).setColor(
						color));

		Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 125);
		getShapes(EAiShapesLayer.OFFENSE_FINISHER)
				.add((new DrawableCircle(Circle.createCircle(sampledPoint, 50))).setColor(transparentColor)
						.setFill(true));
	}


	private boolean recalculateFinisherMoveShapeNeeded(IVector2 botPos)
	{
		return finisherMoveShape == null || !dribblingInformation.get().isDribblingInProgress() ||
				finisherMoveShape.stepOnShape(finisherMoveShape.getStepPositionOnShape(botPos)).distanceTo(
						botPos) > Geometry.getBotRadius();
	}


	private FinisherMoveShape generateFinisherMoveMovementShape(IVector2 lineBase)
	{
		double yMin = determineYMin(lineBase);
		double y = Math.min(yMin + 1000, Math.max(Math.abs(lineBase.y()), yMin));

		double xMax = determineXMax(lineBase);
		double x = Math.max(xMax - 1900, Math.min(lineBase.x(), xMax));

		var finisherMoveShapeCandidate = new FinisherMoveShape(x, Geometry.getFieldLength() / 2.0, y,
				finisherMoveShapeCircleRadius);

		return redefineShapeToFitLineBaseOnCornerPoints(lineBase, yMin, y, xMax, x, finisherMoveShapeCandidate);
	}


	private FinisherMoveShape redefineShapeToFitLineBaseOnCornerPoints(IVector2 lineBase, double yMin, double y,
			double xMax, double x,
			FinisherMoveShape finisherMoveShapeCandidate)
	{
		var boundingRectangle = finisherMoveShapeCandidate.getBoundingRectangle();
		double distToBounding = finisherMoveShapeCandidate.stepOnShape(
						finisherMoveShapeCandidate.getStepPositionOnShape(lineBase))
				.distanceTo(boundingRectangle.nearestPointOutside(lineBase));
		if (distToBounding > 1e-5)
		{
			// modify Finisher Shape to fit corner circles with lineBase pos
			IVector2 basePoint = finisherMoveShapeCandidate.stepOnShape(
					finisherMoveShapeCandidate.getStepPositionOnShape(lineBase));
			var baseToBall = lineBase.subtractNew(basePoint);
			double offsetX = baseToBall.x();
			double offsetY = baseToBall.y();
			y += Math.abs(offsetY);
			x -= Math.abs(offsetX);
			y = Math.min(yMin + 1000, Math.max(y, yMin));
			x = Math.max(xMax - 1900, Math.min(x, xMax));
			finisherMoveShapeCandidate = new FinisherMoveShape(x, Geometry.getFieldLength() / 2.0, y,
					finisherMoveShapeCircleRadius);
		}
		return finisherMoveShapeCandidate;
	}


	private double determineXMax(IVector2 lineBase)
	{
		double xMax = Geometry.getFieldLength() / 2.0 - Geometry.getPenaltyAreaTheir().getRectangle().xExtent()
				- Geometry.getBotRadius() * 3 + Geometry.getBallRadius();
		if (lineBase.x() > Geometry.getFieldLength() / 2.0 - Geometry.getPenaltyAreaTheir().getRectangle().xExtent())
		{
			xMax = xMax - (1 - SumatraMath.relative(
					Math.max(0, Math.abs(lineBase.y()) - Geometry.getPenaltyAreaTheir().getRectangle().yExtent() / 2.0), 0,
					400)) * 500;
		}
		return xMax;
	}


	private double determineYMin(IVector2 lineBase)
	{
		double yMin = Geometry.getPenaltyAreaTheir().getRectangle().yExtent() / 2.0 + Geometry.getBotRadius() * 2;
		if (Math.abs(lineBase.y()) < Geometry.getPenaltyAreaTheir().getRectangle().yExtent() / 2.0)
		{
			yMin = yMin + (1 - SumatraMath.relative(
					Geometry.getFieldLength() / 2.0 - Geometry.getPenaltyAreaTheir().getRectangle().xExtent() - lineBase.x(),
					0, 500)) * 500;
		}
		return yMin;
	}


	private IVector2 getLineBase(IVector2 botPos)
	{
		IVector2 lineBase = getWFrame().getBall().getPos();
		if (botPos.distanceTo(lineBase) < 400)
		{
			return botPos;
		}
		return lineBase;
	}


	private double adjustScore(IRatedTarget ratedTarget, BotID botId, EDribbleKickMoveDirection direction)
	{
		var directionPenalty = 0.0;
		var oldAction = getOldAction(botId);
		var oldMoveDirection = getDirectionFronmOldAction(oldAction);
		if (oldMoveDirection.isPresent() && oldMoveDirection.get() != direction
				&& getWFrame().getTiger(botId).getBallContact().hadRecentContact())
		{
			directionPenalty = wrongDirectionPenalty;
		}

		var antiToggleBonus = 0.0;
		if (getWFrame().getTiger(botId).getBallContact().hadRecentContact())
		{
			antiToggleBonus = 0.25;
		}

		// https://gitlab.tigers-mannheim.de/main/Sumatra/-/issues/1775 driving away from target penalty.

		return Math.min(1, Math.max(0,
				ratedTarget.getScore() - directionPenalty
						- finisherPenaltyScoreAdjustment
						+ getAntiToggleValue(botId, EOffensiveActionMove.FINISHER, antiToggleBonus)));
	}


	private OffensiveAction getOldAction(BotID botId)
	{
		var actions = getAiFrame().getPrevFrame().getTacticalField().getOffensiveActions();
		if (actions == null)
		{
			return null;
		}
		if (!actions.containsKey(botId))
		{
			return null;
		}
		return actions.get(botId).getAction();
	}


	private Optional<EDribbleKickMoveDirection> getDirectionFronmOldAction(OffensiveAction oldAction)
	{
		if (oldAction == null || oldAction.getDribbleToPos() == null
				|| oldAction.getDribbleToPos().getDribbleKickData() == null)
		{
			return Optional.empty();
		}
		return Optional.of(oldAction.getDribbleToPos().getDribbleKickData().getDirection());
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
		var currentPlanIsViable = score > minScoreToKeepForcingTrueViability && finalDribbleToPos != null;
		return prevAttacker.isPresent() && prevAttacker.get().getBot().getBallContact().hasContactFromVisionOrBarrier()
				&& currentPlanIsViable && lastActionWasFinisher;
	}
}
