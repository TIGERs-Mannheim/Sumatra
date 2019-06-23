/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.defense;

import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.CENTER;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.CENTER_LEFT;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.CENTER_RIGHT;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.LEFT;
import static edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode.RIGHT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode;
import edu.tigers.sumatra.ai.pandora.roles.defense.IDefenderRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;


/**
 * Coordinate up to 3 robots that block against an opponent or ball between penArea and threat
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CenterBackGroup extends ADefenseGroup
{
	private static final Map<Integer, List<CenterBackRole.CoverMode>> COVER_MODES = new HashMap<>();
	@Configurable(comment = "When roles are that near (+2xbotRadius), they stick together")
	private static double roleDistanceThreshold = 120;
	
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
	 * @param defendingId an identifying id
	 * @param threat to defend
	 */
	public CenterBackGroup(final AObjectID defendingId, final IDefenseThreat threat)
	{
		super(defendingId);
		this.threat = threat;
	}
	
	
	@Override
	public void assignRoles()
	{
		for (SwitchableDefenderRole sRole : getRoles())
		{
			if (sRole.getOriginalRole().getType() != ERole.CENTER_BACK)
			{
				CenterBackRole centerBackRole = new CenterBackRole(threat, CENTER);
				sRole.setNewRole(centerBackRole);
			}
		}
	}
	
	
	@Override
	public void updateRoles(final AthenaAiFrame aiFrame)
	{
		super.updateRoles(aiFrame);
		
		Set<BotID> allBots = getRoles().stream()
				.map(sr -> sr.getNewRole().getBotID())
				.collect(Collectors.toSet());
		
		Optional<SwitchableDefenderRole> closestRole = getRoles().stream()
				.sorted(Comparator
						.comparingDouble(sr -> ((IDefenderRole) sr.getNewRole()).getProtectionLine(threat.getThreatLine())
								.distanceTo(sr.getNewRole().getPos())))
				.findFirst();
		if (!closestRole.isPresent())
		{
			return;
		}
		
		List<ARole> closeRoles = new ArrayList<>();
		List<ARole> allRoles = new ArrayList<>();
		for (SwitchableDefenderRole sRole : getRoles())
		{
			CenterBackRole role = (CenterBackRole) sRole.getNewRole();
			double dist2Line = role.getProtectionLine(threat.getThreatLine()).distanceTo(role.getPos());
			if (dist2Line < ((Geometry.getBotRadius() * 2) + roleDistanceThreshold))
			{
				closeRoles.add(role);
			}
			allRoles.add(role);
			role.setCompanions(allBots);
			role.setThreat(threat);
			role.setDefendCloserToGoal(false);
		}
		
		assignCoverModes(allRoles);
		assignCoverModes(closeRoles);
		
		if (closeRoles.size() > 1)
		{
			closeRoles.forEach(r -> ((CenterBackRole) r).setDefendCloserToGoal(true));
		}
	}
	
	
	private void assignCoverModes(final List<ARole> closeRoles)
	{
		List<CoverMode> coverModes = new ArrayList<>(COVER_MODES.get(closeRoles.size()));
		closeRoles.stream()
				.sorted(ANGLE_ROLE_COMPARATOR)
				.map(role -> (CenterBackRole) role)
				.forEach(role -> role.setCoverMode(coverModes.remove(0)));
	}
}
