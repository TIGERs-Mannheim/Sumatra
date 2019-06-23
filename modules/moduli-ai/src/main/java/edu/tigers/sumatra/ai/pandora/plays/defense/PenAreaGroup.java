/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.defense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.EBallResponsibility;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.DefenseConstants;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseGroup;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPenAreaRole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * The group containing all penArea bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PenAreaGroup extends ADefenseGroup
{
	@Configurable(defValue = "20", comment = "Distance offset to add to bot radius for determine same cluster of bots")
	private static double clusterDistanceOffset = 20;
	
	@Configurable(defValue = "200.0")
	private static double penaltyAreaMargin = 200;
	
	@Configurable(defValue = "350.0")
	private static double interchangeDist = 350;
	
	
	@Configurable(comment = "Distance between the penalty area defenders times the ball radius", defValue = "1.0")
	private double distBetweenPenAreaBotsFactor = 1.0;
	
	
	private static final IPenaltyArea penaltyAreaOurWithMargin = Geometry.getPenaltyAreaOur()
			.withMargin(Geometry.getBotRadius() * 2 + Geometry.getBallRadius() + 50);
	
	static
	{
		ConfigRegistration.registerClass("plays", PenAreaGroup.class);
	}
	private List<DefenseThreatAssignment> threats = new ArrayList<>();
	
	
	/**
	 * @param defendingId an identifying id
	 */
	public PenAreaGroup(final AObjectID defendingId)
	{
		super(defendingId);
	}
	
	
	@Override
	public void assignRoles()
	{
		for (SwitchableDefenderRole sRole : getRoles())
		{
			if (sRole.getOriginalRole().getType() != ERole.DEFENDER_PEN_AREA)
			{
				ARole newRole = new DefenderPenAreaRole();
				sRole.setNewRole(newRole);
			}
		}
	}
	
	
	@Override
	public void updateRoles(final AthenaAiFrame aiFrame)
	{
		super.updateRoles(aiFrame);
		
		List<DefenseThreatAssignment> threatAssignments = new ArrayList<>(threats);
		
		double extendedPenAreaRadius = Geometry.getPenaltyAreaOur().getRadius() + DefenseConstants.getMinGoOutDistance();
		List<ITrackedBot> foreignBotsToConsider = aiFrame.getWorldFrame().getTigerBotsVisible().values().stream()
				.filter(tBot -> tBot.getPos().distanceTo(Geometry.getGoalOur().getCenter()) < extendedPenAreaRadius)
				.filter(tBot -> !aiFrame.getWorldFrame().getTigerBotsAvailable().containsKey(tBot.getBotId()))
				.collect(Collectors.toList());
		List<IVector2> additionalTargets = foreignBotsToConsider.stream()
				.map(tBot -> Geometry.getPenaltyAreaOur().withMargin(penaltyAreaMargin).nearestPointInside(tBot.getPos()))
				.collect(Collectors.toList());
		
		List<TargetGroup> reducedTargets;
		do
		{
			List<DefenseThreatAssignment> targets = sortThreatAssignmentsByAngle(threatAssignments);
			
			List<List<DefenseThreatAssignment>> targetClusters = getTargetClusters(targets);
			
			reducedTargets = reduceClustersToTargets(targetClusters);
			
			int nUsedBots = reducedTargets.stream().mapToInt(tg -> tg.moveDestinations.size()).sum();
			
			if (nUsedBots <= getRoles().size())
			{
				break;
			}
			threatAssignments.remove(threatAssignments.size() - 1);
		} while (true);
		
		threatAssignments.forEach(t -> getShapes(aiFrame)
				.add(new DrawableLine(t.getThreat().getThreatLine())));
		threatAssignments.forEach(t -> getShapes(aiFrame)
				.add(new DrawableLine(Line.fromPoints(t.getThreat().getPos(), Geometry.getGoalOur().getRightPost()))));
		threatAssignments.forEach(t -> getShapes(aiFrame)
				.add(new DrawableLine(Line.fromPoints(t.getThreat().getPos(), Geometry.getGoalOur().getLeftPost()))));
		
		List<PenAreaSpaces> spaces = fillPenAreaSpaces(reducedTargets, additionalTargets);
		
		List<TargetGroup> allTargetsSorted = getAllSortedTargets(reducedTargets, spaces);
		assignTargetGroupsToRoles(allTargetsSorted);
		if (aiFrame.getGamestate().isDistanceToBallRequired() || aiFrame.getTacticalField().isOpponentWillDoIcing())
		{
			freeForbiddenArea(aiFrame);
		}
		if (aiFrame.getGamestate().getState() == EGameState.BALL_PLACEMENT)
		{
			freeBallPlacementCorridor(aiFrame);
		}
		kickBallIfEnoughTime(aiFrame);
	}
	
	
	private List<IDrawableShape> getShapes(AthenaAiFrame frame)
	{
		return frame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.DEFENSE_PENALTY_AREA_GROUP);
	}
	
	
	private IVector2 moveTargetOutOfCircle(IVector2 target, ICircle circle)
	{
		IVector2 goal = Geometry.getGoalOur().getCenter();
		IVector2 dir = goal.subtractNew(circle.center()).scaleTo(5);
		IVector2 newTarget = target;
		IPenaltyArea penaltyArea = Geometry.getPenaltyAreaOur().withMargin(Geometry.getPenaltyAreaMargin() + 1);
		while (circle.isPointInShape(newTarget))
		{
			IVector2 nextTarget = newTarget.addNew(dir);
			if (penaltyArea.isPointInShape(nextTarget))
			{
				break;
			}
			newTarget = nextTarget;
		}
		return newTarget;
	}
	
	
	private void freeBallPlacementCorridor(final AthenaAiFrame aiFrame)
	{
		IVector2 placementPos = aiFrame.getGamestate().getBallPlacementPositionForUs();
		double margin = Geometry.getBotToBallDistanceStop() + 2 * Geometry.getBotRadius();
		IVector2 ballPos = aiFrame.getWorldFrame().getBall().getPos();
		ITube forbiddenZone = Tube.create(placementPos, ballPos, margin);
		
		List<DefenderPenAreaRole> roles = getRoles().stream()
				.map(sRole -> (DefenderPenAreaRole) sRole.getNewRole()).collect(Collectors.toList());
		
		for (DefenderPenAreaRole role : roles)
		{
			if (forbiddenZone.isPointInShape(role.getPos()))
			{
				ILine line = Line.fromPoints(ballPos, placementPos);
				IVector2 newPos = line.nearestPointOnLine(role.getPos())
						.addNew(line.directionVector().getNormalVector().scaleToNew(margin));
				newPos = Geometry.getPenaltyAreaOur().withMargin(penaltyAreaMargin).isPointInShape(newPos)
						? line.nearestPointOnLine(role.getPos()).addNew(
								line.directionVector().getNormalVector().scaleToNew(-margin))
						: newPos;
				role.setTarget(newPos);
			} else
			{
				role.setTarget(role.getPos());
			}
		}
	}
	
	
	private void freeForbiddenArea(final AthenaAiFrame aiFrame)
	{
		double radius = Geometry.getBotToBallDistanceStop() + Geometry.getBotRadius() + 10;
		ICircle forbiddenCircle = Circle.createCircle(aiFrame.getWorldFrame().getBall().getPos(), radius);
		
		List<DefenderPenAreaRole> sortedRoles = getRoles().stream()
				.map(sRole -> (DefenderPenAreaRole) sRole.getNewRole())
				.sorted(Comparator
						.comparingDouble(role -> getRoleToThreatAngle(role.getPos(), forbiddenCircle.center())))
				.collect(Collectors.toList());
		int numNegative = (int) sortedRoles.stream()
				.filter(role -> getRoleToThreatAngle(role.getPos(), forbiddenCircle.center()) < 0).count();
		
		sortedRoles.forEach(role -> role.setTarget(moveTargetOutOfCircle(role.getTarget(), forbiddenCircle)));
		
		int startNegative = numNegative - 1;
		if (startNegative >= 0 && forbiddenCircle.isPointInShape(sortedRoles.get(startNegative).getTarget()))
		{
			moveUp(startNegative, -1, forbiddenCircle, sortedRoles);
		}
		int startPositive = numNegative;
		if (startPositive < sortedRoles.size()
				&& forbiddenCircle.isPointInShape(sortedRoles.get(startPositive).getTarget()))
		{
			moveUp(startPositive, 1, forbiddenCircle, sortedRoles);
		}
	}
	
	
	private void moveUp(int roleIdx, int dir, I2DShape obstacle, List<DefenderPenAreaRole> sortedRoles)
	{
		IVector2 base = DefenseMath.getBisectionGoal(sortedRoles.get(roleIdx).getTarget());
		double nextAngle = sortedRoles.get(roleIdx).getTarget().subtractNew(base).getAngle()
				+ 0.05 * dir;
		if (Math.abs(nextAngle) > AngleMath.PI_HALF)
		{
			return;
		}
		
		IVector2 nextTarget = getTargetOnPenaltyArea(
				base.addNew(Vector2.fromAngle(nextAngle).scaleTo(Geometry.getPenaltyAreaOur().getRadius())),
				Geometry.getPenaltyAreaMargin() + 1);
		
		sortedRoles.get(roleIdx).setTarget(nextTarget);
		int nextI = roleIdx + dir;
		if (nextI >= 0 && nextI < sortedRoles.size())
		{
			DefenderPenAreaRole nextRole = sortedRoles.get(nextI);
			IVector2 nextRoleTarget = nextRole.getTarget();
			if (nextTarget.distanceTo(nextRoleTarget) < Geometry.getBotRadius() * 2)
			{
				moveUp(nextI, dir, obstacle, sortedRoles);
			}
		}
		DefenderPenAreaRole currentRole = sortedRoles.get(roleIdx);
		if (obstacle.isPointInShape(currentRole.getTarget()))
		{
			moveUp(roleIdx, dir, obstacle, sortedRoles);
		}
	}
	
	
	public void setThreatAssignments(final List<DefenseThreatAssignment> threats)
	{
		this.threats = threats.stream().filter(a -> a.getDefenseGroup() == EDefenseGroup.PENALTY_AREA)
				.collect(Collectors.toList());
		this.threats.addAll(threats.stream()
				.filter(a -> a.getDefenseGroup() == EDefenseGroup.UNASSIGNED)
				.collect(Collectors.toList()));
	}
	
	
	private void assignTargetGroupsToRoles(final List<TargetGroup> allTargetsSorted)
	{
		List<DefenderPenAreaRole> defenderRoles = getRoles().stream()
				.map(sRole -> (DefenderPenAreaRole) sRole.getNewRole())
				.sorted(ANGLE_ROLE_COMPARATOR_REVERSED)
				.collect(Collectors.toList());
		
		List<TargetGroupAssigned> assignedTargetGroups = new ArrayList<>();
		for (TargetGroup targetGroup : allTargetsSorted)
		{
			List<IVector2> remainingMoveDests = new ArrayList<>(targetGroup.moveDestinations);
			for (int i = 0; i < targetGroup.moveDestinations.size(); i++)
			{
				TargetGroupAssigned targetGroupAssigned = new TargetGroupAssigned(targetGroup, defenderRoles.remove(0),
						remainingMoveDests);
				assignedTargetGroups.add(targetGroupAssigned);
			}
		}
		
		for (TargetGroupAssigned nextTargetGroup : assignedTargetGroups)
		{
			DefenderPenAreaRole role = nextTargetGroup.role;
			// check if this role is currently protecting another threat
			Optional<TargetGroupAssigned> currentTarget = assignedTargetGroups.stream()
					.filter(target -> target != nextTargetGroup)
					.filter(target -> target.isProtectedByPos(role.getPos()))
					.findFirst();
			
			if (currentTarget.isPresent() &&
					currentTarget.get().priority >= nextTargetGroup.priority &&
					!currentTarget.get().isProtectedByAssignedRole())
			{
				role.setTarget(currentTarget.get().centerDest);
				nextTargetGroup.remainingMoveDests.remove(0);
			} else
			{
				role.setTarget(nextTargetGroup.remainingMoveDests.remove(0));
			}
		}
	}
	
	
	private List<TargetGroup> getAllSortedTargets(final List<TargetGroup> reducedTargetGroups,
			final List<PenAreaSpaces> spaces)
	{
		List<TargetGroup> targetGroups = new ArrayList<>(reducedTargetGroups);
		int allTargetsCount = targetGroups.stream().mapToInt(targetGroup -> targetGroup.moveDestinations.size()).sum();
		int remainingTargets = getRoles().size() - allTargetsCount;
		
		for (int i = 0; i < remainingTargets; i++)
		{
			spaces.sort(Comparator.comparingDouble(PenAreaSpaces::dist).reversed());
			spaces.get(0).numTargets++;
		}
		
		for (PenAreaSpaces space : spaces)
		{
			spreadTargetsOnSpace(targetGroups, space);
		}
		return sortClusteredThreadsByAngle(targetGroups);
	}
	
	
	private void spreadTargetsOnSpace(final List<TargetGroup> allTargets, final PenAreaSpaces space)
	{
		double angleStep = space.diff();
		double lastAngle = space.startAngle();
		for (int i = 0; i < space.numTargets; i++)
		{
			double angle = lastAngle + angleStep;
			IVector2 dir = Geometry.getGoalOur().getCenter()
					.addNew(Vector2.fromAngle(angle).scaleTo(Geometry.getPenaltyAreaOur().getRadius()));
			IVector2 moveDest = getTargetOnPenaltyArea(dir);
			
			TargetGroup targetGroup = new TargetGroup(moveDest, Integer.MAX_VALUE);
			allTargets.add(targetGroup);
			lastAngle = angle;
		}
	}
	
	
	private List<PenAreaSpaces> fillPenAreaSpaces(
			final List<TargetGroup> reducedTargets,
			final List<IVector2> additionalTargets)
	{
		List<IVector2> penAreaMarkers = new ArrayList<>(reducedTargets.size() + 2);
		double penAreaRadius = Geometry.getPenaltyAreaOur().getRadius();
		penAreaMarkers.add(Geometry.getPenaltyAreaOur().getArcPos().center().addNew(Vector2.fromY(penAreaRadius)));
		penAreaMarkers.addAll(reducedTargets.stream().map(g -> g.centerDest).collect(Collectors.toList()));
		penAreaMarkers.addAll(additionalTargets);
		penAreaMarkers.add(Geometry.getPenaltyAreaOur().getArcNeg().center().addNew(Vector2.fromY(-penAreaRadius)));
		penAreaMarkers.sort(ANGLE_POS_COMPARATOR);
		
		List<PenAreaSpaces> spaces = new ArrayList<>();
		IVector2 lastMarker = penAreaMarkers.remove(0);
		for (IVector2 marker : penAreaMarkers)
		{
			spaces.add(new PenAreaSpaces(lastMarker, marker));
			lastMarker = marker;
		}
		
		return spaces;
	}
	
	
	private List<DefenseThreatAssignment> sortThreatAssignmentsByAngle(final List<DefenseThreatAssignment> threats)
	{
		Comparator<DefenseThreatAssignment> comparator = Comparator.comparingDouble(
				assignment -> getRoleToThreatAngle(assignment.getThreat().getPos(), Geometry.getCenter()));
		return threats.stream()
				.sorted(comparator.reversed())
				.collect(Collectors.toList());
	}
	
	
	private List<TargetGroup> sortClusteredThreadsByAngle(final List<TargetGroup> threats)
	{
		Comparator<TargetGroup> comparator = Comparator
				.comparingDouble(pat -> getRoleToThreatAngle(pat.centerDest, Geometry.getCenter()));
		return threats.stream()
				.sorted(comparator.reversed())
				.collect(Collectors.toList());
	}
	
	
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
	
	
	private List<TargetGroup> reduceClustersToTargets(
			final List<List<DefenseThreatAssignment>> targetClusters)
	{
		List<TargetGroup> reducedTargets = new ArrayList<>();
		int prio = 0;
		for (List<DefenseThreatAssignment> targetCluster : targetClusters)
		{
			IVector2 first = targetCluster.get(0).getThreat().getPos();
			IVector2 last = targetCluster.get(targetCluster.size() - 1).getThreat().getPos();
			double distance = first.distanceTo(last);
			IVector2 target = getTargetOnPenaltyArea(LineMath.stepAlongLine(first, last, distance / 2));
			int numBots = targetCluster.stream().mapToInt(dta -> Math.max(dta.getBotIds().size(), 1)).sum();
			int numThreats = targetCluster.size();
			int numBotsToUse = numBots - numThreats + 1;
			
			IVector2 base = DefenseMath.getBisectionGoal(target);
			IVector2 dir = target.subtractNew(base).turn(-AngleMath.PI_HALF)
					.scaleTo(Geometry.getBotRadius() * 2 + distBetweenPenAreaBotsFactor * Geometry.getBallRadius());
			double length = dir.getLength2() * (numBotsToUse - 1);
			IVector2 firstSubTarget = target.subtractNew(dir.scaleToNew(length / 2));
			List<IVector2> subTargets = new ArrayList<>();
			for (int i = 0; i < numBotsToUse; i++)
			{
				IVector2 subTarget = firstSubTarget.addNew(dir.multiplyNew(i));
				subTargets.add(subTarget);
			}
			TargetGroup targetGroup = new TargetGroup(target, subTargets, prio++);
			reducedTargets.add(targetGroup);
		}
		return reducedTargets;
	}
	
	
	private IVector2 getTargetOnPenaltyArea(IVector2 pointInsidePenArea)
	{
		return Geometry.getPenaltyAreaOur().withMargin(penaltyAreaMargin).nearestPointOutside(
				DefenseMath.getBisectionGoal(pointInsidePenArea),
				pointInsidePenArea);
	}
	
	
	private IVector2 getTargetOnPenaltyArea(IVector2 pointInsidePenArea, double margin)
	{
		return Geometry.getPenaltyAreaOur().withMargin(margin).nearestPointOutside(
				DefenseMath.getBisectionGoal(pointInsidePenArea),
				pointInsidePenArea);
	}
	
	
	private IVector2 getTargetOnPenaltyArea(IDefenseThreat threat)
	{
		return Geometry.getPenaltyAreaOur().withMargin(penaltyAreaMargin).nearestPointOutside(
				threat.getThreatLine().getEnd(),
				threat.getThreatLine().getStart());
	}
	
	
	private void kickBallIfEnoughTime(AthenaAiFrame aiFrame)
	{
		Optional<DefenderPenAreaRole> selectedRole = getRoles().stream().map(SwitchableDefenderRole::getNewRole)
				.map(role -> (DefenderPenAreaRole) role)
				.filter(role -> isAllowedToKick(role, aiFrame))
				.sorted(Comparator.comparingDouble(role -> botDistanceToBall(role, aiFrame.getWorldFrame().getBall())))
				.findFirst();
		selectedRole.ifPresent(DefenderPenAreaRole::setAsActiveKicker);
	}
	
	
	private boolean isAllowedToKick(DefenderPenAreaRole role, AthenaAiFrame aiFrame)
	{
		return aiFrame.getGamestate().isRunning()
				&& role.enoughTimeToKickSafely(0)
				&& aiFrame.getTacticalField().getBallResponsibility() == EBallResponsibility.DEFENSE
				&& !penaltyAreaOurWithMargin.isPointInShape(aiFrame.getWorldFrame().getBall().getPos());
	}
	
	
	private double botDistanceToBall(ARole role, ITrackedBall ball)
	{
		return role.getBot().getPos().distanceTo(ball.getPos());
	}
	
	
	private static class PenAreaSpaces
	{
		IVector2 start;
		IVector2 end;
		int numTargets = 0;
		
		
		public PenAreaSpaces(final IVector2 start, final IVector2 end)
		{
			this.start = start;
			this.end = end;
		}
		
		
		IVector2 goalCenter()
		{
			// make sure we do not reach 180deg
			return Geometry.getGoalOur().getCenter().addNew(Vector2.fromX(-5));
		}
		
		
		double diff()
		{
			double endAngle = end.subtractNew(goalCenter()).getAngle();
			return AngleMath.difference(endAngle, startAngle()) / (numTargets + 1);
		}
		
		
		double dist()
		{
			return Math.abs(diff());
		}
		
		
		double startAngle()
		{
			return start.subtractNew(goalCenter()).getAngle();
		}
	}
	
	private static class TargetGroup
	{
		final IVector2 centerDest;
		final List<IVector2> moveDestinations;
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
			this.moveDestinations = Collections.unmodifiableList(moveDest);
			this.priority = priority;
		}
		
		
		TargetGroup(TargetGroup targetGroup)
		{
			centerDest = targetGroup.centerDest;
			moveDestinations = targetGroup.moveDestinations;
			priority = targetGroup.priority;
		}
	}
	
	private static class TargetGroupAssigned extends TargetGroup
	{
		final DefenderPenAreaRole role;
		final List<IVector2> remainingMoveDests;
		final boolean protectedByAssignedRole;
		
		
		TargetGroupAssigned(final TargetGroup targetGroup, final DefenderPenAreaRole role,
				final List<IVector2> remainingMoveDests)
		{
			super(targetGroup);
			this.role = role;
			this.remainingMoveDests = remainingMoveDests;
			protectedByAssignedRole = isProtectedByPos(role.getPos());
		}
		
		
		boolean isProtectedByAssignedRole()
		{
			return protectedByAssignedRole;
		}
		
		
		boolean isProtectedByPos(IVector2 pos)
		{
			return moveDestinations.stream().map(dest -> dest.distanceTo(pos))
					.anyMatch(dist -> dist < interchangeDist);
		}
	}
}
