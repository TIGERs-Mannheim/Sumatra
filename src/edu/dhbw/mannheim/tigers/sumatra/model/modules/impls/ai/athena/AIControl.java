/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.RoleFinderInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * Contains information about added and removed plays and roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class AIControl
{
	private final List<APlay>						addPlays					= new LinkedList<APlay>();
	private final List<APlay>						removePlays				= new LinkedList<APlay>();
	private final List<ARole>						addRoles					= new LinkedList<ARole>();
	private final List<ARole>						removeRoles				= new LinkedList<ARole>();
	private final Map<BotID, ARole>				assignRoles				= new HashMap<BotID, ARole>();
	
	private final Map<APlay, Integer>			addRoles2Play			= new HashMap<APlay, Integer>();
	private final Map<APlay, Integer>			removeRolesFromPlay	= new HashMap<APlay, Integer>();
	
	private boolean									changed					= false;
	
	private final Map<EPlay, RoleFinderInfo>	roleFinderInfos		= new HashMap<EPlay, RoleFinderInfo>();
	private final Map<EPlay, Boolean>			roleFinderOverrides	= new HashMap<EPlay, Boolean>();
	
	
	/**
	  * 
	  */
	public AIControl()
	{
	}
	
	
	/**
	 * @param play
	 */
	public void addPlay(final APlay play)
	{
		addPlays.add(play);
		changed = true;
	}
	
	
	/**
	 * @param play
	 */
	public void removePlay(final APlay play)
	{
		removePlays.add(play);
		changed = true;
	}
	
	
	/**
	 * @param role
	 * @param botId
	 */
	public void addRole(final ARole role, final BotID botId)
	{
		addRoles.add(role);
		if (botId.isBot())
		{
			assignRoles.put(botId, role);
		}
		changed = true;
	}
	
	
	/**
	 * @param role
	 */
	public void removeRole(final ARole role)
	{
		removeRoles.add(role);
		changed = true;
	}
	
	
	/**
	 * @param play
	 * @param numRoles
	 */
	public void addRoles2Play(final APlay play, final int numRoles)
	{
		addRoles2Play.put(play, numRoles);
		changed = true;
	}
	
	
	/**
	 * @param play
	 * @param numRoles
	 */
	public void removeRolesFromPlay(final APlay play, final int numRoles)
	{
		removeRolesFromPlay.put(play, numRoles);
		changed = true;
	}
	
	
	/**
	 * @return the addPlays
	 */
	public final List<APlay> getAddPlays()
	{
		return addPlays;
	}
	
	
	/**
	 */
	public void reset()
	{
		addPlays.clear();
		removePlays.clear();
		addRoles.clear();
		removeRoles.clear();
		assignRoles.clear();
		addRoles2Play.clear();
		removeRolesFromPlay.clear();
		changed = false;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the changed
	 */
	public final boolean hasChanged()
	{
		return changed;
	}
	
	
	/**
	 * @return the removePlays
	 */
	public final List<APlay> getRemovePlays()
	{
		return removePlays;
	}
	
	
	/**
	 * @return the addRoles
	 */
	public final List<ARole> getAddRoles()
	{
		return addRoles;
	}
	
	
	/**
	 * @return the removeRoles
	 */
	public final List<ARole> getRemoveRoles()
	{
		return removeRoles;
	}
	
	
	/**
	 * @return the addRoles2Play
	 */
	public final Map<APlay, Integer> getAddRoles2Play()
	{
		return addRoles2Play;
	}
	
	
	/**
	 * @return the removeRolesFromPlay
	 */
	public final Map<APlay, Integer> getRemoveRolesFromPlay()
	{
		return removeRolesFromPlay;
	}
	
	
	/**
	 * @return the assignRoles
	 */
	public final Map<BotID, ARole> getAssignRoles()
	{
		return assignRoles;
	}
	
	
	/**
	 * @return the roleFinderInfos
	 */
	public final Map<EPlay, RoleFinderInfo> getRoleFinderInfos()
	{
		return roleFinderInfos;
	}
	
	
	/**
	 * @return the roleFinderOverrides
	 */
	public final Map<EPlay, Boolean> getRoleFinderOverrides()
	{
		return roleFinderOverrides;
	}
}
