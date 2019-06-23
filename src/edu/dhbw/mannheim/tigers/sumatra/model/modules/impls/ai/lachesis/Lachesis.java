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
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERoleBehavior;


/**
 * This class is used to assigned the needed roles of each play to a robot.
 * (@see {@link Lachesis#assignRoles(AIInfoFrame)})
 * 
 * @author Gero, Oliver Steinbrecher <OST1988@aol.com>
 */
public class Lachesis
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log							= Logger.getLogger(Lachesis.class.getName());
	
	private final Assigner			assigner;
	
	private final IRoleAssigner	roleAssigner;
	
	private boolean					assignmentBasedOnPlays	= true;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public Lachesis()
	{
		assigner = new Assigner();
		roleAssigner = new OptimizedRoleAssigner();
	}
	
	
	/**
	 * <p>
	 * This method creates the proper preconditions (three lists of roles, separated by
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERoleBehavior}) before the
	 * {@link IRoleAssigner}-implementation handles the rest. It also catches a lot of special cases (less roles then
	 * bots, less bots then roles, etc.) for debugging purposes.
	 * </p>
	 * @param frame
	 */
	public final void assignRoles(AIInfoFrame frame)
	{
		// ##### Check preconditions
		if (frame.playStrategy.getActivePlays().isEmpty())
		{
			// No play, thus no roles to assign
			return;
		}
		
		// what bots want a role?
		final BotIDMap<TrackedTigerBot> assignees = new BotIDMap<TrackedTigerBot>(frame.worldFrame.tigerBotsAvailable);
		
		// ### Gather roles we have to assign (and handle keeper!)
		final List<ARole> allRolesToAssign = findRolesToAssign(frame, assignees);
		
		// ### Check number of roles...
		final int numRolesToAssign = allRolesToAssign.size();
		if (numRolesToAssign > assignees.size())
		{
			log.warn("There are more roles left (" + numRolesToAssign + ") then free bots (" + assignees.size() + ")!");
		}
		
		// now do the assigning according to the role behavior
		if (assignmentBasedOnPlays)
		{
			assignRolesAccordingToPlayType(frame, allRolesToAssign, assignees);
		} else
		{
			assignRolesAccordingToRoleBehavior(frame, allRolesToAssign, assignees);
		}
	}
	
	
	/**
	 * Find all roles in all plays that have not an assigned bot
	 * 
	 * @param frame
	 * @param assignees
	 * @return
	 */
	private List<ARole> findRolesToAssign(AIInfoFrame frame, BotIDMap<TrackedTigerBot> assignees)
	{
		final List<ARole> allRolesToAssign = new ArrayList<ARole>();
		final BotID keeperId = frame.worldFrame.teamProps.getKeeperId();
		// Tiger who is gonna be the keeper this frame. 'null' means 'no one'
		TrackedTigerBot keeper = null;
		// Used for debugging purposes
		boolean keeperHasAlreadyBeenHandled = false;
		
		for (final APlay play : frame.playStrategy.getActivePlays())
		{
			for (final ARole role : play.getRoles())
			{
				// roles per play
				// Check if has already been assigned:
				if (role.hasBeenAssigned())
				{
					// Reuse old role-assignment
					frame.putAssignedRole(role);
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
						log.error("More then one role pretends to be keeper, role assigning will fail!!! 1: '" + role
								+ "', 2: '" + keeper + "'");
					} else
					{
						keeper = assignees.getWithNull(keeperId);
						if (keeper != null)
						{
							// Assign keeper
							assigner.assign(keeper, assignees.values(), role, frame);
							
							keeperHasAlreadyBeenHandled = true;
							// Keeper handled properly, everything done, next role!
							continue;
						}
						// This is not good. The configured keeper-id has not been found in the ids from the WorldFrame
						log.warn("Keeper role found, bot the configured keeper-id (" + keeperId
								+ ") is not present! Will be handled as normal role...");
					}
				}
				
				// 'role' is a not a keeper here (or at least not the first!)
				allRolesToAssign.add(role);
			}
		}
		return allRolesToAssign;
	}
	
	
	/**
	 * According to behavior assign certain roles before others
	 * 
	 * @param frame
	 * @param allRoles
	 * @param assignees
	 */
	private void assignRolesAccordingToRoleBehavior(AIInfoFrame frame, List<ARole> allRoles,
			BotIDMap<TrackedTigerBot> assignees)
	{
		// Sort every role (except the (first) keeper as normal role)
		final Map<ERoleBehavior, List<ARole>> roles = new EnumMap<ERoleBehavior, List<ARole>>(ERoleBehavior.class);
		roles.put(ERoleBehavior.AGGRESSIVE, new ArrayList<ARole>());
		roles.put(ERoleBehavior.CREATIVE, new ArrayList<ARole>());
		roles.put(ERoleBehavior.DEFENSIVE, new ArrayList<ARole>());
		for (final ARole role : allRoles)
		{
			List<ARole> tmpRoles = roles.get(role.getBehavior());
			if (tmpRoles == null)
			{
				// roles with this behavior are not handled
				throw new IllegalStateException();
			}
			tmpRoles.add(role);
		}
		
		// ##### Preconditions are checked. Now the actual role-assignment is triggered
		// creative and conservative are not specially treated yet
		switch (frame.playStrategy.getMatchBehavior())
		{
			case CREATIVE:
			case AGGRESSIVE:
				roleAssigner.assignRoles(assignees.values(), roles.get(ERoleBehavior.AGGRESSIVE), frame);
				roleAssigner.assignRoles(assignees.values(), roles.get(ERoleBehavior.CREATIVE), frame);
				roleAssigner.assignRoles(assignees.values(), roles.get(ERoleBehavior.DEFENSIVE), frame);
				break;
			
			case NOT_DEFINED:
			case DEFENSIVE:
				roleAssigner.assignRoles(assignees.values(), roles.get(ERoleBehavior.DEFENSIVE), frame);
				roleAssigner.assignRoles(assignees.values(), roles.get(ERoleBehavior.AGGRESSIVE), frame);
				roleAssigner.assignRoles(assignees.values(), roles.get(ERoleBehavior.CREATIVE), frame);
				break;
			case CONSERVATIVE:
				roleAssigner.assignRoles(assignees.values(), roles.get(ERoleBehavior.AGGRESSIVE), frame);
				roleAssigner.assignRoles(assignees.values(), roles.get(ERoleBehavior.DEFENSIVE), frame);
				roleAssigner.assignRoles(assignees.values(), roles.get(ERoleBehavior.CREATIVE), frame);
				break;
			default:
				throw new IllegalStateException();
		}
	}
	
	
	private void assignRolesAccordingToPlayType(AIInfoFrame frame, List<ARole> allRoles,
			BotIDMap<TrackedTigerBot> assignees)
	{
		
		assignRolesRoleToBallDistance(frame, allRoles, assignees);
		// could not be null, this is checked in method before
		List<APlay> plays = frame.getPlayStrategy().getActivePlays();
		Collections.sort(plays);
		for (APlay play : plays)
		{
			List<ARole> roles = play.getRoles();
			List<ARole> toBeAssignedRoles = new ArrayList<ARole>(roles.size());
			for (ARole role : roles)
			{
				if (allRoles.contains(role))
				{
					toBeAssignedRoles.add(role);
				}
			}
			roleAssigner.assignRoles(assignees.values(), toBeAssignedRoles, frame);
		}
	}
	
	
	private void assignRolesRoleToBallDistance(AIInfoFrame frame, List<ARole> allRoles,
			BotIDMap<TrackedTigerBot> assignees)
	{
		ARole shortestRoleToBall = null;
		TrackedBall ball = frame.worldFrame.getBall();
		if (ball != null)
		{
			IVector2 ballPos = ball.getPos();
			float shortestDistance = Float.MAX_VALUE;
			// check which role is the nearest at the ball and assign it after the keeper.
			for (ARole role : allRoles)
			{
				float distance = GeoMath.distancePP(role.getDestination(), ballPos);
				if (distance < shortestDistance)
				{
					shortestDistance = distance;
					shortestRoleToBall = role;
				}
			}
			if (shortestRoleToBall != null)
			{
				List<ARole> roles = new ArrayList<ARole>(1);
				roles.add(shortestRoleToBall);
				roleAssigner.assignRoles(assignees.values(), roles, frame);
				allRoles.remove(shortestRoleToBall);
			}
		}
	}
}
