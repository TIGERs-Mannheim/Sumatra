/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.others;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;


/**
 * This play is the role-container for any {@link ARole} selected by the GUI
 */
public class GuiTestPlay extends APlay
{
	private Set<ARole> roleToBeAdded = new LinkedHashSet<>();
	
	
	/**
	 * Default
	 */
	public GuiTestPlay()
	{
		super(EPlay.GUI_TEST);
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		roleToBeAdded.clear();
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		if (roleToBeAdded.isEmpty())
		{
			return new MoveRole();
		}
		return roleToBeAdded.iterator().next();
	}
	
	
	/**
	 * Add a role to be added by this play
	 * 
	 * @param role
	 */
	public void addRoleToBeAdded(final ARole role)
	{
		roleToBeAdded.add(role);
	}
}
