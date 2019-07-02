/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.repulsive.RepulsiveAttacker;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.obstacles.MovingRobot;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public class BridgePositionCalc extends ACalculator
{
	@Configurable(comment = "Max moving bot Horizon", defValue = "1.")
	private static double maxHorizon = 1;

	@Configurable(comment = "[mm]", defValue = "6000.")
	private static double maxPassDistance = 6000;

	@Configurable(comment = "[mm]", defValue = "4000.")
	private static double maxShootDistance = 4000;

	@Configurable(comment = "[deg]", defValue = "10.")
	private static double minArcWidth = 10;

	@Configurable(comment = "[deg]", defValue = "15.")
	private static double minAngleToSightLine = 15;

	@Configurable(comment = "[mm]", defValue = "500.")
	private static double minDistToOffensive = 500;

	@Configurable(comment = "[deg", defValue = "40.")
	private static double maxArcWidth = 40;


	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		Optional<IVector2> passSender = findPassSender();
		if (passSender.isPresent())
		{
			List<IArc> passSenderArcs = generateUncoveredArc(passSender.get(), maxPassDistance);
			newTacticalField.setOffensiveShadows(passSenderArcs);
			calcGoalShootingPositions(passSender.get(), passSenderArcs);
		}

	}


	private void calcGoalShootingPositions(IVector2 passSender, List<IArc> passSenderArcs)
	{
		IVector2 goal = Geometry.getGoalTheir().getCenter();
		List<IArc> goalArcs = generateUncoveredArc(goal, maxShootDistance).stream()
				.filter(a -> Math.abs(AngleMath.difference(a.getStartAngle() + a.getRotation() / 2.,
						passSender.subtractNew(a.center()).getAngle())) > AngleMath.deg2rad(minAngleToSightLine))
				.collect(Collectors.toList());

		List<IVector2> goalPositions = findAllPositions(passSenderArcs, goalArcs);
		getNewTacticalField().setSupportiveGoalPositions(goalPositions);
		drawPositions(goalPositions);
		drawArcs(goalArcs);
	}


	@SuppressWarnings("squid:UnusedPrivateMethod")
	private void calcBridgePositions(IVector2 passSender, List<IArc> passSenderArcs)
	{
		List<IVector2> passReceiver = findPassReceiver(passSender);
		List<IArc> passReceiverArcs = passReceiver.stream()
				.map(s -> generateUncoveredArc(s, maxPassDistance))
				.flatMap(Collection::stream)
				.filter(a -> AngleMath.difference(a.getStartAngle() + a.getRotation() / 2.,
						passSender.subtractNew(a.center()).getAngle()) < AngleMath.deg2rad(minAngleToSightLine))
				.collect(Collectors.toList());

		List<IVector2> bridgePositions = findAllPositions(passSenderArcs, passReceiverArcs);
		drawPositions(bridgePositions);
		drawArcs(passReceiverArcs);
	}


	private void drawPositions(List<IVector2> bridgePositions)
	{
		List<IDrawableShape> shapes = getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.SUPPORT_ACTIVE_ROLES);
		for (IVector2 pos : bridgePositions)
		{
			DrawableCircle dCircle = new DrawableCircle(pos, Geometry.getBotRadius() / 2, Color.YELLOW);

			shapes.add(dCircle);
		}
	}


	private List<IVector2> findAllPositions(List<IArc> passSenderArcs,
			List<IArc> passReceiverArcs)
	{
		List<IVector2> bridgePositions = new ArrayList<>();
		for (IArc passReceiverArc : passReceiverArcs)
		{
			drawArcs(passSenderArcs);
			for (IArc passSenderArc : passSenderArcs)
			{

				ILineSegment passSenderMidLine = Lines.segmentFromPoints(passSenderArc.center(),
						passSenderArc.center()
								.addNew(Vector2.fromAngle(passSenderArc.getStartAngle() + passSenderArc.getRotation() / 2.)
										.scaleTo(passSenderArc.radius())));
				checkForBridgePosition(passReceiverArc, passSenderMidLine)
						.ifPresent(bridgePositions::add);
			}
		}
		List<IVector2> offensives = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(EPlay.OFFENSIVE).stream()
				.map(ARole::getPos)
				.collect(Collectors.toList());

		PointChecker checker = new PointChecker()
				.checkInsideField()
				.checkNotInPenaltyAreas()
				.checkBallDistances()
				.checkConfirmWithKickOffRules()
				.checkCustom(p -> offensives.stream().noneMatch(o -> o.distanceTo(p) < minDistToOffensive));

		return bridgePositions.stream()
				.filter(s -> checker.allMatch(getAiFrame(), s))
				.filter(s -> s.distanceTo(passSenderArcs.get(0).center()) > minDistToOffensive)
				.collect(Collectors.toList());
	}


	private Optional<IVector2> checkForBridgePosition(IArc passReceiverArc, ILineSegment passSenderMidLine)
	{
		ILineSegment passReceiverMidLine = Lines.segmentFromPoints(passReceiverArc.center(),
				passReceiverArc.center()
						.addNew(Vector2.fromAngle(passReceiverArc.getStartAngle() + passReceiverArc.getRotation() / 2.)
								.scaleTo(passReceiverArc.radius())));

		Optional<IVector2> intersection = passReceiverMidLine.intersectSegment(passSenderMidLine);
		if (intersection.isPresent())
		{
			if (Geometry.getPenaltyAreaTheir().isPointInShape(intersection.get()))
			{
				List<IVector2> intersections = Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() * 1.5)
						.lineIntersections(passReceiverMidLine);

				return intersections.stream().min(Comparator.comparingDouble(IVector::x));

			}
			return intersection;
		} else
		{
			List<IVector2> intersections = passReceiverArc.lineIntersections(passSenderMidLine);
			if (!intersections.isEmpty())
			{
				IVector2 firstIntersection = intersections.get(0);
				if (intersections.size() == 2 && passReceiverArc.getRotation() < Math.PI)
				{
					IVector2 secondIntersection = intersections.get(1);
					return Optional.of(Lines.segmentFromPoints(firstIntersection, secondIntersection).getCenter());
				}

			}
		}
		return Optional.empty();

	}


	private Optional<IVector2> findPassSender()
	{
		Optional<BotID> attackerID = getNewTacticalField().getOffensiveStrategy().getAttackerBot();
		return attackerID.map(botID -> getWFrame().getBot(botID).getPos());
	}


	@SuppressWarnings("squid:S1872") // No other way
	private List<IVector2> findPassReceiver(IVector2 passSender)
	{
		List<ARole> supporter = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(EPlay.SUPPORT);

		Optional<IVector2> sup = supporter.stream()
				.filter(s -> s.getCurrentState().getIdentifier().equals(RepulsiveAttacker.class.getSimpleName()))
				.map(ARole::getPos)
				.max(Comparator.comparingDouble(a -> a.distanceTo(passSender)));

		if (sup.isPresent())
		{
			ArrayList<IVector2> pos = new ArrayList<>();
			pos.add(sup.get());
			return pos;
		}

		return Collections.emptyList();
	}


	private List<IArc> generateUncoveredArc(IVector2 center, double radius)
	{

		Map<BotID, MovingRobot> movingRobots = getAiFrame().getWorldFrame().getFoeBots().values().stream()
				.filter(b -> b.getPos().distanceTo(center) < radius)
				.collect(Collectors.toMap(ITrackedBot::getBotId,
						bot -> new MovingRobot(bot, maxHorizon, Geometry.getBotRadius() + Geometry.getBallRadius())));

		FullAngleRangeGenerator generator = new FullAngleRangeGenerator(
				new ArrayList<>(movingRobots.values()), center, getWFrame().getBall().getStraightConsultant());

		List<AngleRange> uncoveredAngleRanges = generator.getUncoveredAngleRanges();

		return uncoveredAngleRanges.stream().map(
				ar -> Arc.createArc(center, radius, ar.getRightAngle(),
						ar.getLeftAngle() - ar.getRightAngle()))
				.map(this::splitArc)
				.flatMap(Collection::stream)
				.filter(a -> a.getRotation() > AngleMath.deg2rad(minArcWidth))
				.collect(Collectors.toList());
	}


	private List<IArc> splitArc(IArc arc)
	{
		double angleThresh = AngleMath.deg2rad(maxArcWidth);
		List<IArc> newArcs = new ArrayList<>(2);
		int newNumberOfArcs = (int) (arc.getRotation() / angleThresh) + 1;
		double newRotation = arc.getRotation() / newNumberOfArcs;
		for (int i = 0; i < newNumberOfArcs; i++)
		{
			newArcs.add(Arc.createArc(arc.center(), arc.radius(),
					AngleMath.normalizeAngle(arc.getStartAngle() + i * newRotation), newRotation));
		}
		return newArcs;
	}


	private void drawArcs(List<IArc> arcs)
	{
		List<IDrawableShape> shapes = getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.SUPPORT_ANGLE_RANGE);
		arcs.forEach(a -> shapes.add(new DrawableArc(a, Color.GRAY)));
	}
}
