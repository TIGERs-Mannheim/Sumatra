/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePenAreaTargetGroup;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.LineMath;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.PenAreaBoundary;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class DefensePenAreaTargetGroupFinderCalc extends ACalculator
{
	@Configurable(defValue = "5.0", comment = "Distance offset to add to bot radius to determine same cluster of bots")
	private static double clusterDistanceOffset = 5.0;

	@Configurable(comment = "Distance between the bots", defValue = "10.0")
	private static double distBetweenBots = 10.0;

	@Configurable(defValue = "80.0", comment = "Extra margin to default penArea margin (must be >0 to avoid bad path planning)")
	private static double penAreaExtraMargin = 80.0;


	private final Supplier<List<DefenseThreatAssignment>> defensePenAreaThreatAssignments;
	private final Supplier<Set<BotID>> penAreaDefender;

	@Getter
	private List<DefensePenAreaTargetGroup> defensePenAreaTargetGroups;
	@Getter
	private PenAreaBoundary penAreaBoundary;


	@Override
	protected void doCalc()
	{
		penAreaBoundary = PenAreaBoundary.ownWithMargin(Geometry.getBotRadius() + penAreaExtraMargin);

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
		do
		{
			List<DefenseThreatAssignment> targets = sortThreatAssignments(reducedAssignments);
			List<List<DefenseThreatAssignment>> targetClusters = getTargetClusters(targets);
			List<DefensePenAreaTargetGroup> reducedTargets = reduceClustersToTargets(targetClusters);

			int nUsedBots = reducedTargets.stream().mapToInt(tg -> tg.moveDestinations().size()).sum();
			if (nUsedBots <= penAreaDefender.get().size())
			{
				return reducedTargets;
			}
			reducedAssignments.remove(reducedAssignments.size() - 1);
		} while (true);
	}


	/**
	 * @param threats unsorted threat assignments
	 * @return threat assignments sorted along the penalty area boundary from start to end
	 */
	private List<DefenseThreatAssignment> sortThreatAssignments(final List<DefenseThreatAssignment> threats)
	{
		return threats.stream()
				.sorted((r1, r2) -> penAreaBoundary.compare(r1.getThreat().getPos(), r2.getThreat().getPos()))
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
				if (target.distanceTo(lastThreat) < sameClusterDistance)
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
		return penAreaBoundary.projectPoint(threat.getThreatLine().getEnd(), threat.getThreatLine().getStart());
	}


	private List<DefensePenAreaTargetGroup> reduceClustersToTargets(
			final List<List<DefenseThreatAssignment>> targetClusters)
	{
		List<DefensePenAreaTargetGroup> reducedTargets = new ArrayList<>(targetClusters.size());
		int priority = 0;
		for (List<DefenseThreatAssignment> targetCluster : targetClusters)
		{
			var numBotsToUse = numBotsForCluster(targetCluster);
			var target = targetOfCluster(targetCluster);
			var subTargets = subTargetsAroundCenter(target, numBotsToUse);
			var threats = targetCluster.stream().map(DefenseThreatAssignment::getThreat).toList();
			var targetGroup = DefensePenAreaTargetGroup.fromTargetCluster(target, subTargets, threats, priority++);
			reducedTargets.add(targetGroup);
			drawTargetGroupThreatAssignments(targetCluster, targetGroup);
		}
		return reducedTargets;
	}


	private int numBotsForCluster(final List<DefenseThreatAssignment> targetCluster)
	{
		return targetCluster.stream().mapToInt(dta -> dta.getBotIds().size()).sum();
	}


	private IVector2 targetOfCluster(final List<DefenseThreatAssignment> targetCluster)
	{
		return penAreaBoundary.projectPoint(centerOfThreats(targetCluster));
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
		List<IVector2> subTargets = new ArrayList<>(numBots);
		if (numBots == 0)
		{
			return subTargets;
		} else if (numBots % 2 == 0)
		{
			double margin = Geometry.getBotRadius() + distBetweenBots / 2;
			IVector2 nextNegative = nextOnChain(target, margin, -1);
			subTargets.add(nextNegative);
			subTargets.addAll(chainBotTargets(nextNegative, numBots / 2 - 1, -1));
			subTargets.addAll(chainBotTargets(nextNegative, numBots / 2, 1));
		} else
		{
			subTargets.add(target);
			subTargets.addAll(chainBotTargets(target, (numBots - 1) / 2, -1));
			subTargets.addAll(chainBotTargets(target, (numBots - 1) / 2, +1));
		}
		subTargets.sort(((p1, p2) -> penAreaBoundary.compare(p1, p2)));
		return subTargets;
	}


	private List<IVector2> chainBotTargets(final IVector2 start, final int count, final int direction)
	{
		List<IVector2> chain = new ArrayList<>(count);
		double margin = Geometry.getBotRadius() * 2 + distBetweenBots;
		IVector2 last = start;
		for (int i = 0; i < count; i++)
		{
			last = nextOnChain(last, margin, direction);
			chain.add(last);
		}
		return chain;
	}


	private IVector2 nextOnChain(final IVector2 last, final double margin, final int direction)
	{
		Optional<IVector2> next = penAreaBoundary.nextTo(last, margin, direction);
		if (next.isPresent())
		{
			return next.get();
		} else if (direction > 0)
		{
			return penAreaBoundary.getEnd();
		} else
		{
			return penAreaBoundary.getStart();
		}
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
		penAreaMarkers.add(penAreaBoundary.getStart());
		penAreaMarkers.add(penAreaBoundary.getEnd());
		penAreaMarkers.sort(penAreaBoundary);
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
		double width = penAreaBoundary.distanceBetween(space.start, space.end);
		double step = width / (space.numTargets + 1);

		List<DefensePenAreaTargetGroup> groups = new ArrayList<>(space.numTargets);
		IVector2 next = space.start;
		for (int i = 0; i < space.numTargets; i++)
		{
			next = penAreaBoundary.stepAlongBoundary(next, step).orElseThrow(IllegalStateException::new);
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
				.forEach(m -> getShapes().add(new DrawableLine(Line.fromPoints(t.centerDest(), m), Color.blue))));
		reducedTargets.forEach(t -> t.moveDestinations()
				.forEach(m -> getShapes().add(new DrawableCircle(Circle.createCircle(m, 20), Color.blue))));
	}


	private void drawTargetGroupThreatAssignments(List<DefenseThreatAssignment> penAreaThreatAssignments,
			DefensePenAreaTargetGroup targetGroup)
	{
		penAreaThreatAssignments.forEach(tc -> getShapes().add(
				new DrawableLine(Lines.segmentFromPoints(tc.getThreat().getPos(), targetGroup.centerDest()), Color.WHITE)));
	}


	private void drawSpaces(final List<PenAreaSpaces> spaces)
	{
		spaces.forEach(s -> getShapes().add(new DrawableLine(Line.fromPoints(s.start, s.end), Color.orange)));
		spaces.forEach(s -> getShapes().add(
				new DrawableAnnotation(Lines.segmentFromPoints(s.start, s.end).getCenter(), String.valueOf(s.numTargets),
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
			return penAreaBoundary.distanceBetween(start, end) / (numTargets + 1);
		}
	}
}
