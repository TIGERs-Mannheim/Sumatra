/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.11.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.RoleFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass.NotCreateableException;


/**
 * This play is the role-container for any {@link ARole} selected by the GUI
 * 
 * @author Gero
 */
public class GuiTestPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log					= Logger.getLogger(GuiTestPlay.class.getName());
	
	private ARole						roleToBeRemoved	= null;
	private ARole						roleToBeAdded		= null;
	
	private ERole						lastAddedRoleType	= ERole.MOVE;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public GuiTestPlay()
	{
		super(EPlay.GUI_TEST);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected ARole onRemoveRole()
	{
		if (roleToBeRemoved != null)
		{
			ARole role = roleToBeRemoved;
			roleToBeRemoved = null;
			return role;
		}
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		if (roleToBeAdded != null)
		{
			ARole role = roleToBeAdded;
			roleToBeAdded = null;
			lastAddedRoleType = role.getType();
			return role;
		}
		try
		{
			return RoleFactory.createDefaultRole(lastAddedRoleType);
		} catch (NotCreateableException err)
		{
			log.error("Could not create role " + lastAddedRoleType, err);
		}
		return null;
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
	}
	
	
	/**
	 * Little bit hacky... do not ask
	 * 
	 * @param role
	 */
	public void setRoleToBeRemoved(final ARole role)
	{
		roleToBeRemoved = role;
	}
	
	
	/**
	 * Little bit hacky... do not ask
	 * 
	 * @param role
	 */
	public void setRoleToBeAdded(final ARole role)
	{
		roleToBeAdded = role;
	}
	
	
	@Override
	public boolean overrideRoleAssignment()
	{
		return true;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
