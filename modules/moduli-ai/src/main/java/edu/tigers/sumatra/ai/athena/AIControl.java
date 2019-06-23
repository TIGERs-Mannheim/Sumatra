/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ids.BotID;


/**
 * Contains information about added and removed plays and roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class AIControl
{
	private final List<APlay> addPlays = new LinkedList<>();
	private final List<APlay> removePlays = new LinkedList<>();
	private final List<ARole> addRoles = new LinkedList<>();
	private final List<ARole> removeRoles = new LinkedList<>();
	private final Map<BotID, ARole> assignRoles = new HashMap<>();
	
	private final Map<APlay, Integer> addRoles2Play = new HashMap<>();
	private final Map<APlay, Integer> removeRolesFromPlay = new HashMap<>();
	
	private boolean changed = false;
	
	private final Map<EPlay, RoleFinderInfo> roleFinderInfos = new EnumMap<>(EPlay.class);
	private final Map<EPlay, Boolean> roleFinderOverrides = new EnumMap<>(EPlay.class);
	
	
	/**
	 * Creates a new AIControl
	 */
	public AIControl()
	{
		// Do nothing
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
	 * Resets the AIControl
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
	public final Map<EPlay, Boolean> getRoleFinderUseAiFlags()
	{
		return roleFinderOverrides;
	}
}
