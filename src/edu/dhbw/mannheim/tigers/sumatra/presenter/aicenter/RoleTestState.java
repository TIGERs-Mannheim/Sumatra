/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;

/**
 * This {@link IAICenterState} handles every input which is allowed if the AI-developer wants to test roles
 * 
 * @author Gero
 */
public class RoleTestState extends AICenterState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * @param panel
	  */
	public RoleTestState(AICenterPresenter presenter)
	{
		super(presenter);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void init()
	{
		getControl().clear();
		getControl().addPlay(EPlay.GUI_TEST_PLAY);
		sendControl();
	}
	
	
	@Override
	public void addRole(ERole role)
	{
		getControl().addRole(role);
		sendControl();
	}

	@Override
	public void addRole(ERole role, int botId)
	{
		getControl().addRole(role, botId);
		sendControl();
		
	}

	@Override
	public void removeRole(ERole role)
	{
		getControl().removeRole(role);
		sendControl();
	}

	@Override
	public void clearRoles()
	{
		getControl().clearRoles();
		sendControl();
	}


	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
