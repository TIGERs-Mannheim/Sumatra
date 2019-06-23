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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * This class is used to assigned the needed roles of each play to a robot.
 * (@see {@link Lachesis#assignRoles(AthenaAiFrame)})
 * 
 * @author Gero, Oliver Steinbrecher <OST1988@aol.com>
 */
public class Lachesis
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger		log	= Logger.getLogger(Lachesis.class.getName());
	
	private final INewRoleAssigner	roleAssigner;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public Lachesis()
	{
		log.trace("Creating");
		roleAssigner = new NewRoleAssigner();
		log.trace("Created");
	}
	
	
	/**
	 * <p>
	 * Takes the roles and assigns them. It also catches a lot of special cases (less roles then bots, less bots then
	 * roles, etc.) for debugging purposes.
	 * </p>
	 * 
	 * @param frame
	 */
	public final void assignRoles(final AthenaAiFrame frame)
	{
		// ##### Check preconditions
		if (frame.getPlayStrategy().getActivePlays().isEmpty())
		{
			// No play, thus no roles to assign
			return;
		}
		
		
		// what bots want a role?
		final BotIDMap<TrackedTigerBot> assignees = new BotIDMap<TrackedTigerBot>(
				frame.getWorldFrame().tigerBotsAvailable);
		
		List<APlay> playsToAssign = new LinkedList<APlay>(frame.getPlayStrategy().getActivePlays());
		Collections.sort(playsToAssign, new APlayComparator());
		roleAssigner.assignRoles(assignees, playsToAssign, frame);
		
		for (ARole role : frame.getPlayStrategy().getActiveRoles().values())
		{
			if (!role.hasBeenAssigned())
			{
				log.warn("Role not assigned: " + role.getType());
			}
		}
	}
	
	
	/**
	 * Comparator for RoleAssigner.
	 * Sorts the Plays in assigning-order.
	 * 
	 * @author MalteJ
	 */
	private class APlayComparator implements Comparator<APlay>
	{
		
		//
		@Override
		public int compare(final APlay a, final APlay b)
		{
			if (a.getType() == EPlay.OFFENSIVE)
			{
				return -1;
			}
			if ((a.getType() == EPlay.DEFENSIVE) && (b.getType() != EPlay.OFFENSIVE))
			{
				return -1;
			}
			if ((a.getType() == EPlay.SUPPORT) && ((b.getType() != EPlay.OFFENSIVE) && (b.getType() != EPlay.DEFENSIVE)))
			{
				return -1;
			}
			// Alle anderen Fälle sind untergeordnet (alte Plays zuletzt zugewiesen werden...
			return 1;
		}
	}
}
