/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.athena.roleassigner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.athena.IPlayStrategy;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * This role assigner simply applies the set of desiredBots to the current plays
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimpleRoleAssigner implements IRoleAssigner
{
	private static final Logger log = Logger.getLogger(SimpleRoleAssigner.class.getName());
	
	
	@Override
	public void assignRoles(final IPlayStrategy playStrategy)
	{
		List<APlay> activePlays = new ArrayList<>(playStrategy.getActivePlays());
		activePlays.sort(Comparator.comparingInt(play -> play.getType().getPrio()));
		
		Map<EPlay, RoleMapping> roleMapping = playStrategy.getRoleMapping();
		
		Set<BotID> assignedBotIds = new HashSet<>();
		for (APlay play : activePlays)
		{
			RoleMapping info = roleMapping.get(play.getType());
			if (info == null)
			{
				continue;
			}
			List<BotID> desiredBots = new ArrayList<>(info.getDesiredBots());

			//remove UninitializedID Bots
			desiredBots.removeIf(AObjectID::isUninitializedID);

			// remove already assigned bots, if there are some by accident
			desiredBots.removeAll(assignedBotIds);
			
			removeNonDesiredBots(play, desiredBots);
			addNewDesiredBots(play, desiredBots);
			
			Set<BotID> postAssignedBots = play.getRoles().stream().map(ARole::getBotID).collect(Collectors.toSet());
			if (SumatraModel.getInstance().isTestMode() &&
					!postAssignedBots.equals(new HashSet<>(info.getDesiredBots())))
			{
				log.warn("Assignment does not match desiredBots. desired: " + info.getDesiredBots() + ", assigned: "
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
