/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

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
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole.CoverMode;
import edu.tigers.sumatra.ids.BotID;


/**
 * Coordinate up to 3 robots that block on the protection line of a threat.
 */
public class CenterBackGroup extends ADefenseGroup
{
	private static final Map<Integer, List<CenterBackRole.CoverMode>> COVER_MODES = new HashMap<>();
	
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
		for (SwitchableDefenderRole sRole : getRoles())
		{
			if (sRole.getOriginalRole().getType() != ERole.CENTER_BACK)
			{
				CenterBackRole centerBackRole = new CenterBackRole(threat);
				sRole.setNewRole(centerBackRole);
			}
		}
	}
	
	
	@Override
	public void updateRoles(final AthenaAiFrame aiFrame)
	{
		super.updateRoles(aiFrame);
		
		List<CenterBackRole> allRoles = getRoles().stream()
				.map(sdr -> (CenterBackRole) sdr.getNewRole())
				.collect(Collectors.toList());
		allRoles.forEach(r -> r.setThreat(threat));
		assignCoverModes(allRoles);
		assignCompanions(allRoles);
	}
	
	
	private void assignCoverModes(final List<CenterBackRole> roles)
	{
		List<CoverMode> coverModes = new ArrayList<>(COVER_MODES.get(roles.size()));
		roles.stream()
				.sorted(Comparator.comparingDouble(role -> getRoleToThreatAngle(role.getPos(), threat.getPos())))
				.forEach(role -> role.setCoverMode(coverModes.remove(0)));
	}


	private void assignCompanions(final List<CenterBackRole> roles)
	{
		Set<BotID> companions = roles.stream()
				.map(ARole::getBotID)
				.collect(Collectors.toSet());
		roles.forEach(cbr -> cbr.setCompanions(companions));
	}
}
