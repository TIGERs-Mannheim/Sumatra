/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import edu.tigers.sumatra.ai.pandora.roles.support.ESupportBehavior;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static edu.tigers.sumatra.pathfinder.TrajectoryGenerator.generatePositionTrajectory;


/**
 * This behavior aims to receive a pass
 */
public class PenaltyAreaAttacker extends ASupportBehavior
{

	@Configurable(comment = "Defines whether this behavior is active or not", defValue = "true")
	private static boolean isActive = true;

	@Configurable(comment = "Activation distance from center line [mm]", defValue = "2500.0")
	private static double minDistanceToCenter = 2500.0;

	@Configurable(comment = "Check for n points in each line Segment", defValue = "4")
	private static int nPoints = 4;

	@Configurable(defValue = "2.0", comment = "Margin between penalty area and bot destination [bot radius]")
	private static double marginBetweenDestAndPenArea = 2.0;

	@Configurable(defValue = "1250.0", comment = "Closest bot within this distance to a target position claims it. Everyone else receives score 0 at this position")
	private static double claimTargetDistance = 1250.0;

	static
	{
		ConfigRegistration.registerClass("roles", PenaltyAreaAttacker.class);
	}

	private final PassFactory passFactory = new PassFactory();
	private final RatedPassFactory ratedPassFactory = new RatedPassFactory();
	private final double xCordOfPointToCalculateBotActivationDistance = 0.66 * Geometry.getFieldLength();
	private final List<IDrawableShape> shapes = new ArrayList<>();
	private IVector2 destination = null;
	private IVector2 passOrigin;
	private MoveToSkill skill;


	public PenaltyAreaAttacker(final ARole role)
	{
		super(role);
	}


	public static boolean isActive()
	{
		return isActive;
	}


	private RatedPosition getPassTargetOnOppositePenaltyAreaSide(Sector sectorOfPassOrigin)
	{
		var penAreaWithMargin = getPenaltyAreaWithSafetyMargin();
		var corner = createOppositePenaltyAreaSideCorner(sectorOfPassOrigin, penAreaWithMargin);
		var targets = createPossibleTargetsFromPenaltyAreaCorner(corner);
		return targets.stream()
				.max(Comparator.comparing(p -> p.getCombinedScore(EPassRating.INTERCEPTION, EPassRating.PASSABILITY)))
				.map(this::toRatedPosition)
				.orElse(new RatedPosition());
	}


	private RatedPosition toRatedPosition(RatedPass best)
	{
		var target = best.getPass().getKick().getTarget();
		var time = generatePositionTrajectory(getRole().getBot(), target).getTotalTime();

		double combinedScore = best.getCombinedScore(EPassRating.INTERCEPTION, EPassRating.PASSABILITY);
		double score = 0.5 * combinedScore + 0.5 * combinedScore / (time + 1);
		return new RatedPosition(target, score);
	}


	private List<RatedPass> createPossibleTargetsFromPenaltyAreaCorner(final IVector2 corner)
	{
		if (corner == null)
		{
			return new ArrayList<>();
		}

		List<RatedPass> possibleTargets = new ArrayList<>();

		ILineSegment lineSegment = Lines.segmentFromPoints(corner,
				corner.addNew(Vector2.fromX(0.66 * Geometry.getPenaltyAreaDepth())));

		// Divide lineSegment into possible targets and rate the targets
		for (int i = 0; i < nPoints; i++)
		{
			var pos = lineSegment.stepAlongLine(lineSegment.getLength() * i / (nPoints - 1));
			if (!targetClaimedByOtherTiger(pos))
			{
				passFactory.passes(getRole().getBall().getPos(), pos, BotID.noBot(), getRole().getBotID(), 0.0)
						.stream()
						.map(ratedPassFactory::rate)
						.forEach(possibleTargets::add);
			}
		}

		return possibleTargets;
	}


	private IVector2 createOppositePenaltyAreaSideCorner(Sector sectorOfPassOrigin, IPenaltyArea penaltyArea)
	{
		switch (sectorOfPassOrigin)
		{
			case LEFT_CORNER:
				if (getRole().getPos().y() >= 0)
				{
					return null;
				}
				return penaltyArea.getRectangle().getCorner(IRectangle.ECorner.BOTTOM_LEFT);

			case RIGHT_CORNER:
				if (getRole().getPos().y() < 0)
				{
					return null;
				}
				return penaltyArea.getRectangle().getCorner(IRectangle.ECorner.TOP_LEFT);

			default:
				throw new InvalidParameterException("Unknown corner sector passed");
		}
	}


	private RatedPosition getReboundIntersectionPosition(TacticalField tacticalField)
	{
		if (tacticalField.getOffensiveStrategy().getAttackerBot().isEmpty())
		{
			return new RatedPosition();
		}
		GoalKick goalKick = tacticalField.getBestGoalKick();
		if (goalKick == null)
		{
			return new RatedPosition();
		}

		IPenaltyArea penAreaWithMargin = getPenaltyAreaWithSafetyMargin();

		IVector2 pos = createPositionReboundIntersectPenArea(goalKick.getRatedTarget(), penAreaWithMargin);
		if (pos == null)
		{
			return new RatedPosition();
		}
		if (targetClaimedByOtherTiger(pos))
		{
			return new RatedPosition(pos, 0);
		}
		double time = generatePositionTrajectory(getRole().getBot(), pos).getTotalTime();

		double score = goalKick.getRatedTarget().getScore();
		return new RatedPosition(pos, 0.5 * score + 0.5 * score / (time + 1));
	}


	private IPenaltyArea getPenaltyAreaWithSafetyMargin()
	{
		double freeKickMargin = (getRole().getAiFrame().getGameState().getState() != EGameState.RUNNING)
				? RuleConstraints.getBotToPenaltyAreaMarginStandard()
				: 0;
		return Geometry.getPenaltyAreaTheir()
				.withMargin(
						Geometry.getBotRadius() + Geometry.getBotRadius() * marginBetweenDestAndPenArea + freeKickMargin);
	}


	private IVector2 createPositionReboundIntersectPenArea(IRatedTarget target,
			IPenaltyArea penaltyArea)
	{
		IVector2 targetPos = target.getTarget();

		ILine line = Line.fromPoints(passOrigin, targetPos);
		ILine invertedLine = Line.fromDirection(
				targetPos,
				line.directionVector().multiplyNew(Vector2.fromXY(-1, 1)));
		IHalfLine reboundTravelLine = Lines.halfLineFromDirection(targetPos, invertedLine.directionVector());

		shapes.add(new DrawableLine(line, Color.YELLOW));
		shapes.add(new DrawableLine(invertedLine, Color.YELLOW));

		List<IVector2> intersections = penaltyArea.lineIntersections(reboundTravelLine);

		if (intersections.size() == 1)
		{
			return intersections.get(0);
		} else if (intersections.size() == 2)
		{
			intersections.sort(Comparator.comparingDouble(IVector::y));
			return intersections.get(0);
		}

		return null;
	}


	private boolean targetClaimedByOtherTiger(IVector2 position)
	{
		double minDistance = getRole().getWFrame().getTigerBotsAvailable().keySet().stream()
				.filter(this::isNeitherKeeperOrI)
				.mapToDouble(e -> getRole().getWFrame().getBot(e).getPos().distanceTo(position)).min()
				.orElse(Geometry.getFieldLength());

		return (minDistance < claimTargetDistance && minDistance < position.distanceTo(getRole().getBot().getPos()));
	}


	private boolean isNeitherKeeperOrI(BotID id)
	{
		return id != getRole().getBotID() && id != getRole().getAiFrame().getKeeperId();
	}


	private Sector getSectorFromPosition(IVector2 position)
	{
		if (position.x() < minDistanceToCenter)
		{
			return Sector.INVALID;
		}

		IVector2 absPos = Vector2.fromXY(position.x(), Math.abs(position.y()));

		// Naming of variables is for blue KI, if the field is aligned horizontally
		IVector2 leftUpperPenCorner = Geometry.getPenaltyAreaTheir().getRectangle()
				.getCorner(IRectangle.ECorner.TOP_LEFT);
		IVector2 upperCorner = Vector2.fromXY(minDistanceToCenter, Geometry.getFieldWidth() * 0.5);
		IVector2 rightUpperPenCorner = Geometry.getPenaltyAreaTheir().getRectangle()
				.getCorner(IRectangle.ECorner.TOP_RIGHT);
		IVector2 goalCenter = Geometry.getGoalTheir().getCenter();
		IVector2 penAreaOnFieldBorder = Vector2.fromXY(leftUpperPenCorner.x(),
				Geometry.getFieldWidth() * 0.5);

		// ECorner sectors will be divided in 3 shapes, one Rectangle two triangles
		// - Sector outside the PenArea is represented by outerTriangle and rectangle
		// - Sector inside the PenArea is represented by innerTriangle

		ITriangle outerTriangle = Triangle.fromCorners(leftUpperPenCorner, upperCorner, penAreaOnFieldBorder);
		ITriangle innerTriangle = Triangle.fromCorners(goalCenter, leftUpperPenCorner, rightUpperPenCorner);
		IRectangle rectangle = Rectangle.fromPoints(leftUpperPenCorner, Geometry.getField().getCorners().get(2));

		// Check position with absolute y to decide if CORNER or MIDDLE sector, then decide which side of the field
		if (outerTriangle.isPointInShape(absPos) || innerTriangle.isPointInShape(absPos)
				|| rectangle.isPointInShape(absPos))
		{
			return position.y() < 0 ? Sector.RIGHT_CORNER : Sector.LEFT_CORNER;
		}
		return position.y() < 0 ? Sector.RIGHT_MIDDLE : Sector.LEFT_MIDDLE;
	}


	private void drawHelpLines()
	{
		// Mark the destination for the bot
		if (destination != null
				&& ((SupportRole) getRole()).getCurrentBehavior() == ESupportBehavior.PENALTY_AREA_ATTACKER)
		{
			shapes.add(new DrawableCircle(Circle.createCircle(destination, Geometry.getBotRadius() * 1.2), Color.RED));
			shapes.add(new DrawableLine(Line.fromPoints(destination, getRole().getPos()), Color.RED));
		}
		// Position where attacker plans to control the ball
		shapes.add(new DrawableCircle(
				Circle.createCircle(passOrigin, Geometry.getBotRadius() * 1.2), Color.YELLOW));

		// Sector borders
		shapes.add(new DrawableLine(Line.fromPoints(
				Vector2.fromXY(minDistanceToCenter, Geometry.getFieldWidth() * (-0.5)),
				Geometry.getPenaltyAreaTheir().getRectangle().getCorner(IRectangle.ECorner.BOTTOM_LEFT)), Color.CYAN));
		shapes.add(new DrawableLine(
				Line.fromPoints(Geometry.getPenaltyAreaTheir().getRectangle().getCorner(IRectangle.ECorner.BOTTOM_LEFT),
						Geometry.getGoalTheir().getCenter()),
				Color.CYAN));

		shapes.add(new DrawableLine(Line.fromPoints(
				Vector2.fromXY(minDistanceToCenter, Geometry.getFieldWidth() * (0.5)),
				Geometry.getPenaltyAreaTheir().getRectangle().getCorner(IRectangle.ECorner.TOP_LEFT)), Color.CYAN));
		shapes.add(new DrawableLine(
				Line.fromPoints(Geometry.getPenaltyAreaTheir().getRectangle().getCorner(IRectangle.ECorner.TOP_LEFT),
						Geometry.getGoalTheir().getCenter()),
				Color.CYAN));

		shapes.add(new DrawableLine(
				Line.fromPoints(Geometry.getGoalTheir().getCenter(), Vector2.fromXY(minDistanceToCenter, 0)), Color.CYAN));

		// Is reasonable distance for Ball
		shapes.add(new DrawableLine(Line.fromPoints(
				Vector2.fromXY(minDistanceToCenter, Geometry.getFieldWidth() * (-0.5)),
				Vector2.fromXY(minDistanceToCenter, Geometry.getFieldWidth() * (0.5))), Color.CYAN));
		// Is reasonable distance for Bot
		IVector2 center = Vector2.fromX(xCordOfPointToCalculateBotActivationDistance);
		ICircle circle = Circle.createCircle(center, xCordOfPointToCalculateBotActivationDistance - minDistanceToCenter);
		List<IVector2> intersectionsA = circle.lineIntersections(Geometry.getField().getEdgesAsSegments().get(1));
		List<IVector2> intersectionsB = circle.lineIntersections(Geometry.getField().getEdgesAsSegments().get(3));
		if (!intersectionsA.isEmpty() && !intersectionsB.isEmpty())
		{
			double startAngle = Vector2.fromPoints(center, intersectionsA.get(0)).getAngle();
			double endAngle = Vector2.fromPoints(center, intersectionsB.get(0)).getAngle();
			DrawableArc arc = new DrawableArc(Arc.createArc(center,
					xCordOfPointToCalculateBotActivationDistance - minDistanceToCenter, startAngle,
					AngleMath.difference(endAngle, startAngle)),
					Color.CYAN);
			arc.setArcType(Arc2D.OPEN);
			shapes.add(arc);
		}
	}


	private boolean isReasonable(TacticalField tacticalField)
	{
		// Is useless if ball is too far away from PenArea
		if (passOrigin.x() < minDistanceToCenter)
		{
			return false;
		}
		// Is useless if opponent team controls the ball
		if (tacticalField.getBallPossession().getEBallPossession() == EBallPossession.THEY)
		{
			return false;
		}
		// Is useless if the ball stays in opponent pen area
		if (Geometry.getPenaltyAreaTheir().isPointInShapeOrBehind(
				getRole().getBall().getPos()) && getRole().getBall().getVel().getLength2() <= 1.0)
		{
			return false;
		}
		// Is useless if bot is too far away from PenArea
		return (getRole().getBot().getPos().distanceTo(
				Vector2.fromX(xCordOfPointToCalculateBotActivationDistance)) <= xCordOfPointToCalculateBotActivationDistance
				- minDistanceToCenter);
	}


	@Override
	public double calculateViability()
	{
		TacticalField tacticalField = getRole().getAiFrame().getTacticalField();
		shapes.clear();
		passFactory.update(getRole().getWFrame());
		ratedPassFactory.update(getRole().getWFrame().getOpponentBots().values());

		passOrigin = passOriginFromAttackerAndBall();
		if (!isActive || !isReasonable(tacticalField))
		{
			destination = null;
			return 0;
		}

		drawHelpLines();

		RatedPosition res = createRatedPosition(tacticalField);

		getRole().getAiFrame().getShapes(EAiShapesLayer.SUPPORT_PENALTY_AREA_ATTACKER).addAll(shapes);

		destination = res.pos;
		return res.score;
	}


	private IVector2 passOriginFromAttackerAndBall()
	{
		var attacker = getRole().getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.map(id -> getRole().getWFrame().getBot(id))
				.orElse(null);
		return attacker != null
				? getRole().getBall().getTrajectory().getTravelLineRolling().closestPointOnLine(attacker.getBotKickerPos())
				: getRole().getBall().getPos();
	}


	private RatedPosition createRatedPosition(TacticalField tacticalField)
	{
		Sector sec = getSectorFromPosition(passOrigin);
		switch (sec)
		{
			case RIGHT_CORNER:
			case LEFT_CORNER:
				return getPassTargetOnOppositePenaltyAreaSide(sec);

			case RIGHT_MIDDLE:
			case LEFT_MIDDLE:
				return getReboundIntersectionPosition(tacticalField);

			default:
				return new RatedPosition();
		}
	}


	@Override
	public void doUpdate()
	{
		if (destination != null)
		{
			skill.updateDestination(destination);
		}
	}


	@Override
	public void doEntryActions()
	{
		skill = MoveToSkill.createMoveToSkill();
		getRole().setNewSkill(skill);
	}


	@Override
	public boolean getIsActive()
	{
		return PenaltyAreaAttacker.isActive();
	}


	private enum Sector
	{
		LEFT_CORNER,
		RIGHT_CORNER,
		LEFT_MIDDLE,
		RIGHT_MIDDLE,
		INVALID
	}

	private static class RatedPosition
	{
		protected final IVector2 pos;
		protected final double score;


		protected RatedPosition()
		{
			pos = null;
			score = 0.;
		}


		protected RatedPosition(IVector2 pos, double score)
		{
			this.pos = pos;
			this.score = score;
		}
	}
}
