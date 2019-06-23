/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
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
	private final List<ARole> addRoles = new ArrayList<>();
	private final Map<EPlay, RoleMapping> roleMapping = new EnumMap<>(EPlay.class);
	private final Map<EPlay, Boolean> useAiFlags = new EnumMap<>(EPlay.class);
	
	
	/**
	 * @param role
	 * @param botId
	 */
	public void addRole(final ARole role, final BotID botId)
	{
		addRoles.add(role);
		roleMapping.computeIfAbsent(EPlay.GUI_TEST, e -> new RoleMapping())
				.getDesiredBots().add(botId);
	}
	
	
	/**
	 * @param role
	 */
	public void removeRole(final ARole role)
	{
		roleMapping.computeIfAbsent(EPlay.GUI_TEST, e -> new RoleMapping())
				.getDesiredBots().remove(role.getBotID());
	}
	
	
	/**
	 * Clear roles
	 */
	public void clearRoles()
	{
		roleMapping.computeIfAbsent(EPlay.GUI_TEST, e -> new RoleMapping())
				.getDesiredBots().clear();
	}
	
	
	/**
	 * @return the addRoles
	 */
	public final List<ARole> getAddRoles()
	{
		return addRoles;
	}
	
	
	/**
	 * @return the roleMapping
	 */
	public final Map<EPlay, RoleMapping> getRoleMapping()
	{
		return roleMapping;
	}
	
	
	/**
	 * @return the useAiFlags
	 */
	public final Map<EPlay, Boolean> getUseAiFlags()
	{
		return useAiFlags;
	}
}
