/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePenAreaPositionAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePenAreaTargetGroup;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.PenAreaBoundary;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class DefensePenAreaTargetGroupAssignerCalc extends ACalculator
{
	@Configurable(defValue = "350.0", comment = "Distance when a bot is considered close enough that other bots can leave the protected target")
	private static double interchangeDist = 350.0;

	private final Supplier<Set<BotID>> penAreaDefenders;
	private final Supplier<List<DefensePenAreaTargetGroup>> targetGroups;
	private final Supplier<PenAreaBoundary> penAreaBoundary;

	@Getter
	private List<DefensePenAreaPositionAssignment> penAreaPositionAssignments;


	@Override
	protected void doCalc()
	{
		var defender = sortedDefenders(penAreaDefenders.get());
		var allTargetsSorted = sortedTargetGroups(targetGroups.get());
		var penAreaTargetGroupAssignments = assignTargetGroups(allTargetsSorted, defender);

		penAreaPositionAssignments = penAreaTargetGroupAssignments.stream()
				.map(groupAss -> assignTargetGroupToRole(groupAss, allTargetsSorted, penAreaTargetGroupAssignments))
				.toList();
	}


	/**
	 * Sort defenders along the penalty area boundary from start to end
	 *
	 * @param defenders the defenders to sort
	 * @return the sorted defenders
	 */
	private List<ITrackedBot> sortedDefenders(Set<BotID> defenders)
	{
		return defenders.stream()
				.map(botID -> getWFrame().getBot(botID))
				.sorted((r1, r2) -> penAreaBoundary.get().compare(r1.getPos(), r2.getPos()))
				.toList();
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
				var isProtected = Lines.segmentFromPoints(targetGroup.centerDest(), moveDest).distanceTo(defender.getPos())
						< Geometry.getBotRadius() * 2 + interchangeDist;

				targetGroupAssignments.add(
						new TargetGroupAssignment(targetGroup, defender, moveDest, isProtected));
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
			final List<TargetGroupAssignment> targetGroupAssignments)
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
			getShapes().add(new DrawableLine(Line.fromPoints(defender.getPos(), centerDest), Color.RED));
			finalMoveDest = centerDest;
		} else if (isTargetProtected(targetGroupAssignments, targetGroupAssignment))
		{
			// apply the desired move destination
			finalMoveDest = moveDest;
		} else
		{
			// Stay on centerDest until second bot is near its destination
			final IVector2 centerDest = targetGroupAssignment.targetGroup.centerDest();
			getShapes().add(new DrawableLine(Line.fromPoints(defender.getPos(), centerDest), Color.RED));
			finalMoveDest = centerDest;
		}
		getShapes().add(new DrawableLine(Line.fromPoints(defender.getPos(), moveDest), Color.PINK));
		return new DefensePenAreaPositionAssignment(defender.getBotId(), finalMoveDest,
				targetGroupAssignment.targetGroup.threats());
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
