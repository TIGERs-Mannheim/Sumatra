/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallControl;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.KickFactory;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableQuadrilateral;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.quadrilateral.IQuadrilateral;
import edu.tigers.sumatra.math.quadrilateral.Quadrilateral;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Calculate a DribbleToPos for a given tigerBot while regarding the closest opponent bot to the ball
 */
@RequiredArgsConstructor
public class BallDribbleToPosCalc extends ACalculator
{
	@Configurable(comment = "[mm] No opponent closer than this to the ball -> it is not dangerous", defValue = "1800.0")
	private static double opponentIsCloseDistance = 1800.0;

	@Configurable(comment = "[mm] protectKick target distance", defValue = "800.0")
	private static double protectKickTargetDistance = 800.0;

	@Configurable(comment = "[mm] consider only closest bot dist", defValue = "300.0")
	private static double considerOnlyClosestBotDist = 300.0;

	@Configurable(comment = "[s] needed time to reach protect kick target", defValue = "1.2")
	private static double timeToReachProtectKickTarget = 1.2;

	@Configurable(comment = "number of protect kick target samples per half", defValue = "5")
	private static int numOfProtectKickSamplesPerHalf = 5;

	static
	{
		ConfigRegistration.registerClass("metis", BallDribbleToPosCalc.class);
	}

	private final KickFactory kickFactory = new KickFactory();

	private final Supplier<List<BotID>> ballHandlingBots;
	private final Supplier<DribblingInformation> dribblingInformation;
	private final Supplier<BallPossession> ballPossession;
	private final Supplier<BotDistance> closestOpponent;

	@Getter
	private DribbleToPos dribbleToPos;

	@Getter
	private Pass protectPass;


	@Override
	public void doCalc()
	{
		if (!ballHandlingBots.get().isEmpty())
		{
			dribbleToPos = getDribbleToPos(getWFrame().getTiger(ballHandlingBots.get().getFirst()));
		} else
		{
			// there is no ball handling robot, so this has no effect
			dribbleToPos = new DribbleToPos(Geometry.getGoalTheir().getCenter(), null, EDribblingCondition.DEFAULT, null);
		}
	}


	private DribbleToPos getDribbleToPos(ITrackedBot tigerBot)
	{
		if (tigerBot.getBotId().isUninitializedID())
		{
			// default values, this cannot happen in game.
			return new DribbleToPos(Geometry.getGoalTheir().getCenter(), null, EDribblingCondition.DEFAULT, null);
		}

		IVector2 dribbleToDestination = null;
		EDribblingCondition condition = EDribblingCondition.DEFAULT;
		addDribblingInformationShapes(dribblingInformation.get());

		ICircle dribbleCircle = Circle.createCircle(
				dribblingInformation.get().getStartPos(),
				dribblingInformation.get().getDribblingCircle().radius()
		);
		var opponents = getWFrame().getOpponentBots().values().stream()
				.filter(e -> dribbleCircle.withMargin(500).isPointInShape(e.getPos())).toList();

		protectPass = calculatePotentialProtectPass();
		if (protectPass != null)
		{
			getShapesProtectKick().add(
					new DrawableArrow(
							getBall().getPos(), protectPass.getKick().getTarget().subtractNew(getBall().getPos()),
							new Color(207, 24, 255, 237)
					));
		}

		List<IQuadrilateral> blockingQuads = getBlockingQuads(tigerBot, dribbleCircle, opponents);

		List<ValuePoint> ratedPoints = getRatedPoints(dribbleCircle, blockingQuads);
		var bestPoint = ratedPoints.stream().max(Comparator.comparingDouble(ValuePoint::getValue));
		if (bestPoint.isPresent())
		{
			var line = Lines.segmentFromPoints(tigerBot.getPos(), bestPoint.get());
			boolean isBlocked = blockingQuads.stream().map(e -> e.intersectPerimeterPath(line))
					.anyMatch(e -> !e.isEmpty());

			getShapes().add(new DrawableAnnotation(
					dribblingInformation.get().getStartPos(),
					isBlocked ? "is blocked" : "not blocked"
			));
			getShapes().add(
					new DrawableCircle(Circle.createCircle(bestPoint.get(), 30)).setFill(true).setColor(Color.cyan));
			getShapes().add(
					new DrawableArrow(tigerBot.getPos(), bestPoint.get().subtractNew(tigerBot.getPos()), Color.cyan));

			if (!isBlocked)
			{
				dribbleToDestination = bestPoint.get();
			}
		}

		if (dribbleToDestination == null)
		{
			// fallback position, stay at closest point
			dribbleToDestination = ratedPoints.stream()
					.min(Comparator.comparingDouble(point -> point.distanceTo(tigerBot.getPos()))).map(IVector2.class::cast)
					.orElse(null);
		}

		// calculate protectFrom
		IVector2 protectTarget;
		if (closestOpponent.get().getDist() < considerOnlyClosestBotDist)
		{
			protectTarget = getWFrame().getBot(closestOpponent.get().getBotId()).getPos();
		} else
		{
			var weightedOpponents = opponents.stream().map(e -> new ValuePoint(
							e.getBotKickerPos(),
							Math.max(0, 1 - e.getBotKickerPos().distanceTo(tigerBot.getPos()) / opponentIsCloseDistance)
					))
					.filter(e -> e.getValue() < dribbleCircle.radius()).toList();

			protectTarget = calcProtectTarget(tigerBot, weightedOpponents);
		}
		return new DribbleToPos(protectTarget, dribbleToDestination, condition, null);
	}


	private Pass calculatePotentialProtectPass()
	{
		if (ballHandlingBots.get().isEmpty())
		{
			return null;
		}

		List<Pass> consideredPasses = new ArrayList<>();
		for (int i = -numOfProtectKickSamplesPerHalf + 1; i < numOfProtectKickSamplesPerHalf - 1; i++)
		{
			IVector2 opponentGoalFront = Vector2.fromXY(
							Geometry.getFieldLength() / 2.0,
							(Geometry.getFieldWidth() / 2.0 / numOfProtectKickSamplesPerHalf) * i
					)
					.addNew(Vector2.fromX(-Geometry.getPenaltyAreaTheir().getRectangle().xExtent() * 1.25));
			IVector2 ballToOpponentGoalFront = opponentGoalFront.subtractNew(getBall().getPos());
			IVector2 kickTarget = getBall().getPos().addNew(ballToOpponentGoalFront.scaleToNew(protectKickTargetDistance));
			getPassForKickTarget(kickTarget, opponentGoalFront).ifPresent(consideredPasses::add);
		}

		return consideredPasses.stream().max(Comparator.comparingDouble(Pass::getPreparationTime)).orElse(null);
	}


	private Optional<Pass> getPassForKickTarget(IVector2 kickTarget, IVector2 kickTargetSource)
	{
		var closeOpponent = getWFrame().getOpponentBots().values().stream()
				.filter(bot -> getWFrame().getBall().getPos().distanceTo(bot.getPos()) < 5000)
				.map(bot -> new Pair<>(
						bot.getBotId(),
						TrajectoryGenerator.generatePositionTrajectory(bot, kickTarget).getTotalTime()
				))
				.min(Comparator.comparingDouble(Pair::getValue));

		boolean reachingProtectKickBeforeOpponent;
		double minOpponentTime;
		if (closeOpponent.isPresent())
		{
			minOpponentTime = closeOpponent.get().getValue();
			reachingProtectKickBeforeOpponent = minOpponentTime > timeToReachProtectKickTarget;
		} else
		{
			reachingProtectKickBeforeOpponent = true;
			minOpponentTime = 0;
		}

		var ballToKickTarget = kickTarget.subtractNew(getBall().getPos());
		var kickTargetSide1 = kickTarget.addNew(ballToKickTarget.getNormalVector().scaleToNew(250));
		var kickTargetSide2 = kickTarget.addNew(ballToKickTarget.getNormalVector().scaleToNew(-250));
		var pathQuadliteral = Quadrilateral.fromCorners(
				kickTargetSide1.subtractNew(ballToKickTarget).addNew(ballToKickTarget.getNormalVector().scaleToNew(-120)),
				kickTargetSide2.subtractNew(ballToKickTarget).addNew(ballToKickTarget.getNormalVector().scaleToNew(120)),
				kickTargetSide1.addNew(ballToKickTarget.scaleToNew(Geometry.getBotRadius() * 2)),
				kickTargetSide2.addNew(ballToKickTarget.scaleToNew(Geometry.getBotRadius() * 2))
		);

		boolean protectPathFree = getWFrame().getOpponentBots().values().stream()
				.filter(bot -> kickTarget.distanceTo(bot.getPos()) < 2000)
				.filter(bot -> pathQuadliteral.isPointInShape(bot.getPos())).findAny().isEmpty();

		if (reachingProtectKickBeforeOpponent
				&& protectPathFree
				&& Geometry.getField().withMargin(-600).isPointInShape(kickTarget)
				&& !Geometry.getPenaltyAreaTheir().getRectangle().withMargin(Geometry.getBotRadius() * 2).isPointInShape(
				kickTarget)
				&& kickTargetSource.distanceTo(getBall().getPos()) > protectKickTargetDistance * 1.25)
		{
			if (minOpponentTime < Double.MAX_VALUE)
			{
				getShapesProtectKick().add(
						new DrawableAnnotation(kickTarget, "time: " + String.format("%1.2f", minOpponentTime), Color.CYAN));
			}

			getShapesProtectKick().add(
					new DrawableArrow(
							getBall().getPos(), kickTarget.subtractNew(getBall().getPos()),
							new Color(47, 104, 255, 137)
					));

			kickFactory.setAimingTolerance(0.6);
			kickFactory.update(getWFrame());
			var kick = kickFactory.chip(getBall().getPos(), kickTarget, 1.7);
			return Optional.of(
					new Pass(
							kick, BotID.noBot(), ballHandlingBots.get().getFirst(), 0, 0, minOpponentTime,
							EBallReceiveMode.DONT_CARE
					));
		}

		return Optional.empty();
	}


	private IVector2 calcProtectTarget(ITrackedBot tigerBot, List<ValuePoint> weightedOpponents)
	{
		IVector2 protectTarget;
		if (ballPossession.get().getOpponentBallControl() == EBallControl.STRONG)
		{
			var opponentBot = getWFrame().getOpponentBot(ballPossession.get().getOpponentsId());
			protectTarget = opponentBot.getPos();
		} else
		{
			IVector2 protectFrom = Vector2.zero();
			for (var wO : weightedOpponents)
			{
				protectFrom = protectFrom.addNew(
						wO.subtractNew(tigerBot.getPos()).scaleToNew(Geometry.getBotRadius() * 2 * wO.getValue()));
			}
			IVector2 fallbackTarget = dribblingInformation.get().getStartPos().addNew(
					Geometry.getGoalOur().getCenter().subtractNew(dribblingInformation.get().getStartPos()).multiplyNew(-1));
			protectTarget = protectFrom.isZeroVector() ? fallbackTarget : tigerBot.getPos().addNew(protectFrom);
		}

		getShapes().add(new DrawableCircle(Circle.createCircle(protectTarget, 50), Color.ORANGE).setFill(true));
		return protectTarget;
	}


	private void addDribblingInformationShapes(DribblingInformation dribblingInformation)
	{
		if (dribblingInformation.isDribblingInProgress())
		{
			getShapes().add(new DrawableCircle(
					dribblingInformation.getStartPos(),
					dribblingInformation.getDribblingCircle().radius(), Color.RED.brighter()
			));
		} else
		{
			getShapes().add(new DrawableCircle(
					dribblingInformation.getStartPos(),
					dribblingInformation.getDribblingCircle().radius(), Color.RED.darker()
			));
		}
	}


	private List<ValuePoint> getRatedPoints(ICircle dribbleCircle, List<IQuadrilateral> blockingQuads)
	{
		List<ValuePoint> ratedPoints = new ArrayList<>();
		IVector2 center = dribbleCircle.center();
		int samplingSize = 10;
		for (int i = 0; i <= samplingSize; i++)
		{
			double sfx = (i / (double) samplingSize) * 2 - 1;
			for (int j = 0; j <= samplingSize; j++)
			{
				double sfy = (j / (double) samplingSize) * 2 - 1;
				double x = dribbleCircle.radius() * sfx;
				double y = dribbleCircle.radius() * sfy;
				IVector2 point = center.subtractNew(Vector2.fromXY(x, y));

				if (isPointInvalid(point, dribbleCircle))
				{
					continue;
				}

				double score = 1.0;
				score = reduceScoreByBlockingQuads(blockingQuads, point, score);
				score = reduceScoreByDistanceToCircleCenter(dribbleCircle, point, score);
				score = Math.max(0, score);
				ratedPoints.add(new ValuePoint(point, score));

				addDebugShapes(point, score);
			}
		}
		return ratedPoints;
	}


	private void addDebugShapes(IVector2 point, double score)
	{
		getShapes().add(new DrawableCircle(
				Circle.createCircle(point, 10),
				new Color(Math.min(255, (int) ((1 - score) * 255)), Math.min(255, (int) (score * 255)), 0)
		).setFill(true));
		getShapes().add(new DrawableAnnotation(point, String.format("%.2f", score)).withFontHeight(8));
	}


	private boolean isPointInvalid(IVector2 point, ICircle dribbleCircle)
	{
		if (!dribbleCircle.withMargin(-140).isPointInShape(point))
		{
			return true;
		}

		return !Geometry.getField().withMargin(-Geometry.getBotRadius() * 2).isPointInShape(point)
				|| Geometry.getPenaltyAreaTheir().withMargin(50).isPointInShape(point) || Geometry.getPenaltyAreaOur()
				.withMargin(200).isPointInShape(point);
	}


	private double reduceScoreByDistanceToCircleCenter(ICircle dribbleCircle, IVector2 point, double score)
	{
		double distToCenter = point.distanceTo(dribbleCircle.center());
		return score - Math.min(1, Math.max(0, distToCenter - 300) / dribbleCircle.radius());
	}


	private double reduceScoreByBlockingQuads(List<IQuadrilateral> blockingQuads, IVector2 point, double score)
	{
		// reduce score regarding blockingQuads
		double tmpScore = score;
		for (var quad : blockingQuads)
		{
			if (quad.isPointInShape(point))
			{
				return 0;
			} else
			{
				double dist = quad.nearestPointInside(point).distanceTo(point);
				tmpScore -= (1 - Math.min(1, dist / 600));
			}
		}
		return tmpScore;
	}


	private List<IQuadrilateral> getBlockingQuads(
			ITrackedBot tigerBot, ICircle dribbleCircle,
			List<ITrackedBot> opponents
	)
	{
		List<IQuadrilateral> blockingQuads = opponents.stream()
				.map(e -> getBlockingQuad(
						tigerBot, dribblingInformation.get().getDribblingCircle().radius(), dribbleCircle,
						e
				)).filter(Optional::isPresent).map(Optional::get).toList();
		blockingQuads.forEach(
				e -> getShapes().add(new DrawableQuadrilateral(e, new Color(248, 0, 0, 128)).setFill(true)));
		return blockingQuads;
	}


	private Optional<IQuadrilateral> getBlockingQuad(
			ITrackedBot tigerBot, double dribblingCircleRadius,
			ICircle dribbleCircle, ITrackedBot opponent
	)
	{
		var toOpponent = opponent.getPos().subtractNew(tigerBot.getPos());
		var offset = toOpponent.getNormalVector().scaleToNew(Geometry.getBotRadius() * 1.5);
		double distTowardsTiger = Math.min(Geometry.getBotRadius() * 2.5 + 20, toOpponent.getLength2() - 10);

		var p1 = opponent.getPos().addNew(offset).addNew(toOpponent.scaleToNew(-distTowardsTiger));
		var p2 = opponent.getPos().subtractNew(offset).addNew(toOpponent.scaleToNew(-distTowardsTiger));

		var base1 = opponent.getPos().addNew(offset.multiplyNew(2));
		var toBase1 = base1.subtractNew(tigerBot.getPos().addNew(offset));
		var intersections = Circle.createCircle(dribbleCircle.center(), dribblingCircleRadius * 2.0)
				.intersectPerimeterPath(
						Lines.halfLineFromDirection(base1, toBase1.scaleToNew(dribblingCircleRadius * 4.2)));
		if (intersections.size() != 1)
		{
			return Optional.empty();
		}
		var p3 = intersections.getFirst();

		var base2 = opponent.getPos().addNew(offset.multiplyNew(-2));
		var toBase2 = base2.subtractNew(tigerBot.getPos().subtractNew(offset));
		intersections = Circle.createCircle(dribbleCircle.center(), dribblingCircleRadius * 2.0).intersectPerimeterPath(
				Lines.halfLineFromDirection(base2, toBase2.scaleToNew(dribblingCircleRadius * 4.2)));
		if (intersections.size() != 1)
		{
			return Optional.empty();
		}
		var p4 = intersections.getFirst();

		var quad = Quadrilateral.fromCorners(p1, p2, p3, p4);
		return Optional.of(quad);
	}


	private List<IDrawableShape> getShapesProtectKick()
	{
		return getAiFrame().getShapes(EAiShapesLayer.OFFENSE_PROTECT_KICK);
	}


	private List<IDrawableShape> getShapes()
	{
		return getAiFrame().getShapes(EAiShapesLayer.OFFENSE_DRIBBLE);
	}
}
