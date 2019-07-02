/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import java.util.List;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.ManToManMarkerRole;


/**
 * Manage a single Man2ManMarker
 */
public class Man2ManMarkerGroup extends ADefenseGroup
{
	private final IDefenseThreat threat;


	/**
	 * @param threat the threat to protect
	 */
	public Man2ManMarkerGroup(final IDefenseThreat threat)
	{
		this.threat = threat;
	}


	@Override
	public void assignRoles()
	{
		for (SwitchableDefenderRole sRole : getRoles())
		{
			if (sRole.getOriginalRole().getType() != ERole.MAN_TO_MAN_MARKER)
			{
				ARole newRole = new ManToManMarkerRole(threat);
				sRole.setNewRole(newRole);
			}
		}
		// only one role is assumed, so no further reordering required
	}


	@Override
	public void updateRoles(final AthenaAiFrame aiFrame)
	{
		super.updateRoles(aiFrame);

		List<ManToManMarkerRole> allRoles = getRoles().stream()
				.map(sdr -> (ManToManMarkerRole) sdr.getNewRole())
				.collect(Collectors.toList());
		allRoles.forEach(r -> r.setThreat(threat));
	}
}
