/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.11.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.RoleFactory;


/**
 * This play is the role-container for any {@link ARole} selected by the GUI
 * 
 * @author Gero
 * 
 */
public class GuiTestPlay extends APlay
{
	private static final Logger	log	= Logger.getLogger(GuiTestPlay.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * allowed because this is a Pseudo-Play
	 * @param aiFrame
	 * @param rolesAndInitPos
	 */
	public GuiTestPlay(AIInfoFrame aiFrame, Map<ERole, IVector2> rolesAndInitPos)
	{
		super(aiFrame, rolesAndInitPos.size());
		setType(EPlay.GUI_TEST_PLAY);
		
		setTimeout(Long.MAX_VALUE);
		
		for (Map.Entry<ERole, IVector2> entry : rolesAndInitPos.entrySet())
		{
			try
			{
				final ARole genRole = RoleFactory.createRole(entry.getKey());
				addAggressiveRole(genRole, entry.getValue());
			} catch (final IllegalArgumentException iae)
			{
				log.warn(iae.getMessage());
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		List<ARole> completedRoles = new ArrayList<ARole>();
		for (final ARole role : getRoles())
		{
			if (role.isCompleted())
			{
				completedRoles.add(role);
			}
		}
		for (final ARole role : completedRoles)
		{
			getRoles().remove(role);
			currentFrame.removeAssignedRole(role.getBotID());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		// nothing todo
	}
}
