/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePenAreaPositionAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePenAreaTargetGroup;
import edu.tigers.sumatra.ai.metis.defense.data.EDefensePenAreaPositionAssignmentClass;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.boundary.IShapeBoundary;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequiredArgsConstructor
public class DefensePenAreaTargetGroupAssignerCalc extends ACalculator
{
	@Configurable(defValue = "350.0", comment = "[mm] Distance when a bot is considered close enough that other bots can leave the protected target")
	private static double interchangeDist = 350.0;

	@Configurable(defValue = "150.0", comment = "[mm] Distance when a new bot is considered close enough that other bots can leave the protected target")
	private static double interchangeDistNew = 150.0;

	@Configurable(defValue = "1.0", comment = "[s] Time a first class defender is considered new")
	private static double firstClassNewTime = 1.0;

	@Configurable(defValue = "1000.0", comment = "[mm] ")
	private static double defenderClassDifferentiationDistance = 1000.0;

	private final Supplier<Set<BotID>> penAreaDefenders;
	private final Supplier<List<DefensePenAreaTargetGroup>> targetGroups;
	private final Supplier<IShapeBoundary> penAreaBoundary;

	@Getter
	private List<DefensePenAreaPositionAssignment> penAreaPositionAssignments;


	private Map<BotID, TimestampTimer> isBotNewFirstClass = new HashMap<>();


	@Override
	protected void doCalc()
	{
		var firstClassDefender = sortedDefenders(getFirstClassDefender());
		var ballTargetGroup = targetGroups.get().stream()
				.filter(this::isBallTargetGroup)
				.findAny();
		List<DefensePenAreaPositionAssignment> ballTargetAssignment;
		List<ITrackedBot> ballDefender;
		if (ballTargetGroup.isPresent())
		{
			ballDefender = sortedDefenders(getBallDefenders(ballTargetGroup.get(), firstClassDefender));
			ballTargetAssignment = getDefensePenAreaPositionAssignments(ballTargetGroup.stream().toList(), ballDefender,
					EDefensePenAreaPositionAssignmentClass.BALL);
			firstClassDefender = firstClassDefender.stream().filter(first -> !ballDefender.contains(first)).toList();
		} else
		{
			ballDefender = List.of();
			ballTargetAssignment = List.of();
		}


		var firstClassTargetGroups = sortedTargetGroups(getFirstClassTargetGroups(firstClassDefender));
		var firstClassAssignments = getDefensePenAreaPositionAssignments(firstClassTargetGroups, firstClassDefender,
				EDefensePenAreaPositionAssignmentClass.FIRST_CLASS);

		var secondClassDefender = sortedDefenders(getSecondClassDefender(firstClassAssignments, ballDefender));
		var secondClassTargetGroups = sortedTargetGroups(getSecondClassTargetGroups(firstClassTargetGroups));
		var secondClassAssignments = getDefensePenAreaPositionAssignments(secondClassTargetGroups, secondClassDefender,
				EDefensePenAreaPositionAssignmentClass.SECOND_CLASS);

		penAreaPositionAssignments = Stream.of(ballTargetAssignment.stream(), firstClassAssignments.stream(),
						secondClassAssignments.stream())
				.reduce(Stream::concat).orElseGet(Stream::empty)
				.toList();
	}


	private List<ITrackedBot> getBallDefenders(DefensePenAreaTargetGroup ballTargetGroup,
			List<ITrackedBot> firstClassDefenders)
	{
		var amountNeeded = ballTargetGroup.moveDestinations().size();
		if (firstClassDefenders.size() <= amountNeeded)
		{
			var result = new ArrayList<>(firstClassDefenders);
			if (result.size() < amountNeeded)
			{
				penAreaDefenders.get().stream()
						.map(def -> getWFrame().getBot(def))
						.filter(def -> !result.contains(def))
						.sorted(Comparator.comparingDouble(def -> getDistanceToTarget(def, ballTargetGroup.centerDest())))
						.limit((long) amountNeeded - result.size())
						.forEach(result::add);
			}
			return Collections.unmodifiableList(result);
		}

		return firstClassDefenders.stream()
				.sorted(Comparator.comparingDouble(def -> getDistanceToTarget(def, ballTargetGroup.centerDest())))
				.limit(amountNeeded)
				.toList();
	}


	private double getDistanceToTarget(ITrackedBot defender, IVector2 target)
	{
		var defenderProjected = penAreaBoundary.get().closestPoint(defender.getPos());
		var targetProjected = penAreaBoundary.get().closestPoint(target);
		return penAreaBoundary.get().distanceBetween(defenderProjected, targetProjected);
	}


	private List<DefensePenAreaPositionAssignment> getDefensePenAreaPositionAssignments(
			List<DefensePenAreaTargetGroup> targetGroups,
			List<ITrackedBot> defenders,
			EDefensePenAreaPositionAssignmentClass defenderClass)
	{
		var targetGroupsSorted = sortedTargetGroups(targetGroups);
		var penAreaTargetGroupAssignments = assignTargetGroups(targetGroupsSorted, defenders);

		return penAreaTargetGroupAssignments.stream()
				.map(groupAss -> assignTargetGroupToRole(groupAss, targetGroupsSorted, penAreaTargetGroupAssignments,
						defenderClass))
				.toList();
	}


	private List<ITrackedBot> getFirstClassDefender()
	{
		var firstClassDefender = penAreaDefenders.get().stream()
				.map(botID -> getWFrame().getBot(botID))
				.filter(bot -> Geometry.getPenaltyAreaOur().withMargin(defenderClassDifferentiationDistance)
						.isPointInShape(bot.getPos()))
				.toList();
		var botIDs = firstClassDefender.stream().map(ITrackedBot::getBotId).collect(Collectors.toUnmodifiableSet());
		isBotNewFirstClass.entrySet().removeIf(entry -> !botIDs.contains(entry.getKey()));
		return firstClassDefender;
	}


	private List<ITrackedBot> getSecondClassDefender(List<DefensePenAreaPositionAssignment> firstClass,
			List<ITrackedBot> ballDefenders)
	{
		var usedDefenders = Stream.concat(
				ballDefenders.stream().map(ITrackedBot::getBotId),
				firstClass.stream().map(DefensePenAreaPositionAssignment::botID)
		).collect(Collectors.toUnmodifiableSet());
		return penAreaDefenders.get().stream()
				.filter(botID -> !usedDefenders.contains(botID))
				.map(botID -> getWFrame().getBot(botID))
				.toList();
	}


	/**
	 * Sort defenders along the penalty area boundary from start to end
	 *
	 * @param defenders the defenders to sort
	 * @return the sorted defenders
	 */
	private List<ITrackedBot> sortedDefenders(List<ITrackedBot> defenders)
	{
		return defenders.stream()
				.sorted((r1, r2) -> penAreaBoundary.get().compare(r1.getPos(), r2.getPos()))
				.toList();
	}


	private List<DefensePenAreaTargetGroup> getFirstClassTargetGroups(List<ITrackedBot> firstClassDefender)
	{
		List<DefensePenAreaTargetGroup> firstClassTargetGroups = new ArrayList<>();

		var numUsedDefenders = 0;
		for (var targetGroup : targetGroups.get())
		{
			if (isBallTargetGroup(targetGroup))
			{
				continue;
			}
			numUsedDefenders += targetGroup.moveDestinations().size();
			if (numUsedDefenders <= firstClassDefender.size())
			{
				firstClassTargetGroups.add(targetGroup);
			}
		}
		return firstClassTargetGroups;
	}


	private List<DefensePenAreaTargetGroup> getSecondClassTargetGroups(
			List<DefensePenAreaTargetGroup> firstClassTargetGroups)
	{
		var protectedPriorities = firstClassTargetGroups.stream()
				.map(DefensePenAreaTargetGroup::priority)
				.collect(Collectors.toUnmodifiableSet());
		return targetGroups.get().stream()
				.filter(tg -> !isBallTargetGroup(tg))
				.filter(tg -> !protectedPriorities.contains(tg.priority()))
				.toList();
	}


	private boolean isBallTargetGroup(DefensePenAreaTargetGroup targetGroup)
	{
		return targetGroup.threats().stream().anyMatch(t -> t.getType() == EDefenseThreatType.BALL);
	}


	/**
	 * Sort target groups along the penalty area boundary from start to end
	 *
	 * @param targetGroups the target groups to sort
	 * @return the sorted target groups
	 */
	private List<DefensePenAreaTargetGroup> sortedTargetGroups(List<DefensePenAreaTargetGroup> targetGroups)
	{
		return targetGroups.stream()
				.sorted((r1, r2) -> penAreaBoundary.get().compare(r1.centerDest(), r2.centerDest()))
				.toList();
	}


	private List<TargetGroupAssignment> assignTargetGroups(
			final List<DefensePenAreaTargetGroup> allTargetsSorted,
			final List<ITrackedBot> defenders)
	{
		List<TargetGroupAssignment> targetGroupAssignments = new ArrayList<>();
		var count = 0;
		for (var targetGroup : allTargetsSorted)
		{
			for (IVector2 moveDest : targetGroup.moveDestinations())
			{
				var defender = defenders.get(count++);
				var newTimer = isBotNewFirstClass.computeIfAbsent(defender.getBotId(),
						ignored -> new TimestampTimer(firstClassNewTime));
				newTimer.update(getWFrame().getTimestamp());
				var currentInterchangeDist = newTimer.isTimeUp(getWFrame().getTimestamp()) ?
						interchangeDist :
						interchangeDistNew;
				var isProtected = Lines.segmentFromPoints(targetGroup.centerDest(), moveDest).distanceTo(defender.getPos())
						< Geometry.getBotRadius() * 2 + currentInterchangeDist;

				targetGroupAssignments.add(new TargetGroupAssignment(targetGroup, defender, moveDest, isProtected));
			}
		}
		return targetGroupAssignments;
	}


	/**
	 * Assign a role to a target group
	 *
	 * @param targetGroupAssignment
	 * @param allTargetsSorted
	 * @param targetGroupAssignments
	 */
	private DefensePenAreaPositionAssignment assignTargetGroupToRole(
			final TargetGroupAssignment targetGroupAssignment,
			final List<DefensePenAreaTargetGroup> allTargetsSorted,
			final List<TargetGroupAssignment> targetGroupAssignments,
			EDefensePenAreaPositionAssignmentClass defenderClass
	)
	{
		var defender = targetGroupAssignment.defender;
		final IVector2 moveDest = targetGroupAssignment.moveDest;
		IVector2 finalMoveDest;
		Optional<DefensePenAreaTargetGroup> otherProtectedTargetGroup = otherProtectedTargetGroup(allTargetsSorted,
				targetGroupAssignment, defender);
		if (otherProtectedTargetGroup.isPresent())
		{
			// the role is currently still protecting a more important threat in another target group
			final IVector2 centerDest = otherProtectedTargetGroup.get().centerDest();
			getShapes().add(new DrawableLine(defender.getPos(), centerDest, Color.RED));
			finalMoveDest = centerDest;
		} else if (isTargetProtected(targetGroupAssignments, targetGroupAssignment))
		{
			// apply the desired move destination
			finalMoveDest = moveDest;
		} else
		{
			// Stay on centerDest until second bot is near its destination
			// See issue #1874
			final IVector2 centerDest = targetGroupAssignment.targetGroup.centerDest();
			getShapes().add(new DrawableLine(defender.getPos(), centerDest, Color.RED));
			finalMoveDest = moveDest;
		}
		getShapes().add(new DrawableLine(defender.getPos(), moveDest, Color.PINK));
		getShapes().add(new DrawableAnnotation(defender.getPos(), defenderClass.toString()).withOffsetX(100));
		return new DefensePenAreaPositionAssignment(
				defender.getBotId(),
				finalMoveDest,
				targetGroupAssignment.targetGroup.threats(),
				defenderClass
		);
	}


	/**
	 * check if this role is currently protecting a threat in another target group
	 *
	 * @param allTargetsSorted
	 * @param targetGroupAssignment
	 * @param defender
	 * @return
	 */
	private Optional<DefensePenAreaTargetGroup> otherProtectedTargetGroup(
			final List<DefensePenAreaTargetGroup> allTargetsSorted,
			final TargetGroupAssignment targetGroupAssignment,
			final ITrackedBot defender)
	{
		return allTargetsSorted.stream()
				// not the one that is processed
				.filter(targetGroup -> targetGroup != targetGroupAssignment.targetGroup)
				// only those that are protected by the currently processed role
				.filter(targetGroup -> targetGroup.isProtectedByPos(defender.getPos(), interchangeDist))
				// only those that are more important than the one currently processed
				.filter(targetGroup -> currentTargetIsMoreImportant(targetGroup, targetGroupAssignment.targetGroup))
				// only those that are not already protected by their own role
				.filter(targetGroup -> !targetGroup.isProtectedByPos(defender.getPos(), interchangeDist))
				// one is enough, more is unlikely or impossible
				.findFirst();
	}


	private boolean currentTargetIsMoreImportant(final DefensePenAreaTargetGroup currentTarget,
			final DefensePenAreaTargetGroup nextTargetGroup)
	{
		return currentTarget.priority() < nextTargetGroup.priority();
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
				.anyMatch(TargetGroupAssignment::protectedByAssignedBot);
	}


	private List<IDrawableShape> getShapes()
	{
		return getShapes(EAiShapesLayer.DEFENSE_PENALTY_AREA_GROUP_ASSIGNMENT);
	}


	private record TargetGroupAssignment(DefensePenAreaTargetGroup targetGroup, ITrackedBot defender,
	                                     IVector2 moveDest, boolean protectedByAssignedBot)
	{
	}

}
