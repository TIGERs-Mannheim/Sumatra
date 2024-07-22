/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePenAreaTargetGroup;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.boundary.IShapeBoundary;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;


@Log4j2
@RequiredArgsConstructor
public class DefensePenAreaTargetGroupFinderCalc extends ACalculator
{
	@Configurable(defValue = "5.0", comment = "Distance offset to add to bot radius to determine same cluster of bots")
	private static double clusterDistanceOffset = 5.0;

	@Configurable(comment = "Distance between the bots", defValue = "10.0")
	private static double distBetweenBots = 10.0;

	@Configurable(comment = "Gain factor for threat velocity in ProtectionState, high gain => high overshoot, low gain => defender lags behind", defValue = "0.75")
	private static double mimicThreatVelocityGain = 0.75;

	private final Supplier<List<DefenseThreatAssignment>> defensePenAreaThreatAssignments;
	private final Supplier<Set<BotID>> penAreaDefender;
	private final Supplier<IShapeBoundary> penAreaBoundary;

	@Getter
	private List<DefensePenAreaTargetGroup> defensePenAreaTargetGroups;


	@Override
	protected void doCalc()
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		boolean ballInPenArea = Geometry.getPenaltyAreaOur().withMargin(300).isPointInShapeOrBehind(ballPos);
		List<DefenseThreatAssignment> threatAssignments = ballInPenArea && getAiFrame().getGameState().isBallPlacement() ?
				Collections.emptyList() :
				defensePenAreaThreatAssignments.get();

		List<DefensePenAreaTargetGroup> reducedTargetGroups = reduceThreatAssignmentsToTargetGroups(threatAssignments);
		drawReducedTargetGroups(reducedTargetGroups);

		List<PenAreaSpaces> spaces = createPenAreaSpaces(reducedTargetGroups);
		drawSpaces(spaces);

		defensePenAreaTargetGroups = addTargetsFromSpacesToTargetGroups(reducedTargetGroups, spaces);
		drawAllSortedTargetGroups(defensePenAreaTargetGroups);
	}


	/**
	 * Reduce the list of threat assignments until they match the available number of defenders
	 *
	 * @param threatAssignments all threat assignments reported by Metis
	 * @return the reduce list of target groups
	 */
	private List<DefensePenAreaTargetGroup> reduceThreatAssignmentsToTargetGroups(
			final List<DefenseThreatAssignment> threatAssignments)
	{
		List<DefenseThreatAssignment> reducedAssignments = new ArrayList<>(threatAssignments);
		int count = 0;
		do
		{
			var targets = sortThreatAssignments(reducedAssignments);
			var targetClusters = getTargetClusters(targets);
			var reducedTargets = reduceClustersToTargets(targetClusters);

			int nUsedBots = reducedTargets.stream().mapToInt(tg -> tg.moveDestinations().size()).sum();
			if (finishedBuildingTargetGroups(nUsedBots, reducedTargets))
			{
				return reducedTargets;
			}
			reducedAssignments.remove(reducedAssignments.size() - 1);
			++count;
			if (count > 50)
			{
				log.warn("reduceThreatAssignmentsToTargetGroups did not converge");
				return reducedTargets;
			}
		} while (true);
	}


	private boolean finishedBuildingTargetGroups(int nUsedBots, List<DefensePenAreaTargetGroup> groups)
	{
		for (int i = 1; i < groups.size(); ++i)
		{
			var lastGroup = groups.get(i - 1);
			var group = groups.get(i);
			var allowedDist =
					(Geometry.getBotRadius() + clusterDistanceOffset) * (Geometry.getBotRadius() + clusterDistanceOffset);
			var groupOkay = group.moveDestinations().stream()
					.noneMatch(current -> lastGroup.moveDestinations().stream()
							.anyMatch(last -> current.distanceToSqr(last) < allowedDist));
			if (!groupOkay)
			{
				return false;
			}
		}
		return nUsedBots <= penAreaDefender.get().size();
	}


	/**
	 * @param threats unsorted threat assignments
	 * @return threat assignments sorted along the penalty area boundary from start to end
	 */
	private List<DefenseThreatAssignment> sortThreatAssignments(final List<DefenseThreatAssignment> threats)
	{
		return threats.stream()
				.sorted((r1, r2) -> penAreaBoundary.get().compare(r1.getThreat().getPos(), r2.getThreat().getPos()))
				.toList();
	}


	/**
	 * Build clusters of threats that are close together and should be protected only once on the penalty area
	 *
	 * @param threatAssignments
	 * @return
	 */
	private List<List<DefenseThreatAssignment>> getTargetClusters(final List<DefenseThreatAssignment> threatAssignments)
	{
		List<List<DefenseThreatAssignment>> targetClusters = new ArrayList<>();
		for (DefenseThreatAssignment threatAssignment : threatAssignments)
		{
			List<DefenseThreatAssignment> targetCluster;
			IVector2 target = getTargetOnPenaltyArea(threatAssignment.getThreat());
			if (targetClusters.isEmpty())
			{
				targetCluster = new ArrayList<>();
				targetClusters.add(targetCluster);
			} else
			{
				List<DefenseThreatAssignment> lastTargetCluster = targetClusters.get(targetClusters.size() - 1);
				DefenseThreatAssignment lastThreatAssignment = lastTargetCluster.get(lastTargetCluster.size() - 1);
				IVector2 lastThreat = getTargetOnPenaltyArea(lastThreatAssignment.getThreat());
				int nDefender = threatAssignment.getBotIds().size() + lastThreatAssignment.getBotIds().size();
				double sameClusterDistance = (Geometry.getBotRadius() + clusterDistanceOffset) * nDefender;

				if (penAreaBoundary.get().distanceBetween(target, lastThreat) < sameClusterDistance)
				{
					targetCluster = lastTargetCluster;
				} else
				{
					targetCluster = new ArrayList<>();
					targetClusters.add(targetCluster);
				}
			}
			targetCluster.add(threatAssignment);
		}
		return targetClusters;
	}


	private IVector2 getTargetOnPenaltyArea(IDefenseThreat threat)
	{
		if (threat.getType() == EDefenseThreatType.BALL && penAreaBoundary.get().isPointInShape(threat.getPos()))
		{
			return penAreaBoundary.get().closestPoint(threat.getPos());
		}
		return penAreaBoundary.get().projectPoint(threat.getThreatLine().getPathEnd(), threat.getThreatLine().getPathStart());
	}


	private List<DefensePenAreaTargetGroup> reduceClustersToTargets(
			final List<List<DefenseThreatAssignment>> targetClusters)
	{
		List<DefensePenAreaTargetGroup> reducedTargets = new ArrayList<>(targetClusters.size());
		// 0 = most important; Higher is less important
		int priority = 0;
		for (List<DefenseThreatAssignment> targetCluster : sortTargetClustersByPriority(targetClusters))
		{
			var numBotsToUse = numBotsForCluster(targetCluster);
			var target = targetOfCluster(targetCluster);
			var subTargets = subTargetsAroundCenter(target, numBotsToUse);
			var threats = targetCluster.stream().map(DefenseThreatAssignment::getThreat).toList();
			var velAdaptedMoveDest = mimicThreatGroupVelocity(threats, subTargets);
			var velAdaptedCenterDest = mimicThreatGroupVelocity(threats, List.of(target)).get(0);
			var targetGroup = DefensePenAreaTargetGroup.fromTargetCluster(target, velAdaptedCenterDest, subTargets,
					velAdaptedMoveDest, threats, priority++);
			reducedTargets.add(targetGroup);
			drawTargetGroupThreatAssignments(targetCluster, targetGroup);
		}
		return reducedTargets;
	}


	private List<IVector2> mimicThreatGroupVelocity(List<IDefenseThreat> threats, List<IVector2> moveDestinations)
	{
		// mimic threat velocity at protection line => no more lag of defenders
		double smallestAcc = penAreaDefender.get().stream()
				.map(botID -> getWFrame().getBot(botID))
				.filter(Objects::nonNull)
				.map(ITrackedBot::getMoveConstraints)
				.mapToDouble(MoveConstraints::getAccMax)
				.min().orElseThrow();
		var ballThreats = threats.stream()
				.filter(t -> t.getType() == EDefenseThreatType.BALL)
				.toList();
		var relevantThreats = ballThreats.isEmpty() ? threats : ballThreats;
		var averageVel = relevantThreats.stream()
				.map(IDefenseThreat::getVel)
				.reduce(IVector2::addNew)
				.map(vel -> vel.multiplyNew(1.0 / relevantThreats.size()))
				.orElseThrow();

		double timeToBrake = averageVel.getLength2() / smallestAcc;
		double brakeDistance = 0.5 * smallestAcc * timeToBrake * timeToBrake * 1000.0;

		var offset = averageVel.scaleTo(mimicThreatVelocityGain * brakeDistance);

		return moveDestinations.stream()
				.map(dest -> dest.addNew(offset))
				.map(dest -> penAreaBoundary.get().closestPoint(dest))
				.toList();
	}


	private List<List<DefenseThreatAssignment>> sortTargetClustersByPriority(
			List<List<DefenseThreatAssignment>> targetCluster)
	{
		return targetCluster.stream()
				.sorted(Comparator.comparingDouble(this::getNegativeThreatRating))
				.toList();
	}


	private double getNegativeThreatRating(List<DefenseThreatAssignment> defenseThreatAssignments)
	{
		// Return the negative threat rating to ensure the highest threat is at the beginning of the list
		return -defenseThreatAssignments.stream().mapToDouble(dta -> dta.getThreat().getThreatRating()).max().orElse(0.0);
	}


	private int numBotsForCluster(final List<DefenseThreatAssignment> targetCluster)
	{
		return targetCluster.stream().mapToInt(dta -> dta.getBotIds().size()).sum();
	}


	private IVector2 targetOfCluster(final List<DefenseThreatAssignment> targetCluster)
	{
		return penAreaBoundary.get().projectPoint(Geometry.getGoalOur().getCenter(), centerOfThreats(targetCluster));
	}


	private IVector2 centerOfThreats(final List<DefenseThreatAssignment> targetCluster)
	{
		Optional<DefenseThreatAssignment> ballAssignment = targetCluster.stream()
				.filter(ta -> !ta.getThreat().getObjectId().isBot()).findFirst();
		if (ballAssignment.isPresent())
		{
			// if the ball is part of the threat cluster, focus on the ball, not the center of all threats
			return ballAssignment.get().getThreat().getPos();
		}
		IVector2 first = targetCluster.get(0).getThreat().getPos();
		IVector2 last = targetCluster.get(targetCluster.size() - 1).getThreat().getPos();
		double distance = first.distanceTo(last);
		return LineMath.stepAlongLine(first, last, distance / 2);
	}


	private List<IVector2> subTargetsAroundCenter(final IVector2 target, final int numBots)
	{
		if (numBots == 0)
		{
			return List.of();
		}

		var requiredSpacePerSidePerRobot = Geometry.getBotRadius() + distBetweenBots / 2;
		var requiredSpacePerRobot = 2 * requiredSpacePerSidePerRobot;
		var requiredSpacePerSide = numBots * requiredSpacePerSidePerRobot - distBetweenBots / 2;
		var boundary = penAreaBoundary.get();
		var distToStart = boundary.distanceFromStart(target);
		var distToEnd = boundary.distanceFromEnd(target);
		double first;
		if (distToStart < requiredSpacePerSide)
		{
			first = 0;
		} else if (distToEnd < requiredSpacePerSide)
		{
			first = boundary.getShape().getPerimeterLength() - (numBots - 1) * requiredSpacePerRobot;
		} else
		{
			first = distToStart - requiredSpacePerSidePerRobot * (numBots - 1);
		}
		List<IVector2> subTargets = new ArrayList<>(numBots);
		for (int i = 0; i < numBots; ++i)
		{
			subTargets.add(
					boundary.stepAlongBoundary(first + i * requiredSpacePerRobot).orElseThrow()
			);
		}
		return subTargets;
	}


	/**
	 * Create penalty area spaces and fill them with the number of remaining defenders
	 *
	 * @param reducedTargetGroups current target groups
	 * @return all spaces
	 */
	private List<PenAreaSpaces> createPenAreaSpaces(final List<DefensePenAreaTargetGroup> reducedTargetGroups)
	{
		List<IVector2> penAreaMarkers = createPenAreaMarkers(reducedTargetGroups);
		List<PenAreaSpaces> spaces = penAreaMarkersToSpaces(penAreaMarkers);
		assignDefenderToSpaces(reducedTargetGroups, spaces);
		return spaces;
	}


	private List<IVector2> createPenAreaMarkers(final List<DefensePenAreaTargetGroup> reducedTargetGroups)
	{
		List<IVector2> penAreaMarkers = new ArrayList<>(reducedTargetGroups.size() + 2);
		penAreaMarkers.addAll(
				reducedTargetGroups.stream().map(DefensePenAreaTargetGroup::moveDestinations).flatMap(List::stream)
						.toList());
		penAreaMarkers.add(penAreaBoundary.get().getStart());
		penAreaMarkers.add(penAreaBoundary.get().getEnd());
		penAreaMarkers.sort(penAreaBoundary.get());
		return penAreaMarkers;
	}


	private List<PenAreaSpaces> penAreaMarkersToSpaces(final List<IVector2> penAreaMarkers)
	{
		List<PenAreaSpaces> spaces = new ArrayList<>();
		IVector2 lastMarker = penAreaMarkers.remove(0);
		for (IVector2 marker : penAreaMarkers)
		{
			spaces.add(new PenAreaSpaces(lastMarker, marker));
			lastMarker = marker;
		}
		return spaces;
	}


	private void assignDefenderToSpaces(final List<DefensePenAreaTargetGroup> reducedTargetGroups,
			final List<PenAreaSpaces> spaces)
	{
		int defenderDestinationSum = reducedTargetGroups.stream().mapToInt(g -> g.moveDestinations().size()).sum();
		int remainingDefender = penAreaDefender.get().size() - defenderDestinationSum;
		for (int i = 0; i < remainingDefender; i++)
		{
			spaces.sort(Comparator.comparingDouble(PenAreaSpaces::distByNumTargets).reversed());
			spaces.get(0).numTargets++;
		}
	}


	private List<DefensePenAreaTargetGroup> addTargetsFromSpacesToTargetGroups(
			final List<DefensePenAreaTargetGroup> reducedTargetGroups, final List<PenAreaSpaces> spaces)
	{
		List<DefensePenAreaTargetGroup> targetGroups = new ArrayList<>(reducedTargetGroups);
		for (PenAreaSpaces space : spaces)
		{
			targetGroups.addAll(spreadTargetsOnSpace(space));
		}
		return Collections.unmodifiableList(targetGroups);
	}


	/**
	 * Spread new target groups equally on the given space
	 *
	 * @param space the space to spread on
	 * @return the resulting target groups
	 */
	private List<DefensePenAreaTargetGroup> spreadTargetsOnSpace(final PenAreaSpaces space)
	{
		double width = penAreaBoundary.get().distanceBetween(space.start, space.end);
		double step = width / (space.numTargets + 1);

		List<DefensePenAreaTargetGroup> groups = new ArrayList<>(space.numTargets);
		IVector2 next = space.start;
		for (int i = 0; i < space.numTargets; i++)
		{
			next = penAreaBoundary.get().stepAlongBoundary(next, step).orElseThrow(IllegalStateException::new);
			groups.add(DefensePenAreaTargetGroup.fromSpace(next));
		}
		return groups;
	}


	private List<IDrawableShape> getShapes()
	{
		return getShapes(EAiShapesLayer.DEFENSE_PENALTY_AREA_GROUP_FINDER);
	}


	private void drawAllSortedTargetGroups(final List<DefensePenAreaTargetGroup> allTargetsSorted)
	{
		allTargetsSorted.forEach(
				t -> getShapes().add(new DrawableCircle(Circle.createCircle(t.centerDest(), 60), Color.magenta)));
		allTargetsSorted.forEach(t -> getShapes().add(
				new DrawableAnnotation(t.centerDest(), String.valueOf(t.priority()), Color.magenta).withCenterHorizontally(
						true)));
	}


	private void drawReducedTargetGroups(final List<DefensePenAreaTargetGroup> reducedTargets)
	{
		reducedTargets.forEach(
				t -> getShapes().add(new DrawableCircle(Circle.createCircle(t.centerDest(), 30), Color.blue)));
		reducedTargets.forEach(t -> t.moveDestinations()
				.forEach(m -> getShapes().add(new DrawableLine(t.centerDest(), m, Color.blue))));
		reducedTargets.forEach(t -> t.moveDestinations()
				.forEach(m -> getShapes().add(new DrawableCircle(Circle.createCircle(m, 20), Color.blue))));
	}


	private void drawTargetGroupThreatAssignments(List<DefenseThreatAssignment> penAreaThreatAssignments,
			DefensePenAreaTargetGroup targetGroup)
	{
		penAreaThreatAssignments.forEach(
				tc -> getShapes().add(new DrawableLine(tc.getThreat().getPos(), targetGroup.centerDest(), Color.WHITE)));
	}


	private void drawSpaces(final List<PenAreaSpaces> spaces)
	{
		spaces.forEach(s -> getShapes().add(new DrawableLine(s.start, s.end, Color.orange)));
		spaces.forEach(s -> getShapes().add(
				new DrawableAnnotation(Lines.segmentFromPoints(s.start, s.end).getPathCenter(),
						String.valueOf(s.numTargets),
						Color.orange).withCenterHorizontally(true).withOffset(Vector2.fromY(-100))));
	}


	private class PenAreaSpaces
	{
		final IVector2 start;
		final IVector2 end;
		int numTargets = 0;


		public PenAreaSpaces(final IVector2 start, final IVector2 end)
		{
			this.start = start;
			this.end = end;
		}


		double distByNumTargets()
		{
			return penAreaBoundary.get().distanceBetween(start, end) / (numTargets + 1);
		}
	}
}
