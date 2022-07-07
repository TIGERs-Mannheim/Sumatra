/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match.defense;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import org.apache.commons.lang.Validate;


/**
 * Data structure that stores current role with a changeable new role.
 */
public class SwitchableDefenderRole
{
	private final ARole originalRole;
	private ARole newRole;


	/**
	 * A new switchable role
	 *
	 * @param originalRole
	 */
	public SwitchableDefenderRole(final ARole originalRole)
	{
		Validate.notNull(originalRole);
		this.originalRole = originalRole;
		this.newRole = originalRole;
	}


	public ARole getOriginalRole()
	{
		return originalRole;
	}


	public ARole getNewRole()
	{
		return newRole;
	}


	public void setNewRole(final ARole newRole)
	{
		this.newRole = newRole;
	}
}
