/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static edu.tigers.sumatra.pathfinder.TrajectoryGenerator.generatePositionTrajectory;


/**
 * Position the robot at the opponent penalty area to prepare it for a goal kick.
 */
@RequiredArgsConstructor
public class PenaltyAreaAttackerRepulsiveBehavior extends ASupportBehavior
{
	@Configurable(comment = "Defines whether this behavior is active or not", defValue = "true")
	private static boolean enabled = true;

	@Configurable(comment = "Activation distance from center line [mm]", defValue = "2500.0")
	private static double minDistanceToCenter = 2500.0;

	@Configurable(comment = "Check for n points in each line Segment", defValue = "4")
	private static int nPoints = 4;

	@Configurable(defValue = "2.0", comment = "Margin between penalty area and bot destination [bot radius]")
	private static double marginBetweenDestAndPenArea = 2.0;

	@Configurable(defValue = "1250.0", comment = "Closest bot within this distance to a target position claims it. Everyone else receives score 0 at this position")
	private static double claimTargetDistance = 1250.0;

	private final PassFactory passFactory = new PassFactory();
	private final RatedPassFactory ratedPassFactory = new RatedPassFactory();

	private final Supplier<OffensiveStrategy> offensiveStrategy;
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;
	private final Supplier<GoalKick> bestGoalKick;
	private final Supplier<BallPossession> ballPossession;


	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		passFactory.update(getWFrame());
		ratedPassFactory.update(getWFrame().getOpponentBots().values());

		return kickOrigins.get().values().stream()
				.map(kickOrigin -> calculatePositionForKickOrigin(botID, kickOrigin.getPos()))
				.max(Comparator.comparing(SupportBehaviorPosition::getViability))
				.orElseGet(SupportBehaviorPosition::notAvailable);
	}


	private SupportBehaviorPosition calculatePositionForKickOrigin(BotID botID, IVector2 kickOrigin)
	{
		ITrackedBot bot = getWFrame().getBot(botID);
		if (!isReasonable(bot.getPos()))
		{
			return SupportBehaviorPosition.notAvailable();
		}

		SupportBehaviorPosition pos = createRatedPosition(bot, kickOrigin);
		drawHelpLines(pos.getPosition(), bot.getPos(), kickOrigin);
		return pos;
	}


	private double xCordOfPointToCalculateBotActivationDistance()
	{
		return 0.66 * Geometry.getFieldLength();
	}


	private boolean isReasonable(IVector2 myPosition)
	{
		// Is useless if opponent team controls the ball
		if (ballPossession.get().getEBallPossession() == EBallPossession.THEY)
		{
			return false;
		}
		// Is useless if the ball stays in opponent pen area
		ITrackedBall ball = getWFrame().getBall();
		IPenaltyArea penaltyAreaTheir = Geometry.getPenaltyAreaTheir();
		if (ball.getVel().getLength2() <= 1.0 && penaltyAreaTheir.isPointInShapeOrBehind(ball.getPos()))
		{
			return false;
		}
		// Is useless if bot is too far away from PenArea
		double distance = myPosition.distanceTo(Vector2.fromX(xCordOfPointToCalculateBotActivationDistance()));
		return distance <= xCordOfPointToCalculateBotActivationDistance() - minDistanceToCenter;
	}


	private void drawHelpLines(IVector2 destination, IVector2 myPos, IVector2 kickOrigin)
	{
		var shapes = getAiFrame().getShapes(EAiShapesLayer.SUPPORT_PENALTY_AREA_ATTACKER);
		// Mark the destination for the bot
		if (destination != null)
		{
			shapes.add(new DrawableCircle(Circle.createCircle(destination, Geometry.getBotRadius() * 1.2), Color.RED));
			shapes.add(new DrawableLine(destination, myPos, Color.RED));
		}
		// Position where attacker plans to control the ball
		shapes.add(new DrawableCircle(Circle.createCircle(kickOrigin, Geometry.getBotRadius() * 1.2), Color.YELLOW));

		// Sector borders
		shapes.add(new DrawableLine(
				Vector2.fromXY(minDistanceToCenter, Geometry.getFieldWidth() * (-0.5)),
				Geometry.getPenaltyAreaTheir().getRectangle().getCorner(IRectangle.ECorner.BOTTOM_LEFT), Color.CYAN));
		shapes.add(new DrawableLine(
				Geometry.getPenaltyAreaTheir().getRectangle().getCorner(IRectangle.ECorner.BOTTOM_LEFT),
				Geometry.getGoalTheir().getCenter(),
				Color.CYAN));

		shapes.add(new DrawableLine(
				Vector2.fromXY(minDistanceToCenter, Geometry.getFieldWidth() * (0.5)),
				Geometry.getPenaltyAreaTheir().getRectangle().getCorner(IRectangle.ECorner.TOP_LEFT), Color.CYAN));
		shapes.add(new DrawableLine(
				Geometry.getPenaltyAreaTheir().getRectangle().getCorner(IRectangle.ECorner.TOP_LEFT),
				Geometry.getGoalTheir().getCenter(),
				Color.CYAN));

		shapes.add(
				new DrawableLine(Geometry.getGoalTheir().getCenter(), Vector2.fromXY(minDistanceToCenter, 0), Color.CYAN));

		// Is reasonable distance for Ball
		shapes.add(new DrawableLine(
				Vector2.fromXY(minDistanceToCenter, Geometry.getFieldWidth() * (-0.5)),
				Vector2.fromXY(minDistanceToCenter, Geometry.getFieldWidth() * (0.5)), Color.CYAN));
		// Is reasonable distance for Bot
		IVector2 center = Vector2.fromX(xCordOfPointToCalculateBotActivationDistance());
		ICircle circle = Circle
				.createCircle(center, xCordOfPointToCalculateBotActivationDistance() - minDistanceToCenter);
		List<IVector2> intersectionsA = circle.intersectPerimeterPath(Geometry.getField().getEdges().get(1));
		List<IVector2> intersectionsB = circle.intersectPerimeterPath(Geometry.getField().getEdges().get(3));
		if (!intersectionsA.isEmpty() && !intersectionsB.isEmpty())
		{
			double startAngle = Vector2.fromPoints(center, intersectionsA.get(0)).getAngle();
			double endAngle = Vector2.fromPoints(center, intersectionsB.get(0)).getAngle();
			DrawableArc arc = new DrawableArc(Arc.createArc(center,
					xCordOfPointToCalculateBotActivationDistance() - minDistanceToCenter, startAngle,
					AngleMath.difference(endAngle, startAngle)),
					Color.CYAN);
			arc.setArcType(Arc2D.OPEN);
			shapes.add(arc);
		}
	}


	private SupportBehaviorPosition createRatedPosition(ITrackedBot bot, IVector2 kickOrigin)
	{
		Sector sector = getSectorFromPosition(kickOrigin);
		return switch (sector)
		{
			case RIGHT_CORNER, LEFT_CORNER -> getPassTargetOnOppositePenaltyAreaSide(bot, sector);
			case RIGHT_MIDDLE, LEFT_MIDDLE -> getReboundIntersectionPosition(bot, kickOrigin);
			default -> SupportBehaviorPosition.notAvailable();
		};
	}


	private Sector getSectorFromPosition(IVector2 position)
	{
		if (position.x() < minDistanceToCenter)
		{
			return Sector.INVALID;
		}

		IVector2 absPos = Vector2.fromXY(position.x(), Math.abs(position.y()));

		// Naming of variables is for blue KI, if the field is aligned horizontally
		IPenaltyArea penaltyAreaTheir = Geometry.getPenaltyAreaTheir();
		IVector2 leftUpperPenCorner = penaltyAreaTheir.getRectangle().getCorner(IRectangle.ECorner.TOP_LEFT);
		IVector2 upperCorner = Vector2.fromXY(minDistanceToCenter, Geometry.getFieldWidth() * 0.5);
		IVector2 rightUpperPenCorner = penaltyAreaTheir.getRectangle().getCorner(IRectangle.ECorner.TOP_RIGHT);
		IVector2 goalCenter = Geometry.getGoalTheir().getCenter();
		IVector2 penAreaOnFieldBorder = Vector2.fromXY(leftUpperPenCorner.x(), Geometry.getFieldWidth() * 0.5);

		// ECorner sectors will be divided in 3 shapes, one Rectangle two triangles
		// - Sector outside the PenArea is represented by outerTriangle and rectangle
		// - Sector inside the PenArea is represented by innerTriangle

		ITriangle outerTriangle = Triangle.fromCorners(leftUpperPenCorner, upperCorner, penAreaOnFieldBorder);
		ITriangle innerTriangle = Triangle.fromCorners(goalCenter, leftUpperPenCorner, rightUpperPenCorner);
		IRectangle rectangle = Rectangle.fromPoints(leftUpperPenCorner, Geometry.getField().getCorners().get(2));

		// Check position with absolute y to decide if CORNER or MIDDLE sector, then decide which side of the field
		if (outerTriangle.isPointInShape(absPos)
				|| innerTriangle.isPointInShape(absPos)
				|| rectangle.isPointInShape(absPos))
		{
			return position.y() < 0 ? Sector.RIGHT_CORNER : Sector.LEFT_CORNER;
		}
		return position.y() < 0 ? Sector.RIGHT_MIDDLE : Sector.LEFT_MIDDLE;
	}


	private SupportBehaviorPosition getPassTargetOnOppositePenaltyAreaSide(ITrackedBot bot, Sector sectorOfPassOrigin)
	{
		IPenaltyArea penAreaWithMargin = getPenaltyAreaWithSafetyMargin();
		IVector2 corner = createOppositePenaltyAreaSideCorner(sectorOfPassOrigin, penAreaWithMargin, bot.getPos());
		List<RatedPass> targets = createPossibleTargetsFromPenaltyAreaCorner(corner, bot);

		return targets.stream()
				.max(Comparator.comparing(p -> p.getCombinedScore(EPassRating.INTERCEPTION, EPassRating.PASSABILITY)))
				.map(target -> toRatedPosition(target, bot))
				.orElse(SupportBehaviorPosition.notAvailable());
	}


	private SupportBehaviorPosition toRatedPosition(RatedPass best, ITrackedBot bot)
	{
		var target = best.getPass().getKick().getTarget();
		var time = generatePositionTrajectory(bot, target).getTotalTime();

		double combinedScore = best.getCombinedScore(EPassRating.INTERCEPTION, EPassRating.PASSABILITY);
		double score = 0.5 * combinedScore + 0.5 * combinedScore / (time + 1);
		return SupportBehaviorPosition.fromDestination(target, score);
	}


	private List<RatedPass> createPossibleTargetsFromPenaltyAreaCorner(final IVector2 corner, ITrackedBot bot)
	{
		if (corner == null)
		{
			return List.of();
		}

		List<RatedPass> possibleTargets = new ArrayList<>();

		ILineSegment lineSegment = Lines.segmentFromPoints(corner,
				corner.addNew(Vector2.fromX(0.66 * Geometry.getPenaltyAreaDepth())));

		// Divide lineSegment into possible targets and rate the targets
		for (int i = 0; i < nPoints; i++)
		{
			IVector2 pos = lineSegment.stepAlongPath(lineSegment.getLength() * i / (nPoints - 1));

			if (!targetClaimedByOtherTiger(pos, bot))
			{
				passFactory.passes(getWFrame().getBall().getPos(), pos, BotID.noBot(), bot.getBotId(), 0.0)
						.stream()
						.map(ratedPassFactory::rate)
						.forEach(possibleTargets::add);
			}
		}

		return possibleTargets;
	}


	private IPenaltyArea getPenaltyAreaWithSafetyMargin()
	{
		boolean notRunning = !getAiFrame().getGameState().isRunning();
		double freeKickMargin = notRunning ? RuleConstraints.getPenAreaMarginStandard() : 0;
		double margin = Geometry.getBotRadius() + Geometry.getBotRadius() * marginBetweenDestAndPenArea + freeKickMargin;
		return Geometry.getPenaltyAreaTheir().withMargin(margin);
	}


	private boolean targetClaimedByOtherTiger(IVector2 position, ITrackedBot myself)
	{
		double minDistance = getWFrame().getTigerBotsAvailable().keySet().stream()
				.filter(e -> !e.equals(myself.getBotId()))
				.mapToDouble(e -> getWFrame().getBot(e).getPos().distanceTo(position)).min()
				.orElse(Geometry.getFieldLength());

		return minDistance < claimTargetDistance && minDistance < position.distanceTo(myself.getPos());
	}


	private IVector2 createOppositePenaltyAreaSideCorner(
			Sector sectorOfPassOrigin,
			IPenaltyArea penaltyArea,
			IVector2 myPos
	)
	{
		switch (sectorOfPassOrigin)
		{
			case LEFT_CORNER ->
			{
				if (myPos.y() >= 0)
				{
					return null;
				}
				return penaltyArea.getRectangle().getCorner(IRectangle.ECorner.BOTTOM_LEFT);
			}
			case RIGHT_CORNER ->
			{
				if (myPos.y() < 0)
				{
					return null;
				}
				return penaltyArea.getRectangle().getCorner(IRectangle.ECorner.TOP_LEFT);
			}
			default -> throw new InvalidParameterException("Unknown corner sector passed");
		}
	}


	private SupportBehaviorPosition getReboundIntersectionPosition(ITrackedBot bot, IVector2 kickOrigin)
	{
		if (offensiveStrategy.get().getAttackerBot().isEmpty())
		{
			return SupportBehaviorPosition.notAvailable();
		}

		GoalKick goalKick = bestGoalKick.get();
		if (goalKick == null)
		{
			return SupportBehaviorPosition.notAvailable();
		}

		IRatedTarget ratedTarget = goalKick.getRatedTarget();
		IPenaltyArea penAreaWithMargin = getPenaltyAreaWithSafetyMargin();
		IVector2 pos = createPositionReboundIntersectPenArea(kickOrigin, ratedTarget.getTarget(), penAreaWithMargin);
		if (pos == null)
		{
			return SupportBehaviorPosition.notAvailable();
		}
		if (targetClaimedByOtherTiger(pos, bot))
		{
			return SupportBehaviorPosition.fromDestination(pos, 0);
		}
		double time = TrajectoryGenerator.generatePositionTrajectory(bot, pos).getTotalTime();

		double score = ratedTarget.getScore();
		return SupportBehaviorPosition.fromDestination(pos, 0.5 * score + 0.5 * score / (time + 1));
	}


	private IVector2 createPositionReboundIntersectPenArea(IVector2 source, IVector2 target, IPenaltyArea penaltyArea)
	{
		var shapes = getAiFrame().getShapes(EAiShapesLayer.SUPPORT_PENALTY_AREA_ATTACKER);

		var line = Lines.segmentFromPoints(source, target);
		var invertedDirection = line.directionVector().multiplyNew(Vector2.fromXY(-1, 1));
		var invertedLine = Lines.halfLineFromDirection(target, invertedDirection);
		var reboundTravelLine = Lines.halfLineFromDirection(target, invertedLine.directionVector());

		shapes.add(new DrawableLine(line, Color.YELLOW));
		shapes.add(new DrawableLine(invertedLine.toLineSegment(line.getLength()), Color.YELLOW));

		List<IVector2> intersections = penaltyArea.intersectPerimeterPath(reboundTravelLine);
		return intersections.stream()
				.min(Comparator.comparingDouble(IVector::y))
				.orElse(null);
	}


	@Override
	public boolean isEnabled()
	{
		return enabled;
	}


	private enum Sector
	{
		LEFT_CORNER,
		RIGHT_CORNER,
		LEFT_MIDDLE,
		RIGHT_MIDDLE,
		INVALID
	}
}
