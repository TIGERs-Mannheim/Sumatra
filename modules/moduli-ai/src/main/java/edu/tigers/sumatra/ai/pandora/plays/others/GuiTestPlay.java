/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.11.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.others;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.RoleFactory;


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
	 * Default
	 */
	public GuiTestPlay()
	{
		super(EPlay.GUI_TEST);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
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
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		if (roleToBeAdded != null)
		{
			ARole role = roleToBeAdded;
			lastAddedRoleType = role.getType();
			return role;
		}
		log.warn(
				"Could not add requested role. Creating new default instance. Your custom parameters will not be used! You may need to set a fixed botID.");
		try
		{
			return RoleFactory.createDefaultRole(lastAddedRoleType);
		} catch (NotCreateableException err)
		{
			log.error("Could not create role " + lastAddedRoleType, err);
		}
		return null;
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
}
