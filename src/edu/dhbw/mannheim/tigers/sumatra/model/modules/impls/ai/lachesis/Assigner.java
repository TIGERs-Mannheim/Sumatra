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
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;


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
	private final Logger log = Logger.getLogger(getClass());
	
	
	/**
	 * 
	 */
	Assigner()
	{
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	void assign(TrackedTigerBot bot, Collection<TrackedTigerBot> assignees, ARole role, Map<Integer, ARole> assignments)
	{
		doAssign(bot, role, assignments);
		assignees.remove(bot);
	}
	

	void assign(TrackedTigerBot bot, Iterator<TrackedTigerBot> assigneesIt, ARole role, Map<Integer, ARole> assignments)
	{
		doAssign(bot, role, assignments);
		assigneesIt.remove();
	}
	

	private void doAssign(TrackedTigerBot bot, ARole role, Map<Integer, ARole> assignments)
	{
		// Safety check
		if (role.hasBeenAssigned())
		{
			log.error("Error while role-assigning: Tried to re-assign already assigned roles!");
//			doReassign(bot, role, assignments);
			return;
		}
		
		// Assign
		role.assignBotID(bot.id);
		assignments.put(role.getBotID(), role);
	}
	
	// Re-assigning seems not to be safe enough!
//	private void doReassign(TrackedTigerBot bot, ARole role, Map<Integer, ARole> assignments)
//	{
//		log.debug("Reassigning old role " + role + " from '" + role.getBotID() + "' to '" + bot.id + "'.");
//		
//		// Remove old assignment (assumes that assignments is the Map-Object in the AIInfoFrame!)
//		if (assignments.remove(role.getBotID()) == null) {
//			log.fatal("Someone changed the implementation of Lachesis, REASSIGNMENT OF ROLE WILL FAIL!");
//		}
//		
//		// Reassign
//		RoleReassignKey key = new RoleReassignKey();
//		role.reassignBotID(bot.id, key);
//		assignments.put(role.getBotID(), role);
//	}
//	
//	
//	/**
//	 * Used for authentication purposes in {@link ARole#reassignBotID(int, RoleReassignKey)}
//	 * 
//	 * @author Gero
//	 */
//	public class RoleReassignKey{
//		private RoleReassignKey()
//		{}
//	}
}
