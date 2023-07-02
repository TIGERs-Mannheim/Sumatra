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
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.CENTER;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.LEFT_0_5;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.LEFT_1;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.LEFT_1_5;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.LEFT_2;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.LEFT_2_5;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.RIGHT_0_5;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.RIGHT_1;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.RIGHT_1_5;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.RIGHT_2;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.RIGHT_2_5;


/**
 * Coordinate up to 3 robots that block on the protection line of a threat.
 */
public class CenterBackGroup extends ADefenseGroup
{
	private static final Map<Integer, List<CenterBackRole.CoverMode>> COVER_MODES = new HashMap<>();
	@Configurable(comment = "[s] time for Bots in Group to give place to new arriving Bots", defValue = "0.3")
	private static double minTimeToGivePlace = 0.3;
	@Configurable(comment = "[Bot Radius] if robot is closer than this to it's final destination, make room for it", defValue = "3.0")
	private static double minDistanceToGivePlaceSufficient = 3;
	@Configurable(comment = "[mm] Robot needs to be at least this distance close to the fastest defender to be considered close", defValue = "1000.0")
	private static double minDistanceToGivePlaceNecessary = 1000;
	@Configurable(comment = "[mm] The space between the bots (actual distance = configured distance + bot diameter)", defValue = "10.0")
	private static double distanceBetweenBots = 10;
	@Configurable(comment = "[rad] CoverModes will stay the same from the last frame, until the angle diff is larger than this", defValue = "0.1")
	private static double angleDifferenceHysteresis = 0.1;
	@Configurable(comment = "[s] Time to project position in the future for decision making", defValue = "0.2")
	private static double lookahead = 0.2;


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

	private final IDefenseThreat threat;

	private List<IDrawableShape> shapes;


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
				.forEach(sdr -> sdr.setNewRole(new CenterBackRole()));
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
		var intermediateCenter = idealProtectionPoint(closeRoles.size());
		assignDefendingPositions(center, intermediateCenter, closeRoles, allRoles);
		assignCompanions(closeRoles, allRoles);
		allRoles.forEach(r -> r.setThreat(threat));

		draw(center, timedRoles, closeRoles);
	}


	private void draw(IVector2 center, List<TimedCenterBackRole> timedRoles, List<TimedCenterBackRole> closeRoles)
	{
		shapes.add(new DrawableLine(threat.getPos(), center, Color.PINK));
		timedRoles.forEach(timed -> {
			shapes.add(new DrawableLine(timed.role.getPos(), center, Color.PINK));
			shapes.add(new DrawableAnnotation(timed.role.getPos().addNew(Vector2.fromY(-250)),
					String.format("%s%n%.3f s", timed.role.getCoverMode().toString(), timed.time), Color.PINK));
		});
		var offset = protectionLine().directionVector().getNormalVector();
		COVER_MODES.get(getRoles().size()).forEach(cm -> shapes.add(
				new DrawableCircle(center.addNew(offset.scaleToNew(getDistanceToProtectionLine(cm))),
						Geometry.getBotRadius() + 0.5 * distanceBetweenBots, Color.PINK)));
		closeRoles.forEach(
				timed -> shapes.add(new DrawableCircle(timed.role.getPos(), Geometry.getBotRadius() + 25, Color.RED)));
	}


	private List<TimedCenterBackRole> createTimedRoles(IVector2 center, List<CenterBackRole> allRoles)
	{
		return allRoles.stream()
				.map(role -> createTimedRole(center, role))
				.sorted().toList();
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
		var fastest = timedRoles.get(timedRoles.size() - 1);
		if (fastest.role.getCoverMode() == null)
		{
			return List.of();
		}
		return timedRoles.stream()
				.filter(timed -> isRoleClose(timed, fastest))
				.sorted(Comparator.comparing(timed -> timed.role.getCoverMode()))
				.toList();
	}


	private boolean isRoleClose(TimedCenterBackRole timedRole, TimedCenterBackRole fastest)
	{
		if (timedRole.role.getCoverMode() == null)
		{
			return false;
		}
		var maxAllowedTimeForCloseRoles = fastest.time + minTimeToGivePlace;
		var distanceToFastestSqr = fastest.role.getBot().getPosByTime(lookahead)
				.distanceToSqr(timedRole.role.getBot().getPosByTime(lookahead));
		if (distanceToFastestSqr > minDistanceToGivePlaceNecessary * minDistanceToGivePlaceNecessary)
		{
			return false;
		}
		return timedRole.time <= maxAllowedTimeForCloseRoles
				|| timedRole.distanceToDestSqr <= minDistanceToGivePlaceSufficient * minDistanceToGivePlaceSufficient;
	}


	private ILineSegment protectionLine()
	{
		return threat.getProtectionLine().orElseThrow(IllegalStateException::new);
	}


	private void assignCoverModes(IVector2 center, List<TimedCenterBackRole> timedRoles,
			List<TimedCenterBackRole> closeRoles)
	{
		var rolesToAssign = timedRoles.stream()
				.filter(r -> !isRoleAssigned(r))
				.collect(Collectors.toCollection(ArrayList::new));
		if (closeRolesMayNeedReordering(center, closeRoles))
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
			// Sort by slowest first
			rolesToAssign.sort(TimedCenterBackRole::compareTo);
			var allCoverModes = COVER_MODES.get(getRoles().size());
			var availableCoverModes = new ArrayList<>(allCoverModes);
			for (var timed : rolesToAssign)
			{
				var coverMode = availableCoverModes.stream()
						.min(Comparator.comparingDouble(
								cm -> getNewTrajectoryTime(offsetProtectionPoint(center, cm), timed.role)))
						.orElse(CENTER);
				availableCoverModes.remove(coverMode);
				timed.role.setCoverMode(coverMode);
			}
			// Fill in the spots with close roles that have not been assigned, without reordering them
			var sortedByCoverMode = closeRoles.stream().sorted(Comparator.comparing(timed -> timed.role.getCoverMode()))
					.toList();
			for (var timed : sortedByCoverMode)
			{
				if (rolesToAssign.stream().anyMatch(r -> r.role.getBotID().equals(timed.role.getBotID())))
				{
					continue;
				}
				timed.role.setCoverMode(availableCoverModes.remove(0));
			}
		}
	}


	private IVector2 idealProtectionPoint(int numDefender)
	{
		var goal = Geometry.getGoalOur();

		IVector2 idealProtectionPoint = DefenseMath.calculateLineDefPoint(
				threat.getPos(),
				goal.getLeftPost(),
				goal.getRightPost(),
				Geometry.getBotRadius() * numDefender);

		return protectionLine().closestPointOnPath(idealProtectionPoint);
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


	private boolean closeRolesMayNeedReordering(IVector2 center, List<TimedCenterBackRole> closeRoles)
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
				return true;
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
			return true;
		}
		return false;
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
		return getRoleToThreatAngle(role.getBot().getPosByTime(lookahead), threat.getPos());
	}


	private void assignDefendingPositions(IVector2 idealProtectionPoint, IVector2 intermediateProtectionPoint,
			List<TimedCenterBackRole> closeRoles, List<CenterBackRole> allRoles)
	{
		var coverModes = new ArrayList<>(COVER_MODES.get(closeRoles.size()));
		var closeIDs = closeRoles.stream().map(TimedCenterBackRole::role).map(ARole::getBotID).toList();
		for (var role : allRoles.stream().sorted(Comparator.comparing(CenterBackRole::getCoverMode)).toList())
		{
			// Keep the order of allRoles, but close defender will behave like a smaller CenterBackGroup to prevent holes
			if (closeIDs.contains(role.getBotID()))
			{
				role.setIdealProtectionPoint(intermediateProtectionPoint);
				role.setDistanceToProtectionLine(getDistanceToProtectionLine(coverModes.remove(0)));
			} else
			{
				role.setIdealProtectionPoint(idealProtectionPoint);
				role.setDistanceToProtectionLine(getDistanceToProtectionLine(role.getCoverMode()));
			}
		}
	}


	private double getDistanceToProtectionLine(CoverMode coverMode)
	{
		var distance = (Geometry.getBotRadius() * 2) + distanceBetweenBots;
		return switch (coverMode)
		{
			case RIGHT_2_5 -> -2.5 * distance;
			case RIGHT_2 -> -2 * distance;
			case RIGHT_1_5 -> -1.5 * distance;
			case RIGHT_1 -> -distance;
			case RIGHT_0_5 -> -0.5 * distance;
			case CENTER -> 0.0;
			case LEFT_0_5 -> 0.5 * distance;
			case LEFT_1 -> distance;
			case LEFT_1_5 -> 1.5 * distance;
			case LEFT_2 -> 2 * distance;
			case LEFT_2_5 -> 2.5 * distance;
		};
	}


	private void assignCompanions(List<TimedCenterBackRole> closeRoles, List<CenterBackRole> allRoles)
	{
		var closeCompanions = closeRoles.stream().map(TimedCenterBackRole::role).map(ARole::getBotID)
				.collect(Collectors.toUnmodifiableSet());
		var companions = allRoles.stream().map(ARole::getBotID).collect(Collectors.toUnmodifiableSet());
		allRoles.forEach(cbr -> cbr.setCompanions(companions));
		allRoles.forEach(cbr -> cbr.setCloseCompanions(closeCompanions));
	}


	private record TimedCenterBackRole(CenterBackRole role, double time, double distanceToDestSqr)
			implements Comparable<TimedCenterBackRole>
	{

		@Override
		public int compareTo(TimedCenterBackRole o)
		{
			return -Double.compare(time, o.time);
		}
	}
}
