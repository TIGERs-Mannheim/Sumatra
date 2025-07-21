/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.movingrobot.IMovingRobot;
import edu.tigers.sumatra.movingrobot.MovingRobotFactory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class SupportBridgePositionCalc extends ACalculator
{
	@Configurable(comment = "Max moving bot Horizon", defValue = "0.8")
	private static double maxHorizon = 0.8;

	@Configurable(defValue = "0.1", comment = "Reaction time of opponent [s] for MovingRobots. Predicts constant velocity in this time.")
	private static double opponentBotReactionTime = 0.1;

	@Configurable(comment = "[mm]", defValue = "9000.")
	private static double maxPassDistance = 9000;

	@Configurable(comment = "[mm]", defValue = "5000.")
	private static double maxShootDistance = 5000;

	@Configurable(comment = "[deg]", defValue = "3.")
	private static double minArcWidth = 3;

	@Configurable(comment = "[deg]", defValue = "15.")
	private static double minAngleToSightLine = 15;

	@Configurable(comment = "[mm]", defValue = "1500.")
	private static double minDistToOffensive = 1500;

	@Configurable(comment = "[deg]", defValue = "17.")
	private static double maxArcWidth = 17;

	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	@Getter
	private List<IArc> offensiveShadows;
	@Getter
	private List<IVector2> supportiveGoalPositions;


	@Override
	public void doCalc()
	{
		Optional<IVector2> passSender = findPassSender();
		if (passSender.isPresent())
		{
			offensiveShadows = generateUncoveredArc(passSender.get(), maxPassDistance);
			supportiveGoalPositions = calcGoalShootingPositions(passSender.get(), offensiveShadows);
			drawPositions(supportiveGoalPositions);
		}
	}


	@Override
	protected void reset()
	{
		offensiveShadows = Collections.emptyList();
		supportiveGoalPositions = Collections.emptyList();
	}


	private List<IVector2> calcGoalShootingPositions(IVector2 passSender, List<IArc> passSenderArcs)
	{
		IVector2 goal = Geometry.getGoalTheir().getCenter();
		List<IArc> goalArcs = generateUncoveredArc(goal, maxShootDistance).stream()
				.filter(a -> AngleMath.diffAbs(a.getStartAngle() + a.getRotation() / 2.,
						passSender.subtractNew(a.center()).getAngle()) > AngleMath.deg2rad(minAngleToSightLine))
				.toList();

		drawArcs(goalArcs);
		return findAllPositions(passSenderArcs, goalArcs);
	}


	private void drawPositions(List<IVector2> bridgePositions)
	{
		List<IDrawableShape> shapes = getShapes(EAiShapesLayer.SUPPORT_ACTIVE_ROLES);
		for (IVector2 pos : bridgePositions)
		{
			DrawableCircle dCircle = new DrawableCircle(pos, Geometry.getBotRadius() / 2, Color.YELLOW);

			shapes.add(dCircle);
		}
	}


	private List<IVector2> findAllPositions(List<IArc> passSenderArcs,
			List<IArc> passReceiverArcs)
	{
		drawArcs(passSenderArcs);
		List<IVector2> bridgePositions = passReceiverArcs
				.parallelStream().map(
						passReceiverArc -> new ArrayList<>(passSenderArcs
								.stream()
								.map(SupportBridgePositionCalc::getPassSenderMidLine)
								.map(passSenderArc -> checkForBridgePosition(passReceiverArc, passSenderArc))
								.filter(Optional::isPresent)
								.map(Optional::get)
								.toList()))
				.flatMap(Collection::stream)
				.toList();

		List<IVector2> offensives = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(EPlay.OFFENSIVE).stream()
				.map(ARole::getPos)
				.toList();

		PointChecker checker = new PointChecker()
				.checkInsideField()
				.checkNotInPenaltyAreas()
				.checkBallDistances()
				.checkConfirmWithKickOffRules()
				.checkCustom(p -> offensives.stream().noneMatch(o -> o.distanceTo(p) < minDistToOffensive));

		return bridgePositions
				.stream()
				.filter(s -> checker.allMatch(getAiFrame(), s))
				.filter(s -> s.distanceTo(passSenderArcs.getFirst().center()) > minDistToOffensive)
				.toList();
	}


	private static ILineSegment getPassSenderMidLine(IArc passSenderArc)
	{
		return Lines.segmentFromPoints(passSenderArc.center(),
				passSenderArc.center()
						.addNew(Vector2.fromAngle(passSenderArc.getStartAngle() + passSenderArc.getRotation() / 2.)
								.scaleTo(passSenderArc.radius())));
	}


	private Optional<IVector2> checkForBridgePosition(IArc passReceiverArc, ILineSegment passSenderMidLine)
	{
		ILineSegment passReceiverMidLine = getPassSenderMidLine(passReceiverArc);

		Optional<IVector2> intersection = passReceiverMidLine.intersect(passSenderMidLine).asOptional();
		if (intersection.isPresent())
		{
			if (Geometry.getPenaltyAreaTheir().isPointInShape(intersection.get()))
			{
				List<IVector2> intersections = Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() * 1.5)
						.intersectPerimeterPath(passReceiverMidLine);

				return intersections.stream().min(Comparator.comparingDouble(IVector::x));

			}
			return intersection;
		} else
		{
			List<IVector2> intersections = passReceiverArc.intersectPerimeterPath(passSenderMidLine);
			if (!intersections.isEmpty())
			{
				IVector2 firstIntersection = intersections.get(0);
				if (intersections.size() == 2 && passReceiverArc.getRotation() < Math.PI)
				{
					IVector2 secondIntersection = intersections.get(1);
					return Optional.of(Lines.segmentFromPoints(firstIntersection, secondIntersection).getPathCenter());
				}

			}
		}
		return Optional.empty();

	}


	private Optional<IVector2> findPassSender()
	{
		return kickOrigins.get().values().stream()
				.min(Comparator.comparingDouble(e -> e.getPos().distanceTo(getWFrame().getBall().getPos())))
				.map(KickOrigin::getPos);
	}


	private List<IArc> generateUncoveredArc(IVector2 center, double radius)
	{
		Map<BotID, IMovingRobot> movingRobots = getAiFrame().getWorldFrame().getOpponentBots().values().stream()
				.filter(b -> b.getPos().distanceTo(center) < radius)
				.collect(Collectors.toMap(ITrackedBot::getBotId,
						bot -> MovingRobotFactory.acceleratingRobot(
								bot.getPos(),
								bot.getVel(),
								bot.getRobotInfo().getBotParams().getMovementLimits().getVelMax(),
								bot.getRobotInfo().getBotParams().getMovementLimits().getAccMax(),
								Geometry.getBotRadius() + Geometry.getBallRadius(),
								opponentBotReactionTime
						)));

		FullAngleRangeGenerator generator = new FullAngleRangeGenerator(
				new ArrayList<>(movingRobots.values()),
				center,
				getWFrame().getBall().getStraightConsultant(),
				maxHorizon
		);

		List<AngleRange> uncoveredAngleRanges = generator.getUncoveredAngleRanges();

		return uncoveredAngleRanges.stream().map(
						ar -> Arc.createArc(center, radius, ar.getRight(),
								ar.getLeft() - ar.getRight()))
				.map(this::splitArc)
				.flatMap(Collection::stream)
				.filter(a -> a.getRotation() > AngleMath.deg2rad(minArcWidth))
				.toList();
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
		List<IDrawableShape> shapes = getShapes(EAiShapesLayer.SUPPORT_ANGLE_RANGE);
		arcs.forEach(a -> shapes.add(new DrawableArc(a, Color.GRAY)));
	}
}
