/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPenAreaRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.PenAreaBoundary;


/**
 * The group containing all penArea bots
 */
public class PenAreaGroup extends ADefenseGroup
{
	@Configurable(defValue = "5.0", comment = "Distance offset to add to bot radius to determine same cluster of bots")
	private static double clusterDistanceOffset = 5.0;

	@Configurable(defValue = "350.0", comment = "Distance when a bot is considered close enough that other bots can leave the protected target")
	private static double interchangeDist = 350.0;

	@Configurable(comment = "Distance between the bots", defValue = "20.0")
	private static double distBetweenBots = 20.0;

	@Configurable(defValue = "10.0", comment = "Extra margin to default penArea margin (must be >0 to avoid bad pathplanning)")
	private static double penAreaExtraMargin = 10.0;


	static
	{
		ConfigRegistration.registerClass("plays", PenAreaGroup.class);
	}

	private AthenaAiFrame aiFrame;
	private PenAreaBoundary penAreaBoundary;


	@Override
	public void assignRoles()
	{
		getRoles().stream()
				.filter(sdr -> sdr.getOriginalRole().getType() != ERole.DEFENDER_PEN_AREA)
				.forEach(sdr -> sdr.setNewRole(new DefenderPenAreaRole()));
	}


	@Override
	public void updateRoles(final AthenaAiFrame aiFrame)
	{
		super.updateRoles(aiFrame);
		this.aiFrame = aiFrame;
		penAreaBoundary = PenAreaBoundary
				.ownWithMargin(Geometry.getBotRadius() + Geometry.getPenaltyAreaMargin() + penAreaExtraMargin);

		List<PenAreaThreatAssigment> threatAssignments = getDefenseThreatAssignments();

		List<TargetGroup> reducedTargetGroups = reduceThreatAssignmentsToTargetGroups(threatAssignments);
		drawReducedTargetGroups(reducedTargetGroups);

		List<PenAreaSpaces> spaces = createPenAreaSpaces(reducedTargetGroups);
		drawSpaces(spaces);

		List<TargetGroup> allTargetGroups = addTargetsFromSpacesToTargetGroups(reducedTargetGroups, spaces);
		drawAllSortedTargetGroups(allTargetGroups);

		assignTargetGroupsToRoles(allTargetGroups);

		assignActiveKicker();
	}


	private void drawAllSortedTargetGroups(final List<TargetGroup> allTargetsSorted)
	{
		allTargetsSorted.forEach(t -> getShapes().add(
				new DrawableCircle(Circle.createCircle(t.centerDest, 60), Color.magenta)));
		allTargetsSorted.forEach(t -> getShapes().add(
				new DrawableAnnotation(t.centerDest, String.valueOf(t.priority), Color.magenta)
						.withCenterHorizontally(true)));
	}


	private void drawReducedTargetGroups(final List<TargetGroup> reducedTargets)
	{
		reducedTargets.forEach(t -> getShapes().add(
				new DrawableCircle(Circle.createCircle(t.centerDest, 30), Color.blue)));
		reducedTargets.forEach(t -> t.moveDestinations.forEach(m -> getShapes().add(
				new DrawableLine(Line.fromPoints(t.centerDest, m), Color.blue))));
		reducedTargets.forEach(t -> t.moveDestinations.forEach(m -> getShapes().add(
				new DrawableCircle(Circle.createCircle(m, 20), Color.blue))));
	}


	private void drawSpaces(final List<PenAreaSpaces> spaces)
	{
		spaces.forEach(s -> getShapes().add(
				new DrawableLine(Line.fromPoints(s.start, s.end), Color.orange)));
		spaces.forEach(s -> getShapes().add(
				new DrawableAnnotation(Lines.segmentFromPoints(s.start, s.end).getCenter(), String.valueOf(s.numTargets),
						Color.orange)
								.withCenterHorizontally(true)
								.withOffset(Vector2.fromY(-100))));
	}


	/**
	 * @return the defense threats that are designated to the penalty area or not assigned at all
	 */
	private List<PenAreaThreatAssigment> getDefenseThreatAssignments()
	{
		return aiFrame.getTacticalField().getDefenseThreatAssignments().stream()
				.filter(dta -> dta.getThreat().getType() != EDefenseThreatType.BALL_TO_BOT)
				.map(this::mapToThreatForThisGroup)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}


	private Optional<PenAreaThreatAssigment> mapToThreatForThisGroup(DefenseThreatAssignment assignment)
	{
		final Set<BotID> assignedBots = getRoles().stream()
				.map(SwitchableDefenderRole::getOriginalRole)
				.map(ARole::getBotID)
				.filter(botID -> assignment.getBotIds().contains(botID))
				.collect(Collectors.toSet());

		if (!assignedBots.isEmpty())
		{
			return Optional.of(new PenAreaThreatAssigment(assignment.getThreat(), assignedBots));
		}
		return Optional.empty();
	}


	/**
	 * Reduce the list of threat assignments until they match the available number of roles
	 *
	 * @param threatAssignments all threat assignments reported by Metis
	 * @return the reduce list of target groups
	 */
	private List<TargetGroup> reduceThreatAssignmentsToTargetGroups(
			final List<PenAreaThreatAssigment> threatAssignments)
	{
		List<PenAreaThreatAssigment> reducedAssignments = new ArrayList<>(threatAssignments);
		do
		{
			List<PenAreaThreatAssigment> targets = sortThreatAssignments(reducedAssignments);
			List<List<PenAreaThreatAssigment>> targetClusters = getTargetClusters(targets);
			List<TargetGroup> reducedTargets = reduceClustersToTargets(targetClusters);

			int nUsedBots = reducedTargets.stream().mapToInt(tg -> tg.moveDestinations.size()).sum();
			if (nUsedBots <= getRoles().size())
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
	private List<PenAreaThreatAssigment> sortThreatAssignments(final List<PenAreaThreatAssigment> threats)
	{
		return threats.stream()
				.sorted((r1, r2) -> penAreaBoundary.compare(r1.threat.getPos(), r2.threat.getPos()))
				.collect(Collectors.toList());
	}


	/**
	 * Assign the target groups to the defenders
	 *
	 * @param allTargetGroups all target groups to assign
	 */
	private void assignTargetGroupsToRoles(final List<TargetGroup> allTargetGroups)
	{
		List<DefenderPenAreaRole> defenderRoles = sortedDefenders();
		List<TargetGroup> allTargetsSorted = sortedTargetGroups(allTargetGroups);
		List<TargetGroupAssignment> targetGroupAssignments = assignTargetGroups(allTargetsSorted, defenderRoles);

		for (TargetGroupAssignment targetGroupAssignment : targetGroupAssignments)
		{
			assignTargetGroupToRole(targetGroupAssignment, allTargetsSorted, targetGroupAssignments);
		}
	}


	/**
	 * Assign a role to a target group
	 *
	 * @param targetGroupAssignment
	 * @param allTargetsSorted
	 * @param targetGroupAssignments
	 */
	private void assignTargetGroupToRole(
			final TargetGroupAssignment targetGroupAssignment,
			final List<TargetGroup> allTargetsSorted,
			final List<TargetGroupAssignment> targetGroupAssignments)
	{
		final DefenderPenAreaRole role = targetGroupAssignment.role;
		final IVector2 moveDest = targetGroupAssignment.moveDest;

		Optional<TargetGroup> otherProtectedTargetGroup = otherProtectedTargetGroup(allTargetsSorted,
				targetGroupAssignment, role);
		if (otherProtectedTargetGroup.isPresent())
		{
			// the role is currently still protecting a more important threat in another target group
			final IVector2 centerDest = otherProtectedTargetGroup.get().centerDest;
			role.setDestination(centerDest);
			getShapes().add(new DrawableLine(Line.fromPoints(role.getPos(), centerDest), Color.red));
		} else if (isTargetProtected(targetGroupAssignments, targetGroupAssignment))
		{
			// apply the desired move destination
			role.setDestination(moveDest);
		} else
		{
			// Stay on centerDest until second bot is near its destination
			final IVector2 centerDest = targetGroupAssignment.targetGroup.centerDest;
			role.setDestination(centerDest);
			getShapes().add(new DrawableLine(Line.fromPoints(role.getPos(), centerDest), Color.RED));
		}
		role.setPenAreaBoundary(penAreaBoundary);
		getShapes().add(new DrawableLine(Line.fromPoints(role.getPos(), moveDest), Color.PINK));
	}


	/**
	 * check if this role is currently protecting a threat in another target group
	 *
	 * @param allTargetsSorted
	 * @param targetGroupAssignment
	 * @param role
	 * @return
	 */
	private Optional<TargetGroup> otherProtectedTargetGroup(
			final List<TargetGroup> allTargetsSorted,
			final TargetGroupAssignment targetGroupAssignment,
			final DefenderPenAreaRole role)
	{
		return allTargetsSorted.stream()
				// not the one that is processed
				.filter(targetGroup -> targetGroup != targetGroupAssignment.targetGroup)
				// only those that are protected by the currently processed role
				.filter(targetGroup -> targetGroup.isProtectedByPos(role.getPos()))
				// only those that are more important than the one currently processed
				.filter(targetGroup -> currentTargetIsMoreImportant(targetGroup, targetGroupAssignment.targetGroup))
				// only those that are not already protected by their own role
				.filter(targetGroup -> !targetGroup.isProtectedByPos(role.getPos()))
				// one is enough, more is unlikely or impossible
				.findFirst();
	}


	/**
	 * Check if a target is currently protected by another bot
	 *
	 * @param targetGroupAssignments
	 * @param targetGroupAssignment
	 * @return
	 */
	private boolean isTargetProtected(final List<TargetGroupAssignment> targetGroupAssignments,
			final TargetGroupAssignment targetGroupAssignment)
	{
		return targetGroupAssignments.stream()
				// other assignment
				.filter(assignment -> assignment != targetGroupAssignment)
				// with the same target group
				.filter(assignment -> assignment.targetGroup == targetGroupAssignment.targetGroup)
				// that already protect the target
				.anyMatch(assignment -> assignment.protectedByAssignedRole);
	}


	/**
	 * Sort defenders along the penalty area boundary from start to end
	 *
	 * @return the sorted defenders
	 */
	private List<DefenderPenAreaRole> sortedDefenders()
	{
		return getRoles().stream()
				.map(sRole -> (DefenderPenAreaRole) sRole.getNewRole())
				.sorted((r1, r2) -> penAreaBoundary.compare(r1.getPos(), r2.getPos()))
				.collect(Collectors.toList());
	}


	private List<TargetGroupAssignment> assignTargetGroups(
			final List<TargetGroup> allTargetsSorted,
			final List<DefenderPenAreaRole> defenderRoles)
	{
		List<TargetGroupAssignment> targetGroupAssignments = new ArrayList<>();
		for (TargetGroup targetGroup : allTargetsSorted)
		{
			for (IVector2 moveDest : targetGroup.moveDestinations)
			{
				final DefenderPenAreaRole role = defenderRoles.remove(0);
				targetGroupAssignments.add(new TargetGroupAssignment(targetGroup, role, moveDest));
			}
		}
		return targetGroupAssignments;
	}


	private boolean currentTargetIsMoreImportant(final TargetGroup currentTarget, final TargetGroup nextTargetGroup)
	{
		return currentTarget.priority < nextTargetGroup.priority;
	}


	/**
	 * Spread new target groups equally on the given space
	 *
	 * @param space the space to spread on
	 * @return the resulting target groups
	 */
	private List<TargetGroup> spreadTargetsOnSpace(final PenAreaSpaces space)
	{
		double width = penAreaBoundary.distanceBetween(space.start, space.end);
		double step = width / (space.numTargets + 1);

		List<TargetGroup> groups = new ArrayList<>(space.numTargets);
		IVector2 next = space.start;
		for (int i = 0; i < space.numTargets; i++)
		{
			next = penAreaBoundary.stepAlongBoundary(next, step).orElseThrow(IllegalStateException::new);
			groups.add(new TargetGroup(next, 99));
		}
		return groups;
	}


	/**
	 * Create penalty area spaces and fill them with the number of roles
	 *
	 * @param reducedTargetGroups current target groups
	 * @return all spaces
	 */
	private List<PenAreaSpaces> createPenAreaSpaces(final List<TargetGroup> reducedTargetGroups)
	{
		List<IVector2> penAreaMarkers = createPenAreaMarkers(reducedTargetGroups);
		List<PenAreaSpaces> spaces = penAreaMarkersToSpaces(penAreaMarkers);
		assignTargetsToSpaces(reducedTargetGroups, spaces);
		return spaces;
	}


	private List<IVector2> createPenAreaMarkers(final List<TargetGroup> reducedTargetGroups)
	{
		List<IVector2> penAreaMarkers = new ArrayList<>(reducedTargetGroups.size() + 2);
		penAreaMarkers.addAll(reducedTargetGroups.stream()
				.map(g -> g.moveDestinations)
				.flatMap(List::stream)
				.collect(Collectors.toList()));
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


	private void assignTargetsToSpaces(final List<TargetGroup> reducedTargetGroups, final List<PenAreaSpaces> spaces)
	{
		int targetSum = reducedTargetGroups.stream().mapToInt(g -> g.moveDestinations.size()).sum();
		int remainingTargets = getRoles().size() - targetSum;
		for (int i = 0; i < remainingTargets; i++)
		{
			spaces.sort(Comparator.comparingDouble(PenAreaSpaces::distByNumTargets).reversed());
			spaces.get(0).numTargets++;
		}
	}


	private List<TargetGroup> addTargetsFromSpacesToTargetGroups(
			final List<TargetGroup> reducedTargetGroups,
			final List<PenAreaSpaces> spaces)
	{
		List<TargetGroup> targetGroups = new ArrayList<>(reducedTargetGroups);
		for (PenAreaSpaces space : spaces)
		{
			targetGroups.addAll(spreadTargetsOnSpace(space));
		}
		return targetGroups;
	}


	/**
	 * Sort target groups along the penalty area boundary from start to end
	 *
	 * @param targetGroups the target groups to sort
	 * @return the sorted target groups
	 */
	private List<TargetGroup> sortedTargetGroups(final List<TargetGroup> targetGroups)
	{
		return targetGroups.stream()
				.sorted((r1, r2) -> penAreaBoundary.compare(r1.centerDest, r2.centerDest))
				.collect(Collectors.toList());
	}


	/**
	 * Build clusters of threats that are close together and should be protected only once on the penalty area
	 *
	 * @param threatAssignments
	 * @return
	 */
	private List<List<PenAreaThreatAssigment>> getTargetClusters(final List<PenAreaThreatAssigment> threatAssignments)
	{
		List<List<PenAreaThreatAssigment>> targetClusters = new ArrayList<>();
		for (PenAreaThreatAssigment threatAssignment : threatAssignments)
		{
			List<PenAreaThreatAssigment> targetCluster;
			IVector2 target = getTargetOnPenaltyArea(threatAssignment.threat);
			if (targetClusters.isEmpty())
			{
				targetCluster = new ArrayList<>();
				targetClusters.add(targetCluster);
			} else
			{
				List<PenAreaThreatAssigment> lastTargetCluster = targetClusters.get(targetClusters.size() - 1);
				PenAreaThreatAssigment lastThreatAssignment = lastTargetCluster.get(lastTargetCluster.size() - 1);
				IVector2 lastThreat = getTargetOnPenaltyArea(lastThreatAssignment.threat);
				int nDefender = threatAssignment.assignedBots.size() + lastThreatAssignment.assignedBots.size();
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


	private List<TargetGroup> reduceClustersToTargets(final List<List<PenAreaThreatAssigment>> targetClusters)
	{
		List<TargetGroup> reducedTargets = new ArrayList<>(targetClusters.size());
		int priority = 0;
		for (List<PenAreaThreatAssigment> targetCluster : targetClusters)
		{
			int numBotsToUse = numBotsForCluster(targetCluster);
			IVector2 target = targetOfCluster(targetCluster);
			List<IVector2> subTargets = subTargetsAroundCenter(target, numBotsToUse);
			reducedTargets.add(new TargetGroup(target, subTargets, priority++));
		}
		return reducedTargets;
	}


	private int numBotsForCluster(final List<PenAreaThreatAssigment> targetCluster)
	{
		return targetCluster.stream().mapToInt(dta -> dta.assignedBots.size()).sum();
	}


	private IVector2 targetOfCluster(final List<PenAreaThreatAssigment> targetCluster)
	{
		return penAreaBoundary.projectPoint(centerOfThreats(targetCluster));
	}


	private IVector2 centerOfThreats(final List<PenAreaThreatAssigment> targetCluster)
	{
		Optional<PenAreaThreatAssigment> ballAssignment = targetCluster.stream()
				.filter(ta -> !ta.threat.getObjectId().isBot())
				.findFirst();
		if (ballAssignment.isPresent())
		{
			// if the ball is part of the threat cluster, focus on the ball, not the center of all threats
			return ballAssignment.get().threat.getPos();
		}
		IVector2 first = targetCluster.get(0).threat.getPos();
		IVector2 last = targetCluster.get(targetCluster.size() - 1).threat.getPos();
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


	private IVector2 getTargetOnPenaltyArea(IDefenseThreat threat)
	{
		return penAreaBoundary.projectPoint(
				threat.getThreatLine().getEnd(),
				threat.getThreatLine().getStart());
	}


	private void assignActiveKicker()
	{
		if (!defendersAreAllowedToKick())
		{
			penAreaDefendersStream().forEach(r -> r.setAllowedToKickBall(false));
			return;
		}
		boolean oneDefenderIsAllowedToKickTheBall = penAreaDefendersStream()
				.anyMatch(DefenderPenAreaRole::isAllowedToKickBall);

		if (!oneDefenderIsAllowedToKickTheBall)
		{
			penAreaDefendersStream()
					.min(Comparator.comparingDouble(this::distanceToBall))
					.ifPresent(r -> r.setAllowedToKickBall(true));
		}
	}


	private boolean defendersAreAllowedToKick()
	{
		return aiFrame.getGamestate().isRunning()
				&& defenseResponsibleForBall()
				&& notTooCloseToPenArea();
	}


	private boolean defenseResponsibleForBall()
	{
		return aiFrame.getTacticalField().getBallResponsibility() == EBallResponsibility.DEFENSE;
	}


	private boolean notTooCloseToPenArea()
	{
		return !Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() * 2)
				.isPointInShape(aiFrame.getWorldFrame().getBall().getPos());
	}


	private double distanceToBall(final ARole role)
	{
		return role.getBot().getPos().distanceTo(aiFrame.getWorldFrame().getBall().getPos());
	}


	private Stream<DefenderPenAreaRole> penAreaDefendersStream()
	{
		return getRoles().stream().map(SwitchableDefenderRole::getNewRole)
				.map(role -> (DefenderPenAreaRole) role);
	}


	private List<IDrawableShape> getShapes()
	{
		return aiFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.DEFENSE_PENALTY_AREA_GROUP);
	}

	private class PenAreaSpaces
	{
		IVector2 start;
		IVector2 end;
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

	private static class TargetGroup
	{
		final IVector2 centerDest;
		final List<IVector2> moveDestinations;
		/** smaller is more important */
		final int priority;


		TargetGroup(final IVector2 centerDest, final List<IVector2> moveDestinations, final int priority)
		{
			this.centerDest = centerDest;
			this.moveDestinations = Collections.unmodifiableList(moveDestinations);
			this.priority = priority;
		}


		TargetGroup(final IVector2 centerDest, final int priority)
		{
			this.centerDest = centerDest;
			List<IVector2> moveDest = new ArrayList<>();
			moveDest.add(centerDest);
			moveDestinations = Collections.unmodifiableList(moveDest);
			this.priority = priority;
		}


		boolean isProtectedByPos(final IVector2 pos)
		{
			return moveDestinations.stream().map(dest -> dest.distanceTo(pos))
					.anyMatch(dist -> dist < interchangeDist);
		}
	}

	private static class TargetGroupAssignment
	{
		final DefenderPenAreaRole role;
		final TargetGroup targetGroup;
		final IVector2 moveDest;
		final boolean protectedByAssignedRole;


		TargetGroupAssignment(final TargetGroup targetGroup, final DefenderPenAreaRole role, final IVector2 moveDest)
		{
			this.role = role;
			this.targetGroup = targetGroup;
			this.moveDest = moveDest;

			protectedByAssignedRole = Lines.segmentFromPoints(targetGroup.centerDest, moveDest)
					.distanceTo(role.getPos()) < Geometry.getBotRadius() * 2 + interchangeDist;
		}
	}

	private static class PenAreaThreatAssigment
	{
		final IDefenseThreat threat;
		final Set<BotID> assignedBots;


		public PenAreaThreatAssigment(final IDefenseThreat threat, final Set<BotID> assignedBots)
		{
			this.threat = threat;
			this.assignedBots = assignedBots;
		}
	}
}
