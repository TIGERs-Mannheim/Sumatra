/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s):
 * Gero
 * Oliver Steinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERoleBehavior;


/**
 * This class is used to assigned the needed roles of each play to a robot.
 * 
 * @see {@link #assignRoles(AIInfoFrame, boolean)}
 * 
 * @author Gero, Oliver Steinbrecher <OST1988@aol.com>
 */
public class Lachesis
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	protected final Logger		log				= Logger.getLogger(getClass());
	
	private static final int	MAX_NUM_BOTS	= 5;
	
	private final Assigner		assigner;
	
	private final int				keeperID			= AIConfig.getGeneral().getKeeperId();
	private IRoleAssigner		roleAssigner;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	public Lachesis()
	{
		assigner = new Assigner();
		roleAssigner = new OptimizedRoleAssigner();
	}
	

	/**
	 * <p>
	 * This method creates the proper preconditions (three lists of roles, separated by {@link ERoleBehavior}) before the
	 * {@link IRoleAssigner}-implementation handles the rest. It also catches a lot of special cases (less roles then
	 * bots, less bots then roles, etc.) for debugging purposes.
	 * </p>
	 */
	public AIInfoFrame assignRoles(AIInfoFrame frame)
	{
		// ##### Check preconditions
		if (frame.playStrategy.getActivePlays().isEmpty())
		{
			log.warn("No play, thus no roles to assign!");
			return frame;
		}
		
		final WorldFrame wFrame = frame.worldFrame;
		final Map<Integer, TrackedTigerBot> assignees = new HashMap<Integer, TrackedTigerBot>(wFrame.tigerBots);
		final Map<Integer, ARole> assignments = frame.assignedRoles;
		

		// ### Gather roles we have to assign (and handle keeper!)
		final List<ARole> aggRoles = new ArrayList<ARole>(MAX_NUM_BOTS);
		final List<ARole> creRoles = new ArrayList<ARole>(MAX_NUM_BOTS);
		final List<ARole> defRoles = new ArrayList<ARole>(MAX_NUM_BOTS);
		
		TrackedTigerBot keeper = null; // Tiger who is gonna be the keeper this frame. 'null' means 'no one'
		boolean keeperHasAlreadyBeenHandled = false; // Used for debugging purposes
		
		ARole oneRole = null; // Used if there is only _one_ role to assign
		
		int i = 0;
		for (APlay play : frame.playStrategy.getActivePlays())
		{
			for (ARole role : play.getRoles())
			{
				// Check if has already been assigned:
				if (role.hasBeenAssigned())
				{
					// Reuse old role-assignment
					assignments.put(role.getBotID(), role);
					assignees.remove(role.getBotID());
					
					if (role.isKeeper())
					{
						keeperHasAlreadyBeenHandled = true;
					}
					continue;
				}
				
				
				// Keeper needs a lot of special-treatment
				if (role.isKeeper())
				{
					if (keeperHasAlreadyBeenHandled)
					{
						// Bad. But try to handle as normal role below!
						log.error("More then one role [" + role + "] pretends to be keeper, role assigning will fail!!!");
					} else
					{
						keeper = assignees.get(keeperID);
						if (keeper != null)
						{
							// Assign keeper
							assigner.assign(keeper, assignees.values(), role, assignments);
							
							keeperHasAlreadyBeenHandled = true;
							continue; // Keeper handled properly, everything done, next role!
						} else
						{
							// This is not good. The configured keeper-id has not been found in the ids from the WorldFrame
							log.warn("Keeper role found, bot the configured keeper-id (" + keeperID
									+ ") is not present! Will be handled as normal role...");
						}
					}
				}
				
				// 'role' is a not a keeper here (or at least not the first!)
				oneRole = role;
				
				// Sort every role (except the (first) keeper as normal role)
				switch (role.getBehavior())
				{
					case AGGRESSIVE:
						aggRoles.add(role);
						break;
					
					case CREATIVE:
						creRoles.add(role);
						break;
					
					case DEFENSIVE:
						defRoles.add(role);
						break;
				}
				i++;
			}
		}
		
		if (!keeperHasAlreadyBeenHandled)
		{
			log.warn("No keeper-role present in current frame!");
		}
		
		final int numRolesToAssign = i;
		final int numFreeTigers = assignees.size();
		

		// ### Check number of roles...
		if (numRolesToAssign > numFreeTigers)
		{
			log.warn("There are more roles left (" + numRolesToAssign + ") then free bots (" + numFreeTigers + ")!");
		} else if (numRolesToAssign == 0 && !keeperHasAlreadyBeenHandled)
		{
			log.warn("Play, but no roles to assign!");
			return frame;
		}
		

		// ### Check if only one bot left (remember, keeper may be assigned!), then just assign and quit...
		if (numFreeTigers == 1)
		{
			if (numRolesToAssign != 0)
			{
				TrackedTigerBot theOne = assignees.values().toArray(new TrackedTigerBot[1])[0];
				assigner.assign(theOne, assignees.values(), oneRole, assignments);
			} else
			{
				log.debug("One bot, but no role to assign!");
			}
			return frame; // Jump off...
		}
		

		// ##### Preconditions are checked. Now the actual role-assignment is triggered
		if (numFreeTigers > 0 && numRolesToAssign > 0)
		{
			switch (frame.playStrategy.getMatchBehavior())
			{
				case AGGRESSIVE:
					roleAssigner.assignRoles(assignees.values(), aggRoles, assignments, frame);
					roleAssigner.assignRoles(assignees.values(), defRoles, assignments, frame);
					roleAssigner.assignRoles(assignees.values(), creRoles, assignments, frame);
					break;
				
				case NOT_DEFINED:
				case DEFENSIVE:
					roleAssigner.assignRoles(assignees.values(), defRoles, assignments, frame);
					roleAssigner.assignRoles(assignees.values(), aggRoles, assignments, frame);
					roleAssigner.assignRoles(assignees.values(), creRoles, assignments, frame);
					break;
			}
		}
		
		return frame;
	}
}
