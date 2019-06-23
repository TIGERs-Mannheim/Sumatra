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

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.PlayAndRoleCount;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.ESelectionReason;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
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
	 * @param presenter
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
		getControl().addNewPlay(new PlayAndRoleCount(EPlay.GUI_TEST_PLAY, EPlay.MAX_BOTS, ESelectionReason.MANUEL));
		sendControl();
	}
	
	
	@Override
	public void addRole(ERole role)
	{
		getControl().addRole(role);
		sendControl();
	}
	
	
	@Override
	public void addRole(ERole role, BotID botId)
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
		for (APlay play : getControl().getActivePlays())
		{
			for (ARole role : play.getRoles())
			{
				role.setCompleted();
			}
		}
		getControl().clearRoles();
		sendControl();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
