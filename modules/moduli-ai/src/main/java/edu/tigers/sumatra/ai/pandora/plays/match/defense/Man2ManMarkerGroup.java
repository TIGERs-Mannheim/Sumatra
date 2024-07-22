/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.Man2ManMarkerRole;


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
			if (sRole.getOriginalRole().getType() != ERole.MAN_2_MAN_MARKER)
			{
				ARole newRole = new Man2ManMarkerRole();
				sRole.setNewRole(newRole);
			}
		}
		// only one role is assumed, so no further reordering required
	}


	@Override
	public void updateRoles(final AthenaAiFrame aiFrame)
	{
		super.updateRoles(aiFrame);

		getRoles().stream()
				.map(sdr -> (Man2ManMarkerRole) sdr.getNewRole())
				.forEach(r -> r.setThreat(threat));
	}
}
