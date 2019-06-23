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
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseGroup;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPenAreaRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.skillsystem.skills.MoveOnPenaltyAreaSkill;
import edu.tigers.sumatra.skillsystem.skills.util.penarea.IDefensePenArea;
import edu.tigers.sumatra.skillsystem.skills.util.penarea.PenAreaFactory;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * The group containing all penArea bots
 */
public class PenAreaGroup extends ADefenseGroup
{
	@Configurable(defValue = "5.0", comment = "Distance offset to add to bot radius to determine same cluster of bots")
	private static double clusterDistanceOffset = 5.0;
	
	@Configurable(defValue = "50.0")
	private static double penaltyAreaMargin = 50;
	
	@Configurable(defValue = "350.0")
	private static double interchangeDist = 350.0;
	
	@Configurable(comment = "Distance between the penalty area defenders times the ball radius", defValue = "0.5")
	private static double distBetweenPenAreaBotsFactor = 0.5;
	
	static
	{
		ConfigRegistration.registerClass("plays", PenAreaGroup.class);
	}
	
	private IDefensePenArea penArea = PenAreaFactory.buildWithMargin(0);
	
	private List<DefenseThreatAssignment> threats = new ArrayList<>();
	private AthenaAiFrame aiFrame;
	
	
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
		this.aiFrame = aiFrame;
		
		List<DefenseThreatAssignment> threatAssignments = new ArrayList<>(threats);
		List<TargetGroup> reducedTargets = reduceThreatAssignments(threatAssignments);
		
		threatAssignments.forEach(t -> getShapes(aiFrame).add(
				new DrawableLine(t.getThreat().getThreatLine(), Color.black)));
		threatAssignments.forEach(t -> getShapes(aiFrame).add(
				new DrawableLine(Line.fromPoints(t.getThreat().getPos(), Geometry.getGoalOur().getRightPost()),
						Color.black)));
		threatAssignments.forEach(t -> getShapes(aiFrame).add(
				new DrawableLine(Line.fromPoints(t.getThreat().getPos(), Geometry.getGoalOur().getLeftPost()),
						Color.black)));
		
		reducedTargets.forEach(t -> getShapes(aiFrame).add(
				new DrawableCircle(Circle.createCircle(t.centerDest, 30), Color.blue)));
		reducedTargets.forEach(t -> t.moveDestinations.forEach(m -> getShapes(aiFrame).add(
				new DrawableLine(Line.fromPoints(t.centerDest, m), Color.blue))));
		
		List<PenAreaSpaces> spaces = fillPenAreaSpaces(reducedTargets);
		List<TargetGroup> allTargetsSorted = getAllSortedTargets(reducedTargets, spaces);
		
		spaces.forEach(s -> getShapes(aiFrame).add(
				new DrawableLine(Line.fromPoints(s.start, s.end), Color.orange)));
		spaces.forEach(s -> getShapes(aiFrame).add(
				new DrawableAnnotation(Lines.segmentFromPoints(s.start, s.end).getCenter(), String.valueOf(s.numTargets),
						Color.orange)));
		
		allTargetsSorted.forEach(t -> getShapes(aiFrame).add(
				new DrawableCircle(Circle.createCircle(t.centerDest, 60), Color.magenta)));
		allTargetsSorted.forEach(t -> getShapes(aiFrame).add(
				new DrawableAnnotation(t.centerDest, String.valueOf(t.priority), Color.magenta)
						.withCenterHorizontally(true)));
		
		assignTargetGroupsToRoles(allTargetsSorted);
		
		keepDistanceToBallIfRequired(aiFrame);
		
		kickBallIfEnoughTime(aiFrame);
	}
	
	
	private void keepDistanceToBallIfRequired(final AthenaAiFrame aiFrame)
	{
		if (aiFrame.getGamestate().isDistanceToBallRequired() || aiFrame.getTacticalField().isOpponentWillDoIcing())
		{
			freeForbiddenArea(aiFrame);
		}
		if (aiFrame.getGamestate().getState() == EGameState.BALL_PLACEMENT)
		{
			freeBallPlacementCorridor(aiFrame);
		}
	}
	
	
	private List<TargetGroup> reduceThreatAssignments(final List<DefenseThreatAssignment> threatAssignments)
	{
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
		return reducedTargets;
	}
	
	
	private List<IDrawableShape> getShapes(AthenaAiFrame frame)
	{
		return frame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.DEFENSE_PENALTY_AREA_GROUP);
	}
	
	
	/**
	 * Move the target out of circle step by step towards goal until it is not in circle or the penArea is reached
	 * 
	 * @param target
	 * @param circle
	 * @return
	 */
	private IVector2 moveTargetOutOfCircle(IVector2 target, ICircle circle)
	{
		IVector2 goal = Geometry.getGoalOur().getCenter();
		IVector2 dir = goal.subtractNew(circle.center()).scaleTo(5);
		IVector2 newTarget = target;
		IDefensePenArea penaltyArea = PenAreaFactory.buildWithMargin(0);
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
		double margin = RuleConstraints.getStopRadius() + 2 * Geometry.getBotRadius();
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
				newPos = (penArea.withMargin(penaltyAreaMargin).isPointInShape(newPos)
						|| !Geometry.getField().isPointInShape(newPos))
								? line.nearestPointOnLine(role.getPos()).addNew(
										line.directionVector().getNormalVector().scaleToNew(-margin))
								: newPos;
				if (penArea.withMargin(penaltyAreaMargin).isPointInShape(newPos))
				{
					newPos = penArea.withMargin(penaltyAreaMargin).nearestPointOutside(newPos,
							newPos.addNew(line.directionVector()));
				}
				role.setTarget(newPos);
			}
		}
	}
	
	
	private void freeForbiddenArea(final AthenaAiFrame aiFrame)
	{
		double radius = RuleConstraints.getStopRadius() + Geometry.getBotRadius() + 10;
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
				base.addNew(Vector2.fromAngle(nextAngle).scaleTo(Geometry.getPenaltyAreaFrontLineLength())),
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
			
			IVector2 designatedTarget = nextTargetGroup.remainingMoveDests.remove(0);
			if (currentTarget.isPresent() &&
					currentTargetIsMoreImportant(currentTarget.get(), nextTargetGroup) &&
					!currentTarget.get().isProtectedByAssignedRole())
			{
				role.setTarget(currentTarget.get().centerDest);
				getShapes(aiFrame)
						.add(new DrawableLine(Line.fromPoints(role.getPos(), currentTarget.get().centerDest), Color.red));
				getShapes(aiFrame).add(new DrawableLine(Line.fromPoints(role.getPos(), designatedTarget), Color.yellow));
			} else
			{
				role.setTarget(designatedTarget);
				getShapes(aiFrame).add(new DrawableLine(Line.fromPoints(role.getPos(), designatedTarget), Color.green));
			}
		}
	}
	
	
	private boolean currentTargetIsMoreImportant(final TargetGroupAssigned currentTarget,
			final TargetGroupAssigned nextTargetGroup)
	{
		return currentTarget.priority < nextTargetGroup.priority;
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
					.addNew(Vector2.fromAngle(angle).scaleTo(Geometry.getPenaltyAreaFrontLineLength()));
			IVector2 moveDest = getTargetOnPenaltyArea(dir);
			
			TargetGroup targetGroup = new TargetGroup(moveDest, 20);
			allTargets.add(targetGroup);
			lastAngle = angle;
		}
	}
	
	
	private List<PenAreaSpaces> fillPenAreaSpaces(
			final List<TargetGroup> reducedTargets)
	{
		List<IVector2> penAreaMarkers = new ArrayList<>(reducedTargets.size() + 2);
		penAreaMarkers.addAll(reducedTargets.stream().map(g -> g.centerDest).collect(Collectors.toList()));
		penAreaMarkers.add(Vector2.fromXY(Geometry.getGoalOur().getCenter().x(),
				penArea.getFrontLineHalfLength() + penaltyAreaMargin));
		penAreaMarkers.add(Vector2.fromXY(Geometry.getGoalOur().getCenter().x(),
				-penArea.getFrontLineHalfLength() - penaltyAreaMargin));
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
			
			IVector2 base = penArea.projectPointOnPenaltyAreaLine(target);
			IVector2 dir = penArea.stepAlongPenArea(base, -Geometry.getBallRadius() * 2 * (numBotsToUse / 2.0))
					.subtractNew(base)
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
		IVector2 targetOnPenArea = penArea.withMargin(penaltyAreaMargin)
				.nearestPointOutside(
						DefenseMath.getBisectionGoal(pointInsidePenArea),
						pointInsidePenArea);
		
		return nearOpponent(targetOnPenArea)
				.map(ITrackedBot::getPos)
				.map(this::validFinalDestination)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.orElse(targetOnPenArea);
	}
	
	
	private Optional<ITrackedBot> nearOpponent(final IVector2 intermediatePos)
	{
		return aiFrame.getWorldFrame().getFoeBots().values().stream()
				.filter(t -> t.getPos().distanceTo(intermediatePos) < MoveOnPenaltyAreaSkill.getMinDistToOpponent() + 5)
				.min(Comparator.comparingDouble(t -> t.getPos().distanceTo(intermediatePos)));
	}
	
	
	private Optional<IVector2> validFinalDestination(final IVector2 obstacle)
	{
		IVector2 protectFromBallDest = LineMath.stepAlongLine(obstacle,
				aiFrame.getWorldFrame().getBall().getPos(),
				MoveOnPenaltyAreaSkill.getMinDistToOpponent() + 10);
		if (penArea.withMargin(penaltyAreaMargin).isPointInShape(protectFromBallDest))
		{
			return Optional.empty();
		}
		return Optional.of(protectFromBallDest);
	}
	
	
	private IVector2 getTargetOnPenaltyArea(IVector2 pointInsidePenArea, double margin)
	{
		return penArea.withMargin(margin).nearestPointOutside(
				DefenseMath.getBisectionGoal(pointInsidePenArea),
				pointInsidePenArea);
	}
	
	
	private IVector2 getTargetOnPenaltyArea(IDefenseThreat threat)
	{
		return penArea.withMargin(penaltyAreaMargin).nearestPointOutside(
				threat.getThreatLine().getEnd(),
				threat.getThreatLine().getStart());
	}
	
	
	private void kickBallIfEnoughTime(AthenaAiFrame aiFrame)
	{
		Optional<DefenderPenAreaRole> selectedRole = getRoles().stream().map(SwitchableDefenderRole::getNewRole)
				.map(role -> (DefenderPenAreaRole) role)
				.filter(role -> isAllowedToKick(role, aiFrame))
				.min(Comparator.comparingDouble(role -> botDistanceToBall(role, aiFrame.getWorldFrame().getBall())));
		selectedRole.ifPresent(DefenderPenAreaRole::setAsActiveKicker);
	}
	
	
	private boolean isAllowedToKick(DefenderPenAreaRole role, AthenaAiFrame aiFrame)
	{
		return aiFrame.getGamestate().isRunning()
				&& role.enoughTimeToKickSafely(0)
				&& aiFrame.getTacticalField().getBallResponsibility() == EBallResponsibility.DEFENSE
				&& !penArea.withMargin(Geometry.getBotRadius() * 2)
						.isPointInShape(aiFrame.getWorldFrame().getBall().getPos());
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
