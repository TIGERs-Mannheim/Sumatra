/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.PassDisruptionRole;


public class PassDisruptionGroup extends ADefenseGroup
{
	@Override
	public void assignRoles()
	{
		for (SwitchableDefenderRole sRole : getRoles())
		{
			if (sRole.getOriginalRole().getType() != ERole.PASS_DISRUPTION_DEFENDER)
			{
				ARole newRole = new PassDisruptionRole();
				sRole.setNewRole(newRole);
			}
		}
		// only one role is assumed, so no further reordering required
	}
}
