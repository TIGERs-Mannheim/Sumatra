/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.trajectory.ITrajectory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.CENTER;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.CENTER_LEFT;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.CENTER_RIGHT;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.LEFT;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.RIGHT;


/**
 * Coordinate up to 3 robots that block on the protection line of a threat.
 */
public class CenterBackGroup extends ADefenseGroup
{
	private static final Map<Integer, List<CenterBackRole.CoverMode>> COVER_MODES = new HashMap<>();
	@Configurable(comment = "[s] time for Bots in Group to give place to new arriving Bots", defValue = "0.5")
	private static double minTimeToGivePlace = 0.5;
	@Configurable(comment = "[mm] The space between the bots (actual distance = configured distance + bot diameter)", defValue = "15.0")
	private static double distanceBetweenBots = 15;
	@Configurable(comment = "CoverModes will stay the same from the last frame, until the angle diff is larger than this", defValue = "0.1")
	private static double coverModeAngleDifferenceHysteresis = 0.1;


	static
	{
		ConfigRegistration.registerClass("plays", CenterBackGroup.class);
	}


	static
	{
		COVER_MODES.put(0, Collections.emptyList());
		COVER_MODES.put(1, Collections.singletonList(CENTER));
		COVER_MODES.put(2, Arrays.asList(CENTER_RIGHT, CENTER_LEFT));
		COVER_MODES.put(3, Arrays.asList(RIGHT, CENTER, LEFT));
	}

	private final IDefenseThreat threat;


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

		List<CenterBackRole> allRoles = getRoles().stream()
				.map(SwitchableDefenderRole::getNewRole)
				.map(CenterBackRole.class::cast)
				.toList();
		allRoles.forEach(r -> r.setThreat(threat));
		var closeRoles = extractCloseRoles(allRoles);
		assignCoverModes(allRoles, closeRoles);
		assignDistanceOffsets(closeRoles);
		assignCompanions(allRoles);

		var shapes = aiFrame.getShapes(EAiShapesLayer.DEFENSE_CENTER_BACK);
		allRoles.forEach(role -> {
			shapes.add(new DrawableLine(Lines.segmentFromPoints(role.getPos(), threat.getPos()), Color.PINK));
			shapes.add(new DrawableAnnotation(role.getPos().addNew(Vector2.fromX(-300)), role.getCoverMode().toString(),
					Color.WHITE));
		});
	}


	private List<CenterBackRole> extractCloseRoles(final List<CenterBackRole> allRoles)
	{
		var smallestTimeToDest = allRoles.stream()
				.mapToDouble(this::getTimeToDist)
				.min().orElse(0.0);
		return allRoles.stream()
				.filter(role -> getTimeToDist(role) <= smallestTimeToDest + minTimeToGivePlace)
				.sorted(Comparator.comparing(CenterBackRole::getCoverMode))
				.toList();
	}


	private void assignCoverModes(final List<CenterBackRole> allRoles, final List<CenterBackRole> closeRoles)
	{
		if (rolesExistWithUnassignedCoverMode(allRoles) || closeRolesSpreadTooMuch(closeRoles))
		{
			List<CoverMode> newCoverModes = new ArrayList<>(COVER_MODES.get(allRoles.size()));

			allRoles.stream()
					.sorted(Comparator.comparing(this::getRoleToThreatAngle, AngleMath::compareAngle))
					.forEach(role -> role.setCoverMode(newCoverModes.remove(0)));
		}
	}


	private boolean rolesExistWithUnassignedCoverMode(List<CenterBackRole> allRoles)
	{

		return allRoles.stream().anyMatch(role -> role.getCoverMode() == null);
	}


	private boolean closeRolesSpreadTooMuch(List<CenterBackRole> closeRoles)
	{
		var closeAngles = closeRoles.stream()
				.map(this::getRoleToThreatAngle)
				.toList();

		var maxAngleDiff = closeAngles.stream()
				.flatMapToDouble(angle1 -> closeAngles.stream().mapToDouble(angle2 -> AngleMath.diffAbs(angle1, angle2)))
				.max().orElse(0.0);

		return maxAngleDiff > coverModeAngleDifferenceHysteresis;

	}


	private double getRoleToThreatAngle(ARole role)
	{
		return getRoleToThreatAngle(role.getBot().getPosByTime(0.2), threat.getPos());
	}


	private void assignDistanceOffsets(List<CenterBackRole> closeRoles)
	{
		// Keep the order of allRoles, but ignore defender that are currently not close enough
		// We will assign the distances to protection line as if the CenterBack Group is smaller (With only the close roles)
		var coverModes = new ArrayList<>(COVER_MODES.get(closeRoles.size()));
		closeRoles.forEach(role -> role.setDistanceToProtectionLine(getDistanceToProtectionLine(coverModes.remove(0))));
	}


	private double getDistanceToProtectionLine(CoverMode coverMode)
	{
		var distance = (Geometry.getBotRadius() * 2) + distanceBetweenBots;
		return switch (coverMode)
				{
					case RIGHT -> -distance;
					case CENTER_RIGHT -> -(distance / 2);
					case CENTER -> 0.0;
					case CENTER_LEFT -> distance / 2;
					case LEFT -> distance;
				};
	}


	private double getTimeToDist(CenterBackRole role)
	{
		return role.getBot().getCurrentTrajectory().map(ITrajectory::getTotalTime).orElse(0.0);
	}


	private void assignCompanions(final List<CenterBackRole> roles)
	{
		Set<BotID> companions = roles.stream()
				.map(ARole::getBotID)
				.collect(Collectors.toSet());
		roles.forEach(cbr -> cbr.setCompanions(companions));
	}
}
