/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.ManToManMarkerRole;
import edu.tigers.sumatra.ids.AObjectID;


/**
 * Manage a single Man2ManMarker
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Man2ManMarkerGroup extends ADefenseGroup
{
	private final IDefenseThreat threat;
	
	
	/**
	 * @param defendingId an identifying id
	 * @param threat the threat to protect
	 */
	public Man2ManMarkerGroup(final AObjectID defendingId, final IDefenseThreat threat)
	{
		super(defendingId);
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
}
