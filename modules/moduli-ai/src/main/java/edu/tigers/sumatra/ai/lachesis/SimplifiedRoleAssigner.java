/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.lachesis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * RoleAssigner with less logic about plays
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimplifiedRoleAssigner implements IRoleAssigner
{
	private static final Logger log = Logger.getLogger(SimplifiedRoleAssigner.class.getName());
	
	private AthenaAiFrame frame;
	
	
	@Override
	public void assignRoles(final BotIDMap<ITrackedBot> assignees, final List<APlay> plays,
			final AthenaAiFrame frame)
	{
		this.frame = frame;
		List<APlay> activePlays = new ArrayList<>(plays);
		activePlays.sort(new PlayPrioComparatorAPlay());
		int numRolesAvail = assignees.size();
		final Map<EPlay, RoleFinderInfo> roleFinderInfo = copyRoleFinderInfos(frame.getTacticalField()
				.getRoleFinderInfos());
		filterRoleFinderInfos(roleFinderInfo, frame);
		Map<EPlay, Integer> numRoles = findRoleBalancing(roleFinderInfo, numRolesAvail, getEPlays(plays));
		removeVanishedBots(assignees, activePlays);
		rebalanceRoleAssigment(activePlays, numRoles, frame);
		reassignment(assignees, activePlays, roleFinderInfo, frame);
	}
	
	
	private void filterRoleFinderInfos(final Map<EPlay, RoleFinderInfo> rfis, final AthenaAiFrame frame)
	{
		Collection<BotID> nonExistingBots = BotID.getAll();
		nonExistingBots.removeAll(frame.getWorldFrame().getTigerBotsAvailable().keySet());
		
		for (RoleFinderInfo rfi : rfis.values())
		{
			rfi.getDesiredBots().removeAll(nonExistingBots);
		}
	}
	
	
	/**
	 * Deep copy
	 * 
	 * @param orig
	 * @return
	 */
	private Map<EPlay, RoleFinderInfo> copyRoleFinderInfos(final Map<EPlay, RoleFinderInfo> orig)
	{
		final Map<EPlay, RoleFinderInfo> newInfo = new EnumMap<>(EPlay.class);
		for (Map.Entry<EPlay, RoleFinderInfo> entry : orig.entrySet())
		{
			newInfo.put(entry.getKey(), new RoleFinderInfo(entry.getValue()));
		}
		return newInfo;
	}
	
	
	private void prepareRoleFinderInfos(final Map<EPlay, RoleFinderInfo> infos,
			final List<EPlay> plays,
			final List<Map.Entry<EPlay, RoleFinderInfo>> infoEntries)
	{
		for (Map.Entry<EPlay, RoleFinderInfo> entry : infos.entrySet())
		{
			if (plays.contains(entry.getKey()))
			{
				infoEntries.add(entry);
			}
		}
		infoEntries.sort(new PlayPrioComparatorInfo());
	}
	
	
	/**
	 * Find the number of roles per play
	 * 
	 * @param infos
	 * @param totalNumRolesAvail
	 * @return
	 */
	private Map<EPlay, Integer> findRoleBalancing(final Map<EPlay, RoleFinderInfo> infos, final int totalNumRolesAvail,
			final List<EPlay> plays)
	{
		Map<EPlay, Integer> numRoles = new EnumMap<>(EPlay.class);
		int numRolesAvail = totalNumRolesAvail;
		
		List<Map.Entry<EPlay, RoleFinderInfo>> infoEntries = new ArrayList<>();
		prepareRoleFinderInfos(infos, plays, infoEntries);
		
		// remove desiredBots with lower Prio
		List<BotID> desiredBots = new ArrayList<>(4);
		for (Map.Entry<EPlay, RoleFinderInfo> entry : infoEntries)
		{
			entry.getValue().getDesiredBots().removeAll(desiredBots);
			desiredBots.addAll(entry.getValue().getDesiredBots());
		}
		
		// fill min roles
		for (Map.Entry<EPlay, RoleFinderInfo> entry : infoEntries)
		{
			int roles = howManyRolesToAdd(entry.getValue().getMinRoles(), numRolesAvail);
			numRolesAvail -= roles;
			numRoles.put(entry.getKey(), roles);
		}
		
		// fill desired roles
		for (Map.Entry<EPlay, RoleFinderInfo> entry : infoEntries)
		{
			int roles = howManyRolesToAdd(entry.getValue().getDesiredRoles(),
					numRolesAvail + numRoles.get(entry.getKey()));
			int minRoles = numRoles.get(entry.getKey());
			int diff = roles - minRoles;
			if (diff < 0)
			{
				log.warn("minRoles is larger than desiredRoles: " + roles + "-" + minRoles + "=" + diff + " for play "
						+ entry.getKey());
				continue;
			}
			numRolesAvail -= diff;
			numRoles.put(entry.getKey(), roles);
		}
		
		// fill up with max roles
		for (Map.Entry<EPlay, RoleFinderInfo> entry : infoEntries)
		{
			int roles = howManyRolesToAdd(entry.getValue().getMaxRoles(), numRolesAvail + numRoles.get(entry.getKey()));
			int diff = roles - numRoles.get(entry.getKey());
			if (diff < 0)
			{
				continue;
			}
			numRolesAvail -= diff;
			numRoles.put(entry.getKey(), roles);
		}
		
		// all assigned?
		if ((numRolesAvail != 0) && !plays.isEmpty()
				&& (frame.getPlayStrategy().getAIControlState() == EAIControlState.MATCH_MODE))
		{
			log.warn(numRolesAvail + " roles could not be assigned! \n" + infoEntries + " \n" + numRoles);
		}
		return numRoles;
	}
	
	
	/**
	 * Returns the smaller value of numRoles and numRolesAvail, but at least 0
	 * 
	 * @param numRoles
	 * @param numRolesAvail
	 * @return
	 */
	private int howManyRolesToAdd(final int numRoles, final int numRolesAvail)
	{
		return Math.max(0, Math.min(numRolesAvail, numRoles));
	}
	
	
	/**
	 * Remove roles that have a bot assigned that is not in assignees list anymore, for example because it vanished from
	 * vision
	 * 
	 * @param assignees
	 * @param activePlays
	 */
	private void removeVanishedBots(final BotIDMap<ITrackedBot> assignees, final List<APlay> activePlays)
	{
		for (APlay play : activePlays)
		{
			List<ARole> rolesToBeDeleted = new ArrayList<>();
			for (ARole role : play.getRoles())
			{
				if (role.getBotID().isBot() && !assignees.containsKey(role.getBotID()))
				{
					rolesToBeDeleted.add(role);
				}
			}
			for (ARole role : rolesToBeDeleted)
			{
				play.removeRole(role);
			}
		}
	}
	
	
	/**
	 * Apply role number changes to all plays
	 * 
	 * @param activePlays
	 * @param numRoles
	 * @param frame
	 */
	private void rebalanceRoleAssigment(final List<APlay> activePlays,
			final Map<EPlay, Integer> numRoles, final MetisAiFrame frame)
	{
		for (APlay play : activePlays)
		{
			Integer numRolesToAssign = numRoles.get(play.getType());
			if (numRolesToAssign != null)
			{
				play.removeRoles(Math.max(0, play.getRoles().size() - numRolesToAssign), frame);
				play.addRoles(Math.max(0, numRolesToAssign - play.getRoles().size()), frame);
			}
		}
	}
	
	
	/**
	 * Assign and reassign bots in all roles
	 * 
	 * @param assignees
	 * @param activePlays
	 * @param infos
	 */
	private void reassignment(final BotIDMap<ITrackedBot> assignees, final List<APlay> activePlays,
			final Map<EPlay, RoleFinderInfo> infos, final MetisAiFrame frame)
	{
		List<BotID> availableBots = new ArrayList<>(assignees.keySet());
		
		List<BotID> allDesiredBots = new ArrayList<>(6);
		for (RoleFinderInfo info : infos.values())
		{
			allDesiredBots.addAll(info.getDesiredBots());
		}
		
		for (APlay play : activePlays)
		{
			RoleFinderInfo info = infos.get(play.getType());
			int numForceDesiredBots;
			List<BotID> desiredBots;
			if (info == null)
			{
				desiredBots = Collections.emptyList();
				numForceDesiredBots = 0;
			} else
			{
				desiredBots = new ArrayList<>(info.getDesiredBots());
				numForceDesiredBots = info.getForceNumDesiredBots();
			}
			
			List<ARole> roles = new ArrayList<>(play.getRoles());
			
			// sort so that desiredBots - unassignedBots - assignedBots
			roles.sort(new RoleAssignedComparator());
			
			Map<ARole, BotID> roleChanges = new IdentityHashMap<>();
			
			for (ARole role : roles)
			{
				BotID preferredBotId = role.getBotID();
				// if there is an unassigned desired bot, prefer this one
				if (!desiredBots.isEmpty())
				{
					preferredBotId = desiredBots.get(0);
				}
				// if the preferred bot is not desired by this play, but desired by any other, request a new bot
				if (!desiredBots.contains(preferredBotId) && allDesiredBots.contains(preferredBotId))
				{
					preferredBotId = BotID.noBot();
				}
				BotID bestId;
				// if play forcably request a certain bot (keeper!), use this id.
				if ((numForceDesiredBots > 0) && !desiredBots.isEmpty())
				{
					bestId = preferredBotId;
					numForceDesiredBots--;
				} else
				{
					// based on the preferred bot, check if we can use it or get another suitable bot
					bestId = getBestSuitedBotID(availableBots, allDesiredBots, preferredBotId);
				}
				desiredBots.remove(bestId);
				allDesiredBots.remove(bestId);
				availableBots.remove(bestId);
				if (!role.getBotID().equals(bestId))
				{
					// change assignment
					roleChanges.put(role, bestId);
				}
			}
			
			// remove desiredBots from current play as they should not be considered for remaining plays
			allDesiredBots.removeAll(desiredBots);
			
			// there is a known bug: Roles may get recreated although they did not change the play.
			// here is a first draft for a fix which did not work out, as bot assignment was not consistent anymore
			// assignedBots = roles.stream().map(ARole::getBotID).filter(BotID::isBot)
			// roleChanges.values().removeAll(assignedBots)
			
			for (Map.Entry<ARole, BotID> rc : roleChanges.entrySet())
			{
				ARole role = rc.getKey();
				BotID bestId = rc.getValue();
				if (role.getBotID().isBot())
				{
					play.removeRole(role);
					List<ARole> newRoles = play.addRoles(1, frame);
					newRoles.get(0).assignBotID(bestId);
				} else
				{
					role.assignBotID(bestId);
				}
			}
		}
	}
	
	
	/**
	 * Go through the score calculators and choose the bot with the lowest cost.
	 * If the current bot is at least limited useful, it will be reused
	 * 
	 * @param availableBots
	 * @param preferredBotId
	 * @return
	 */
	private BotID getBestSuitedBotID(final List<BotID> availableBots, final List<BotID> allDesiredBots,
			final BotID preferredBotId)
	{
		// preferred bot available?
		if (preferredBotId.isBot() && availableBots.contains(preferredBotId))
		{
			return preferredBotId;
		}
		// preferred bot is not available, so look for another bot
		
		List<BotID> candidates = new ArrayList<>(availableBots);
		// as long as there are candidates that are no desired bots from other plays, only use this
		candidates.removeAll(allDesiredBots);
		if (candidates.isEmpty())
		{
			// all available bots are desired bots, so use all bots as candidates
			candidates = new ArrayList<>(allDesiredBots);
			candidates.removeIf(c -> !availableBots.contains(c));
		}
		
		return candidates.get(candidates.size() - 1);
	}
	
	
	private List<EPlay> getEPlays(final List<APlay> plays)
	{
		List<EPlay> ePlays = new ArrayList<>(plays.size());
		for (APlay play : plays)
		{
			ePlays.add(play.getType());
		}
		return ePlays;
	}
	
	
	/**
	 * Compare roles, starting with all unassigned roles before assigned roles
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	private static class RoleAssignedComparator implements Comparator<ARole>, Serializable
	{
		
		/**  */
		private static final long serialVersionUID = -6806508037729222647L;
		
		
		@Override
		public int compare(final ARole o1, final ARole o2)
		{
			if (o1.hasBeenAssigned() == o2.hasBeenAssigned())
			{
				return 0;
			}
			if (o1.hasBeenAssigned())
			{
				return -1;
			}
			return 1;
		}
	}
}
