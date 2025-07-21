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
import org.apache.commons.lang.Validate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

	@Configurable(defValue = "1000.0", comment = "[mm] ")
	private static double preferredDefenderBonus = 1000.0;

	private final Supplier<Set<BotID>> penAreaDefenders;
	private final Supplier<List<DefensePenAreaTargetGroup>> targetGroups;
	private final Supplier<IShapeBoundary> penAreaBoundary;

	@Getter
	private List<DefensePenAreaPositionAssignment> penAreaPositionAssignments;


	private Map<BotID, TimestampTimer> isBotNewFirstClass = new HashMap<>();
	private Set<BotID> usedRobots = new HashSet<>();
	private List<ITrackedBot> allDefenders = new ArrayList<>();


	@Override
	protected void doCalc()
	{
		usedRobots = new HashSet<>();
		allDefenders = sortedDefenders(penAreaDefenders.get().stream().map(botID -> getWFrame().getBot(botID)).toList());


		var firstClassDefender = sortedDefenders(getFirstClassDefender());

		var ballTargetGroups = sortedTargetGroups(getBallTargetGroups());
		var ballTargetAssignments = getDefensePenAreaPositionAssignments(ballTargetGroups, firstClassDefender,
				EDefensePenAreaPositionAssignmentClass.BALL);


		var firstClassTargetGroups = sortedTargetGroups(getFirstClassTargetGroups(firstClassDefender));
		var firstClassAssignments = getDefensePenAreaPositionAssignments(firstClassTargetGroups, firstClassDefender,
				EDefensePenAreaPositionAssignmentClass.FIRST_CLASS);

		var secondClassTargetGroups = sortedTargetGroups(getSecondClassTargetGroups(firstClassTargetGroups));
		var secondClassAssignments = getDefensePenAreaPositionAssignments(secondClassTargetGroups, List.of(),
				EDefensePenAreaPositionAssignmentClass.SECOND_CLASS);

		penAreaPositionAssignments = Stream.of(ballTargetAssignments.stream(), firstClassAssignments.stream(),
						secondClassAssignments.stream())
				.reduce(Stream::concat).orElseGet(Stream::empty)
				.toList();

		Validate.isTrue(penAreaPositionAssignments.size() == penAreaDefenders.get().size());
	}


	private List<DefensePenAreaTargetGroup> getBallTargetGroups()
	{
		return targetGroups.get().stream()
				.filter(this::isBallTargetGroup)
				.toList();
	}


	private List<DefensePenAreaPositionAssignment> getDefensePenAreaPositionAssignments(
			List<DefensePenAreaTargetGroup> targetGroups,
			List<ITrackedBot> preferredDefenders,
			EDefensePenAreaPositionAssignmentClass defenderClass)
	{
		var targetGroupsSorted = sortedTargetGroups(targetGroups);
		var penAreaTargetGroupAssignments = assignTargetGroups(targetGroupsSorted, preferredDefenders, defenderClass);

		return penAreaTargetGroupAssignments.stream()
				.map(groupAss -> assignTargetGroupToRole(groupAss, penAreaTargetGroupAssignments,
						defenderClass))
				.toList();
	}


	private List<ITrackedBot> getFirstClassDefender()
	{
		var firstClassDefender = allDefenders.stream()
				.filter(bot -> Geometry.getPenaltyAreaOur().withMargin(defenderClassDifferentiationDistance)
						.isPointInShape(bot.getPos()))
				.toList();
		var botIDs = firstClassDefender.stream().map(ITrackedBot::getBotId).collect(Collectors.toUnmodifiableSet());
		isBotNewFirstClass.entrySet().removeIf(entry -> !botIDs.contains(entry.getKey()));
		return firstClassDefender;
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
		return targetGroups.get().stream()
				.filter(tg -> !isBallTargetGroup(tg))
				.filter(tg -> !firstClassTargetGroups.contains(tg))
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
			List<DefensePenAreaTargetGroup> allTargetsSorted,
			List<ITrackedBot> preferredDefenders,
			EDefensePenAreaPositionAssignmentClass defenderClass)
	{
		var targetGroupAssignments = new ArrayList<TargetGroupAssignment>();
		for (var targetGroup : allTargetsSorted)
		{
			List<ITrackedBot> defenders;
			if (defenderClass == EDefensePenAreaPositionAssignmentClass.BALL)
			{
				defenders = sortedDefenders(getBestDefendersConsideringDistance(preferredDefenders, targetGroup));
			} else
			{
				defenders = sortedDefenders(getBestDefenders(preferredDefenders, targetGroup));
			}
			Validate.isTrue(defenders.size() == targetGroup.moveDestinations().size());
			for (int i = 0; i < defenders.size(); ++i)
			{
				var moveDest = targetGroup.moveDestinations().get(i);
				var adaptedMoveDest = targetGroup.velAdaptedMoveDestinations().get(i);
				var defender = defenders.get(i);

				var newTimer = isBotNewFirstClass.computeIfAbsent(defender.getBotId(),
						ignored -> new TimestampTimer(firstClassNewTime));
				newTimer.update(getWFrame().getTimestamp());
				var currentInterchangeDist = newTimer.isTimeUp(getWFrame().getTimestamp()) ?
						interchangeDist :
						interchangeDistNew;
				var isProtected = Lines.segmentFromPoints(targetGroup.centerDest(), moveDest).distanceTo(defender.getPos())
						< Geometry.getBotRadius() * 2 + currentInterchangeDist;

				targetGroupAssignments.add(
						new TargetGroupAssignment(targetGroup, defender, moveDest, adaptedMoveDest, isProtected));
			}
		}
		return targetGroupAssignments;
	}


	private List<ITrackedBot> getBestDefendersConsideringDistance(
			List<ITrackedBot> preferredDefenders,
			DefensePenAreaTargetGroup targetGroup
	)
	{
		var numWanted = targetGroup.moveDestinations().size();
		var destination = targetGroup.centerDest();
		var defendersSortedAndLimited = allDefenders.stream()
				.filter(def -> !usedRobots.contains(def.getBotId()))
				.sorted(Comparator.comparingDouble(def -> getConsideredDistance(def, preferredDefenders, destination)))
				.limit(numWanted)
				.toList();
		usedRobots.addAll(defendersSortedAndLimited.stream().map(ITrackedBot::getBotId).toList());
		return defendersSortedAndLimited;
	}


	private double getConsideredDistance(ITrackedBot defender, List<ITrackedBot> preferredDefenders,
			IVector2 destination)
	{
		if (preferredDefenders.contains(defender))
		{
			return defender.getPos().distanceTo(destination) - preferredDefenderBonus;
		} else
		{
			return defender.getPos().distanceTo(destination);
		}
	}


	private List<ITrackedBot> getBestDefenders(
			List<ITrackedBot> preferredDefenders,
			DefensePenAreaTargetGroup targetGroup
	)
	{
		var numWanted = targetGroup.moveDestinations().size();
		var preferredLimited = preferredDefenders.stream()
				.filter(def -> !usedRobots.contains(def.getBotId()))
				.limit(numWanted)
				.toList();
		usedRobots.addAll(preferredLimited.stream().map(ITrackedBot::getBotId).toList());
		if (preferredLimited.size() == numWanted)
		{
			return preferredLimited;
		}
		var numRemaining = numWanted - preferredLimited.size();
		var defendersLimited = Stream.concat(
				preferredLimited.stream(),
				allDefenders.stream()
						.filter(def -> !usedRobots.contains(def.getBotId()))
						.limit(numRemaining)
		).toList();
		usedRobots.addAll(defendersLimited.stream().map(ITrackedBot::getBotId).toList());
		return defendersLimited;
	}


	private DefensePenAreaPositionAssignment assignTargetGroupToRole(
			final TargetGroupAssignment targetGroupAssignment,
			final List<TargetGroupAssignment> targetGroupAssignments,
			EDefensePenAreaPositionAssignmentClass defenderClass
	)
	{
		var defender = targetGroupAssignment.defender;
		IVector2 finalMoveDest;
		if (isTargetProtected(targetGroupAssignments, targetGroupAssignment))
		{
			// apply the desired move destination
			finalMoveDest = targetGroupAssignment.velAdaptedMoveDest;
		} else
		{
			// Stay on centerDest until second bot is near its destination
			final IVector2 centerDest = targetGroupAssignment.targetGroup.centerDest();
			getShapes().add(new DrawableLine(defender.getPos(), centerDest, Color.RED));
			finalMoveDest = targetGroupAssignment.targetGroup.velAdaptedCenterDest();
		}
		getShapes().add(new DrawableLine(defender.getPos(), targetGroupAssignment.moveDest, Color.PINK));
		getShapes().add(new DrawableAnnotation(defender.getPos(), defenderClass.toString()).withOffsetX(100));
		return new DefensePenAreaPositionAssignment(
				defender.getBotId(),
				finalMoveDest,
				targetGroupAssignment.targetGroup.threats(),
				defenderClass
		);
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


	private record TargetGroupAssignment(
			DefensePenAreaTargetGroup targetGroup,
			ITrackedBot defender,
			IVector2 moveDest,
			IVector2 velAdaptedMoveDest,
			boolean protectedByAssignedBot)
	{
	}

}
