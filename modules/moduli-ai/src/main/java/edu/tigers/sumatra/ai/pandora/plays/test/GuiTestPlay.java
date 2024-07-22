/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.test;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;


/**
 * This play is the role-container for any {@link ARole} selected by the GUI
 */
public class GuiTestPlay extends APlay
{
	public GuiTestPlay()
	{
		super(EPlay.GUI_TEST);
	}


	@Override
	protected ARole onAddRole()
	{
		throw new IllegalStateException("Not meant to be called");
	}


	/**
	 * Add a new test role from outside.
	 *
	 * @param role
	 */
	public void addNewRole(final ARole role)
	{
		super.addRole(role);
	}
}
