/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.CENTER;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.LEFT_0_5;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.LEFT_1;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.LEFT_1_5;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.LEFT_2;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.LEFT_2_5;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.RIGHT_0_5;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.RIGHT_1;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.RIGHT_1_5;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.RIGHT_2;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CoverMode.RIGHT_2_5;


/**
 * Coordinate up to 3 robots that block on the protection line of a threat.
 */
public class CenterBackGroup extends ADefenseGroup
{
	private static final Map<Integer, List<CoverMode>> COVER_MODES = new HashMap<>();
	private static final Comparator<TimedCenterBackRole> SORT_BY_SLOWEST_FIRST = Comparator.comparing(
			timed -> -timed.time);
	private static final Comparator<TimedCenterBackRole> SORT_BY_COVER_MODE = Comparator.comparing(
			timed -> timed.role.getCoverMode());
	@Configurable(comment = "[s] time for Bots in Group to give place to new arriving Bots, hysteresis upper limit", defValue = "0.3")
	private static double minTimeToGivePlaceUpper = 0.3;
	@Configurable(comment = "[s] time for Bots in Group to give place to new arriving Bots, hysteresis lower limit", defValue = "0.25")
	private static double minTimeToGivePlaceLower = 0.25;
	@Configurable(comment = "[Bot Radius] if robot is closer than this to it's final destination, make room for it", defValue = "3.0")
	private static double minDistanceToGivePlaceSufficient = 3;
	@Configurable(comment = "[mm] Robot needs to be at least this distance close to the fastest defender to be considered close", defValue = "1000.0")
	private static double minDistanceToGivePlaceNecessary = 1000;
	@Getter
	@Configurable(comment = "[mm] The space between the bots (actual distance = configured distance + bot diameter)", defValue = "10.0")
	private static double distanceBetweenBots = 10;
	@Configurable(comment = "[rad] CoverModes will stay the same from the last frame, until the angle diff is larger than this", defValue = "0.1")
	private static double angleDifferenceHysteresis = 0.1;
	@Configurable(comment = "[s] Time to project position in the future for decision making", defValue = "0.2")
	private static double lookahead = 0.2;
	@Configurable(comment = "[m/s] the speed the centerbacks will back off with as soon as more closeRoles arrived", defValue = "0.25")
	private static double backOffSpeed = 0.25;

	static
	{
		ConfigRegistration.registerClass("plays", CenterBackGroup.class);
	}


	static
	{
		COVER_MODES.put(0, List.of());
		COVER_MODES.put(1, List.of(CENTER));
		COVER_MODES.put(2, List.of(RIGHT_0_5, LEFT_0_5));
		COVER_MODES.put(3, List.of(RIGHT_1, CENTER, LEFT_1));
		COVER_MODES.put(4, List.of(RIGHT_1_5, RIGHT_0_5, LEFT_0_5, LEFT_1_5));
		COVER_MODES.put(5, List.of(RIGHT_2, RIGHT_1, CENTER, LEFT_1, LEFT_2));
		COVER_MODES.put(6, List.of(RIGHT_2_5, RIGHT_1_5, RIGHT_0_5, LEFT_0_5, LEFT_1_5, LEFT_2_5));
	}

	@Getter
	private final IDefenseThreat threat;
	private List<IDrawableShape> shapes;

	@Setter
	private CenterBackGroupState state;


	/**
	 * New group
	 *
	 * @param threat to defend
	 */
	public CenterBackGroup(final IDefenseThreat threat)
	{
		this.threat = threat;
	}


	@Override
	public void assignRoles()
	{
		getRoles().stream()
				.filter(sdr -> sdr.getOriginalRole().getType() != ERole.CENTER_BACK)
				.forEach(sdr -> {
					var hysteresis = new Hysteresis(minTimeToGivePlaceLower, minTimeToGivePlaceUpper);
					state.timeToGivePlaceHysteresis.put(sdr.getOriginalRole().getBotID(), hysteresis);
					sdr.setNewRole(new CenterBackRole());
				});
	}


	@Override
	public void updateRoles(final AthenaAiFrame aiFrame)
	{
		super.updateRoles(aiFrame);
		shapes = aiFrame.getShapes(EAiShapesLayer.DEFENSE_CENTER_BACK);

		List<CenterBackRole> allRoles = getRoles().stream()
				.map(SwitchableDefenderRole::getNewRole)
				.map(CenterBackRole.class::cast)
				.toList();
		var center = idealProtectionPoint(roles.size());
		var timedRoles = createTimedRoles(center, allRoles);
		var closeRoles = extractCloseRoles(timedRoles);
		assignCoverModes(center, timedRoles, closeRoles);
		var idealOnlyCloseProtectionPoint = idealProtectionPoint(closeRoles.size());
		var intermediateCenter = intermediateProtectionPoint(idealOnlyCloseProtectionPoint, aiFrame);
		assignDefendingPositions(center, intermediateCenter, closeRoles, allRoles);
		assignCompanions(closeRoles, allRoles);
		allRoles.forEach(r -> r.setThreat(threat));

		draw(center, timedRoles, closeRoles, idealOnlyCloseProtectionPoint, intermediateCenter);
	}


	private void draw(
			IVector2 center,
			List<TimedCenterBackRole> timedRoles,
			List<TimedCenterBackRole> closeRoles,
			IVector2 idealOnlyCloseProtectionPoint,
			IVector2 intermediateCenter
	)
	{
		shapes.add(new DrawableLine(threat.getPos(), center, Color.PINK));
		timedRoles.forEach(timed -> {
			shapes.add(new DrawableLine(timed.role.getPos(), center, Color.PINK));
			shapes.add(new DrawableAnnotation(
					timed.role.getPos().addNew(Vector2.fromY(-250)),
					String.format("%s%n%.3f s", timed.role.getCoverMode().toString(), timed.time), Color.PINK
			));
		});
		var offset = protectionLine().directionVector().getNormalVector();
		COVER_MODES.get(getRoles().size()).forEach(cm -> shapes.add(
				new DrawableCircle(
						center.addNew(offset.scaleToNew(getDistanceToProtectionLine(cm))),
						Geometry.getBotRadius() + 0.5 * distanceBetweenBots, Color.PINK
				)));
		closeRoles.forEach(
				timed -> shapes.add(new DrawableCircle(timed.role.getPos(), Geometry.getBotRadius() + 25, Color.RED)));
		shapes.add(new DrawableCircle(Circle.createCircle(idealOnlyCloseProtectionPoint, 20), Color.CYAN.darker()));
		shapes.add(new DrawableCircle(Circle.createCircle(intermediateCenter, 30), Color.CYAN.brighter()));
	}


	private List<TimedCenterBackRole> createTimedRoles(IVector2 center, List<CenterBackRole> allRoles)
	{
		return allRoles.stream()
				.map(role -> createTimedRole(center, role))
				.sorted(SORT_BY_SLOWEST_FIRST)
				.toList();
	}


	private TimedCenterBackRole createTimedRole(IVector2 center, CenterBackRole role)
	{
		CoverMode coverMode;
		if (role.getCoverMode() == null)
		{
			coverMode = CENTER;
		} else
		{
			coverMode = role.getCoverMode();
		}
		var destination = offsetProtectionPoint(center, coverMode);
		var time = getNewTrajectoryTime(destination, role);
		var distanceSqr = destination.distanceToSqr(role.getBot().getPosByTime(lookahead));
		return new TimedCenterBackRole(role, time, distanceSqr);
	}


	private List<TimedCenterBackRole> extractCloseRoles(List<TimedCenterBackRole> timedRoles)
	{
		var fastest = timedRoles.getLast();
		if (fastest.role.getCoverMode() == null)
		{
			return List.of();
		}
		return timedRoles.stream()
				.filter(timed -> isRoleClose(timed, fastest))
				.sorted(SORT_BY_COVER_MODE)
				.toList();
	}


	private boolean isRoleClose(TimedCenterBackRole timedRole, TimedCenterBackRole fastest)
	{
		if (timedRole.role.getCoverMode() == null)
		{
			return false;
		}
		double timeAdapted = timedRole.time - fastest.time;
		var hysteresis = state.timeToGivePlaceHysteresis.computeIfAbsent(
				timedRole.role.getBotID(),
				botID -> new Hysteresis(minTimeToGivePlaceLower, minTimeToGivePlaceUpper)
		);
		hysteresis.update(timeAdapted);
		double distanceToFastestSqr = fastest.role.getBot().getPosByTime(lookahead)
				.distanceToSqr(timedRole.role.getBot().getPosByTime(lookahead));
		if (distanceToFastestSqr > minDistanceToGivePlaceNecessary * minDistanceToGivePlaceNecessary)
		{
			return false;
		}
		return hysteresis.isLower()
				|| timedRole.distanceToDestSqr <= minDistanceToGivePlaceSufficient * minDistanceToGivePlaceSufficient;
	}


	private ILineSegment protectionLine()
	{
		return threat.getProtectionLine().orElseThrow(IllegalStateException::new);
	}


	private void assignCoverModes(
			IVector2 center, List<TimedCenterBackRole> timedRoles,
			List<TimedCenterBackRole> closeRoles
	)
	{
		var rolesToAssign = timedRoles.stream()
				.filter(r -> !isRoleAssigned(r))
				.collect(Collectors.toCollection(ArrayList::new));
		var closeReorderCause = closeRolesMayNeedReordering(center, closeRoles);
		if (closeReorderCause != EReorderCause.NONE)
		{
			closeRoles.stream()
					.filter(r1 -> rolesToAssign.stream().noneMatch(r2 -> r1.role.getBotID().equals(r2.role.getBotID())))
					.forEach(rolesToAssign::add);
		}
		if (!rolesToAssign.isEmpty())
		{
			// Reassign all roles that are not close
			rolesToAssign.addAll(timedRoles.stream()
					.filter(r1 -> rolesToAssign.stream().noneMatch(r2 -> r1.role.getBotID().equals(r2.role.getBotID())))
					.filter(r1 -> closeRoles.stream().noneMatch(r2 -> r1.role.getBotID().equals(r2.role.getBotID())))
					.toList());
			rolesToAssign.sort(SORT_BY_SLOWEST_FIRST);
			var allCoverModes = COVER_MODES.get(getRoles().size());
			var availableCoverModes = new ArrayList<>(allCoverModes);
			for (var timed : rolesToAssign)
			{
				var coverMode = selectNewCoverMode(availableCoverModes, closeRoles, timed, center, closeReorderCause);
				availableCoverModes.remove(coverMode);
				timed.role.setCoverMode(coverMode);
			}
			// Fill in the spots with close roles that have not been assigned, without reordering them
			var sortedByCoverMode = closeRoles.stream()
					.sorted(SORT_BY_COVER_MODE)
					.toList();
			for (var timed : sortedByCoverMode)
			{
				if (rolesToAssign.stream().anyMatch(r -> r.role.getBotID().equals(timed.role.getBotID())))
				{
					continue;
				}
				timed.role.setCoverMode(availableCoverModes.removeFirst());
			}
		}
	}


	private CoverMode selectNewCoverMode(
			List<CoverMode> availableCoverModes,
			List<TimedCenterBackRole> closeRoles,
			TimedCenterBackRole timed,
			IVector2 center,
			EReorderCause closeReorderCause
	)
	{
		var fastest = availableCoverModes.stream()
				.min(Comparator.comparingDouble(cm -> getNewTrajectoryTime(offsetProtectionPoint(center, cm), timed.role)))
				.orElse(CENTER);

		if (closeReorderCause != EReorderCause.SPREAD)
		{
			return fastest;
		}
		boolean isCloseRole = closeRoles.stream()
				.map(TimedCenterBackRole::role)
				.map(ARole::getBotID)
				.anyMatch(id -> id == timed.role.getBotID());
		if (isCloseRole && availableCoverModes.contains(timed.role.getCoverMode()))
		{
			// Apply hysteresis for close roles
			double oldTime = getNewTrajectoryTime(offsetProtectionPoint(center, timed.role.getCoverMode()), timed.role);
			double newTime = getNewTrajectoryTime(offsetProtectionPoint(center, fastest), timed.role);
			if (oldTime < newTime + 0.1)
			{
				return timed.role.getCoverMode();
			}
		}
		return fastest;
	}


	private IVector2 idealProtectionPoint(int numDefender)
	{
		IVector2 idealProtectionPoint = DefenseMath.calculateGoalDefPoint(
				threat.getPos(),
				Geometry.getBotRadius() * numDefender
		);

		return protectionLine().closestPointOnPath(idealProtectionPoint);
	}


	private IVector2 intermediateProtectionPoint(IVector2 idealOnlyCloseProtectionPoint, AthenaAiFrame aiFrame)
	{
		var threatLine = threat.getThreatLine();
		var threatPos = threatLine.getPathStart();
		var threatTarget = threatLine.getPathEnd();

		var now = aiFrame.getWorldFrame().getTimestamp();
		var timeSinceLastCalculation = (now - state.lastDistanceCalculationTimestamp) * 1e-9;

		var wantedDistanceFromThreatTarget = threatTarget.distanceTo(idealOnlyCloseProtectionPoint);
		if (state.lastDistanceCalculationTimestamp == 0
				|| wantedDistanceFromThreatTarget > state.currentDistanceFromThreatTarget)
		{
			state.currentDistanceFromThreatTarget = wantedDistanceFromThreatTarget;
		} else
		{
			state.currentDistanceFromThreatTarget -= backOffSpeed * timeSinceLastCalculation * 1e3;
			state.currentDistanceFromThreatTarget = Math.max(
					state.currentDistanceFromThreatTarget,
					wantedDistanceFromThreatTarget
			);
		}

		state.lastDistanceCalculationTimestamp = aiFrame.getWorldFrame().getTimestamp();

		var closestDefensePos = protectionLine().getPathEnd();
		var minDistance = threatTarget.distanceTo(closestDefensePos);
		var maxDistance = threatLine.getLength();

		state.currentDistanceFromThreatTarget = SumatraMath.cap(
				state.currentDistanceFromThreatTarget,
				minDistance,
				maxDistance
		);
		return LineMath.stepAlongLine(threatTarget, threatPos, state.currentDistanceFromThreatTarget);
	}


	private IVector2 offsetProtectionPoint(IVector2 centerPoint, CoverMode coverMode)
	{
		var distance = getDistanceToProtectionLine(coverMode);
		var offset = protectionLine().directionVector().getNormalVector().scaleToNew(distance);
		return centerPoint.addNew(offset);
	}


	private double getNewTrajectoryTime(IVector2 destination, CenterBackRole role)
	{
		return TrajectoryGenerator.generatePositionTrajectory(role.getBot(), destination).getTotalTime();
	}


	private EReorderCause closeRolesMayNeedReordering(IVector2 center, List<TimedCenterBackRole> closeRoles)
	{
		var sortedByAngles = closeRoles.stream()
				.map(TimedCenterBackRole::role)
				.sorted(Comparator.comparingDouble(this::getRoleToThreatAngle))
				.toList();
		for (int index = 0; index < closeRoles.size(); ++index)
		{
			if (!sortedByAngles.get(index).getBotID().equals(closeRoles.get(index).role.getBotID()))
			{
				// Indicates swapping might improve the overall time
				shapes.add(new DrawableAnnotation(center, "SWAPPING", Color.PINK));
				return EReorderCause.SWAPPING;
			}
		}
		var closeAngles = closeRoles.stream()
				.map(TimedCenterBackRole::role)
				.map(this::getRoleToThreatAngle).toList();

		var maxAngleDiff = closeAngles.stream()
				.flatMapToDouble(angle1 -> closeAngles.stream().mapToDouble(angle2 -> AngleMath.diffAbs(angle1, angle2)))
				.max().orElse(0.0);

		var closeRolesSpreadTooMuch = maxAngleDiff > angleDifferenceHysteresis;
		if (closeRolesSpreadTooMuch)
		{
			// Close roles are spread out quite a lot
			shapes.add(new DrawableAnnotation(center, "SPREAD", Color.PINK));
			return EReorderCause.SPREAD;
		}
		return EReorderCause.NONE;
	}


	private boolean isRoleAssigned(TimedCenterBackRole timedRole)
	{
		var allowedModes = COVER_MODES.get(getRoles().size());
		return timedRole.role.getCoverMode() != null
				&& allowedModes.contains(timedRole.role.getCoverMode())
				&& threat.sameAs(timedRole.role.getThreat());
	}


	private double getRoleToThreatAngle(ARole role)
	{
		IVector2 goalCenter = Geometry.getGoalOur().getCenter();
		IVector2 goal2Role = role.getBot().getPosByTime(lookahead).subtractNew(goalCenter);
		IVector2 goal2Threat = threat.getPos().subtractNew(goalCenter);
		return goal2Threat.angleTo(goal2Role).orElse(0.0);
	}


	private void assignDefendingPositions(
			IVector2 idealProtectionPoint, IVector2 intermediateProtectionPoint,
			List<TimedCenterBackRole> closeRoles, List<CenterBackRole> allRoles
	)
	{
		var coverModes = new ArrayList<>(COVER_MODES.get(closeRoles.size()));
		var closeIDs = closeRoles.stream().map(TimedCenterBackRole::role).map(ARole::getBotID).toList();
		for (var role : allRoles.stream().sorted(Comparator.comparing(CenterBackRole::getCoverMode)).toList())
		{
			// Keep the order of allRoles, but close defender will behave like a smaller CenterBackGroup to prevent holes
			if (closeIDs.contains(role.getBotID()))
			{
				role.setIdealProtectionPoint(intermediateProtectionPoint);
				setDistancesToProtectionLine(role, coverModes.removeFirst());

			} else
			{
				role.setIdealProtectionPoint(idealProtectionPoint);
				setDistancesToProtectionLine(role, role.getCoverMode());
			}
		}
	}


	private void setDistancesToProtectionLine(CenterBackRole role, CoverMode coverMode)
	{
		role.setDistanceToProtectionLine(getDistanceToProtectionLine(coverMode));
		role.setDistanceToProtectionLineIntercept(getDistanceToProtectionLineIntercept(coverMode));
	}


	private double getDistanceToProtectionLine(CoverMode coverMode)
	{
		return getDistanceToProtectionLine(coverMode, distanceBetweenBots);
	}


	private double getDistanceToProtectionLineIntercept(CoverMode coverMode)
	{
		return getDistanceToProtectionLine(coverMode, -distanceBetweenBots);
	}


	private double getDistanceToProtectionLine(CoverMode coverMode, double marginBetweenBots)
	{
		var distance = (Geometry.getBotRadius() * 2) + marginBetweenBots;
		return coverMode.getDistanceFactor() * distance;
	}


	private void assignCompanions(List<TimedCenterBackRole> closeRoles, List<CenterBackRole> allRoles)
	{
		var closeCompanions = closeRoles.stream().map(TimedCenterBackRole::role).map(ARole::getBotID)
				.collect(Collectors.toUnmodifiableSet());
		allRoles.forEach(cbr -> cbr.setCompanions(allRoles));
		allRoles.forEach(cbr -> cbr.setCloseCompanions(closeCompanions));
	}


	private enum EReorderCause
	{
		NONE,
		SPREAD,
		SWAPPING,
	}

	private record TimedCenterBackRole(CenterBackRole role, double time, double distanceToDestSqr)
	{
	}

	public static class CenterBackGroupState
	{
		private final Map<BotID, Hysteresis> timeToGivePlaceHysteresis = new HashMap<>();
		private double currentDistanceFromThreatTarget = 0;
		private long lastDistanceCalculationTimestamp = 0;
	}
}
