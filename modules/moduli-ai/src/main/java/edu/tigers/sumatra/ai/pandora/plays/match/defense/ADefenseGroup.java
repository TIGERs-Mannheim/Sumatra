/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An abstract defense group. Each group is updated in the following way:
 * <ol>
 * <li>create group</li>
 * <li>ADefenseGroup#assignRoles -> assign new roles</li>
 * <li>APlay#switchRoles -> new roles get assigned and updated</li>
 * <li>ADefenseGroup#updateRoles -> update new roles</li>
 * </ol>
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ADefenseGroup
{
	protected final List<SwitchableDefenderRole> roles = new ArrayList<>();


	public void addRole(ARole role)
	{
		roles.add(new SwitchableDefenderRole(role));
	}


	/**
	 * Create new roles, if necessary
	 */
	public abstract void assignRoles();


	/**
	 * Update roles.
	 * This is called after all new roles have been assigned and initialized
	 *
	 * @param aiFrame
	 */
	public void updateRoles(AthenaAiFrame aiFrame)
	{

	}


	public List<SwitchableDefenderRole> getRoles()
	{
		return Collections.unmodifiableList(roles);
	}
}
