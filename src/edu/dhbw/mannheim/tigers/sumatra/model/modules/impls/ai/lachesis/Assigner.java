/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * Simply assigns given bots to given roles.
 * 
 * @author Gero
 */
public class Assigner
{
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(Assigner.class.getName());
	
	
	/**
	 * 
	 */
	public Assigner()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param bot
	 * @param assignees
	 * @param role
	 * @param frame
	 */
	public void assign(final TrackedTigerBot bot, final Collection<TrackedTigerBot> assignees, final ARole role,
			final AIInfoFrame frame)
	{
		doAssign(bot, role, frame);
		assignees.remove(bot);
	}
	
	
	/**
	 * @param bot
	 * @param assigneesIt
	 * @param role
	 * @param frame
	 */
	public void assign(final TrackedTigerBot bot, final Iterator<TrackedTigerBot> assigneesIt, final ARole role,
			final AIInfoFrame frame)
	{
		doAssign(bot, role, frame);
		assigneesIt.remove();
	}
	
	
	private void doAssign(TrackedTigerBot bot, ARole role, AIInfoFrame frame)
	{
		// Safety check
		if (role.hasBeenAssigned())
		{
			log.error("Error while role-assigning: Tried to re-assign already assigned roles! Qutting.");
			return;
		}
		
		// Assign
		role.assignBotID(bot.getId());
		frame.putAssignedRole(role);
	}
}
