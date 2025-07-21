/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.skirmish;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribblingInformation;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Supplier;


/**
 * Detect skirmish situations.
 */
@RequiredArgsConstructor
public class SkirmishStrategyCalc extends ACalculator
{
	@Configurable(defValue = "2100.0", comment = "[mm] distance to pen area at which we switch between bully strategy and rip free")
	private static double switchingDistanceBulliesAndRipFree = 2100.0;
	@Configurable(defValue = "500.0", comment = "[mm] Hysteresis size")
	private static double switchingDistanceHysteresisSize = 500.0;

	@Configurable(defValue = "800.0", comment = "[mm]")
	private static double noDefaultBehaviorDistance = 800.0;

	@Configurable(defValue = "200.0", comment = "[mm] distance while skirmish is still pending and we are preparing")
	private static double distanceDuringPrepare = 200.0;

	@Configurable(defValue = "0.15", comment = "[%] Min stuckness score necessary to activate bullies")
	private static double minStucknessScoreForBullies = 0.15;
	@Configurable(defValue = "0.75", comment = "[%] Min stuckness score necessary to activate bodyguards")
	private static double minStucknessScoreForBodyguards = 0.75;
	@Configurable(defValue = "0.9", comment = "[%] Min stuckness score necessary to activate rip free move")
	private static double minStucknessScoreForRipFree = 0.9;

	private final Supplier<ESkirmishCategory> situation;
	private final Supplier<BotDistance> tigerClosestToBall;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<DribblingInformation> dribblingInformation;
	private final Supplier<IVector2> finisherBlockPos;

	@Getter
	private SkirmishStrategy skirmishStrategy;

	private double stucknessScore;
	private long lastTimestamp = 0;
	private Hysteresis switchingDistanceHysteresis = new Hysteresis(
			switchingDistanceBulliesAndRipFree - 0.5 * switchingDistanceHysteresisSize,
			switchingDistanceBulliesAndRipFree + 0.5 * switchingDistanceHysteresisSize
	);


	@Override
	public void doCalc()
	{
		updateHysteresis();
		updateStucknessScore();

		skirmishStrategy = decideSkirmishStrategy();

		var shapes = getShapes(EAiShapesLayer.AI_SKIRMISH_STRATEGY);
		shapes.addAll(
				skirmishStrategy.targetPositions().stream()
						.map(pos -> new DrawableCircle(pos, 150, getAiFrame().getTeamColor().getColor()))
						.toList()
		);
		shapes.add(new DrawableAnnotation(
				getBall().getPos().addNew(Vector2.fromX(400)),
				skirmishStrategy.type().toString(),
				getAiFrame().getTeamColor().getColor()
		));
		shapes.add(new DrawableAnnotation(
				getBall().getPos().addNew(Vector2.fromXY(400, -50)),
				String.format("%.2f", stucknessScore),
				getAiFrame().getTeamColor().getColor()
		));
		lastTimestamp = getWFrame().getTimestamp();
	}


	private void updateHysteresis()
	{
		switchingDistanceHysteresis.setUpperThreshold(
				switchingDistanceBulliesAndRipFree + 0.5 * switchingDistanceHysteresisSize);
		switchingDistanceHysteresis.setLowerThreshold(
				switchingDistanceBulliesAndRipFree - 0.5 * switchingDistanceHysteresisSize);
		switchingDistanceHysteresis.update(Geometry.getPenaltyAreaOur().distanceTo(getBall().getPos()));
	}


	private void updateStucknessScore()
	{
		if (situation.get() == ESkirmishCategory.NO_SKIRMISH)
		{
			stucknessScore = 0.5;
			return;
		}

		var gain = situation.get().isPending() ? 0.005 : 0.01;

		var botId = tigerClosestToBall.get().getBotId();
		var bot = getWFrame().getBot(botId);
		stucknessScore += gain * ((bot.getVel().getLength() - 0.4) * (lastTimestamp - getWFrame().getTimestamp()) * 1e-7);
		stucknessScore = SumatraMath.cap(stucknessScore, 0, 1);
	}


	private SkirmishStrategy decideSkirmishStrategy()
	{
		return switch (situation.get())
		{
			case NO_SKIRMISH -> SkirmishStrategy.none();
			case PENDING_WE_HAVE_CONTROL -> handlePendingWeHaveControl();
			case WE_HAVE_CONTROL -> handleWeHaveControl();
			case PENDING_OPPONENT_HAS_CONTROL, PENDING_CONTESTED_CONTROL -> handlePendingOpponentCouldHaveControl();
			case OPPONENT_HAS_CONTROL -> handelOpponentHasControl();
			case CONTESTED_CONTROL -> handleContestedControl();
		};
	}


	private SkirmishStrategy handlePendingWeHaveControl()
	{
		if (finisherBlockPos.get() != null)
		{
			return new SkirmishStrategy(ESkirmishStrategyType.BLOCKER_FOR_FINISHER, List.of(finisherBlockPos.get()));
		}
		return tryDefaultBehavior();
	}


	private SkirmishStrategy handleWeHaveControl()
	{
		if (finisherBlockPos.get() != null)
		{
			return new SkirmishStrategy(ESkirmishStrategyType.BLOCKER_FOR_FINISHER, List.of(finisherBlockPos.get()));
		}
		return stucknessScore > minStucknessScoreForBodyguards
				? createBodyguardStrategy()
				: SkirmishStrategy.none();
	}


	private SkirmishStrategy handlePendingOpponentCouldHaveControl()
	{
		return switchingDistanceHysteresis.isLower() && stucknessScore > minStucknessScoreForBullies - 0.1
				? prepareDefensiveStrategy()
				: tryDefaultBehavior();
	}


	private SkirmishStrategy handelOpponentHasControl()
	{
		return switchingDistanceHysteresis.isLower() && stucknessScore > minStucknessScoreForBullies
				? createBullyStrategy()
				: tryDefaultBehavior();
	}


	private SkirmishStrategy handleContestedControl()
	{
		if (finisherBlockPos.get() != null)
		{
			return new SkirmishStrategy(ESkirmishStrategyType.BLOCKER_FOR_FINISHER, List.of(finisherBlockPos.get()));
		}
		if (switchingDistanceHysteresis.isUpper())
		{
			return stucknessScore > minStucknessScoreForRipFree
					? createRipFreeStrategy()
					: tryDefaultBehavior();
		} else
		{
			return stucknessScore > minStucknessScoreForBullies
					? createBullyStrategy()
					: tryDefaultBehavior();
		}
	}


	private SkirmishStrategy tryDefaultBehavior()
	{
		var opponent = opponentClosestToBall.get().getBotId();
		var penAreaTheir = Geometry.getPenaltyAreaTheir().withMargin(noDefaultBehaviorDistance);
		if (!opponent.isBot() || penAreaTheir.isPointInShape(getBall().getPos()))
		{
			return SkirmishStrategy.none();
		}

		double distance = distanceDuringPrepare;
		if (penAreaTheir.withMargin(noDefaultBehaviorDistance * 0.5).isPointInShape(getBall().getPos()))
		{
			distance += Geometry.getBotRadius() * 3; // leave space for primary offensive to move in between
		}

		var target = Lines.segmentFromPoints(getBall().getPos(), Geometry.getGoalOur().getCenter())
				.stepAlongPath(distance);
		return new SkirmishStrategy(ESkirmishStrategyType.PREPARE_OFFENSIVE, List.of(target));
	}


	private SkirmishStrategy prepareDefensiveStrategy()
	{
		var offset = Vector2.fromPoints(Geometry.getGoalOur().getCenter(), getBall().getPos())
				.getNormalVector()
				.scaleTo(distanceDuringPrepare)
				.multiply(getBall().getPos().y() > 0 ? 1 : -1);

		var ballToPenAreaDist = Geometry.getPenaltyAreaOur().distanceTo(getBall().getPos());

		var pointOnPenaltyArea = Geometry.getPenaltyAreaOur()
				.withMargin(ballToPenAreaDist)
				.withRoundedCorners(ballToPenAreaDist)
				.projectPointOnToPenaltyAreaBorder(getBall().getPos().addNew(offset));

		return new SkirmishStrategy(ESkirmishStrategyType.PREPARE_DEFENSIVE, List.of(pointOnPenaltyArea));
	}


	private SkirmishStrategy createBullyStrategy()
	{
		var closestAttacker = getWFrame().getBot(opponentClosestToBall.get().getBotId());
		double orientation = getBall().getPos().subtractNew(closestAttacker.getPos()).getAngle();
		var a1 = orientation + AngleMath.deg2rad(120);
		var a2 = orientation - AngleMath.deg2rad(120);

		double defDistance = Geometry.getBotRadius() * 2 + 30;
		var p1 = closestAttacker.getPos().addNew(Vector2.fromAngle(a1).scaleToNew(defDistance));
		var p2 = closestAttacker.getPos().addNew(Vector2.fromAngle(a2).scaleToNew(defDistance));

		return new SkirmishStrategy(ESkirmishStrategyType.BULLY, List.of(p1, p2));
	}


	private SkirmishStrategy createBodyguardStrategy()
	{
		var dribblingBot = getWFrame().getBot(dribblingInformation.get().getDribblingBot());
		if (dribblingBot == null)
		{
			return SkirmishStrategy.none();
		}
		var tigerPos = getWFrame().getBot(dribblingInformation.get().getDribblingBot()).getPos();
		var opponentPos = getWFrame().getBot(opponentClosestToBall.get().getBotId()).getPos();
		var circle = dribblingInformation.get().getDribblingCircle();

		var botsSegment = Lines.segmentFromPoints(tigerPos, opponentPos);
		var circleCenterLine = Lines.lineFromPoints(circle.center(), botsSegment.directionVector());
		var posOnCenterLine = circleCenterLine.closestPointOnPath(botsSegment.getPathCenter());
		var offset = Vector2.fromPoints(tigerPos, posOnCenterLine).scaleTo(Geometry.getBotRadius() * 2.5);
		return new SkirmishStrategy(ESkirmishStrategyType.BODYGUARD, List.of(offset.add(tigerPos)));
	}


	private SkirmishStrategy createRipFreeStrategy()
	{
		var ourPos = getWFrame().getBot(tigerClosestToBall.get().getBotId()).getPos();
		IVector2 ballPos = getBall().getPos();
		IVector2 weToBall = ballPos.subtractNew(ourPos);
		double sgnWanted = Math.signum(ballPos.x()) * Math.signum(ballPos.y());
		double sgnToUs = Math.signum(weToBall.x());
		IVector dir = ourPos.subtractNew(ballPos).normalizeNew().getNormalVector();
		dir = Vector2.fromXY(dir.x() * sgnWanted, dir.y() * sgnToUs);
		IVector2 unscaled = Vector2.fromXY(ballPos.x() - ((250 + Geometry.getBotRadius()) * sgnToUs) * dir.x(),
				ballPos.y() + (-450 * sgnWanted) * dir.y()).addNew(weToBall.scaleToNew(-300));
		return new SkirmishStrategy(
				ESkirmishStrategyType.RIP_FREE,
				List.of(ballPos.addNew(unscaled.subtractNew(ballPos).scaleToNew(500)))
		);
	}
}
