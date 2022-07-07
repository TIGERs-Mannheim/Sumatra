/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This role assigner simply applies the set of desiredBots to the current plays
 */
@Log4j2
public class RoleAssigner
{
	public void assignRoles(final Set<APlay> activePlays, final Map<EPlay, Set<BotID>> roleMapping)
	{
		Set<BotID> assignedBotIds = new HashSet<>();
		for (APlay play : activePlays)
		{
			Set<BotID> allDesiredBots = roleMapping.getOrDefault(play.getType(), Collections.emptySet());
			List<BotID> desiredBots = new ArrayList<>(allDesiredBots);

			// remove UninitializedID Bots
			desiredBots.removeIf(AObjectID::isUninitializedID);

			// remove already assigned bots, if there are some by accident
			desiredBots.removeAll(assignedBotIds);

			removeNonDesiredBots(play, desiredBots);
			addNewDesiredBots(play, desiredBots);

			Set<BotID> postAssignedBots = play.getRoles().stream().map(ARole::getBotID).collect(Collectors.toSet());
			if (!SumatraModel.getInstance().isTournamentMode() && !postAssignedBots.equals(allDesiredBots))
			{
				log.warn("Assignment does not match desiredBots. desired: " + allDesiredBots + ", assigned: "
						+ postAssignedBots + ", roleMapping: " + roleMapping);
			}
			assignedBotIds.addAll(postAssignedBots);
		}
	}


	private void addNewDesiredBots(final APlay play, final List<BotID> desiredBots)
	{
		Set<BotID> preAssignedBots = play.getRoles().stream().map(ARole::getBotID).collect(Collectors.toSet());
		desiredBots.removeAll(preAssignedBots);
		List<ARole> newRoles = play.addRoles(desiredBots.size());
		newRoles.forEach(role -> role.assignBotID(desiredBots.remove(0)));
	}


	private void removeNonDesiredBots(final APlay play, final Collection<BotID> newBots)
	{
		List<ARole> currentRoles = new ArrayList<>(play.getRoles());
		for (ARole role : currentRoles)
		{
			if (!newBots.contains(role.getBotID()))
			{
				play.removeRole(role);
			}
		}
	}
}
