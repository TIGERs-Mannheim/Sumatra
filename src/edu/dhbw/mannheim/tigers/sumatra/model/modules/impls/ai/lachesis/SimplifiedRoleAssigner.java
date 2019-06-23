/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.AScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.EScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.FeatureScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.KickerEmptyScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.ScoreResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.ScoreResult.EUsefulness;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;


/**
 * RoleAssigner with less logic about plays
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimplifiedRoleAssigner implements IRoleAssigner
{
	private static final Logger	log	= Logger.getLogger(SimplifiedRoleAssigner.class.getName());
	
	/** Scores for determination of suitability of bots */
	private Map<EScore, AScore>	scores;
	private AthenaAiFrame			frame;
	
	
	/**
	 * 
	 */
	public SimplifiedRoleAssigner()
	{
		// Adding score calculators for determination of bot's suitability to a certain task.
		scores = new HashMap<EScore, AScore>();
		// if we will ever consider using the DoubleTouch score again, test it before!
		// scores.put(EScore.DOUBLE_TOUCH, new DoubleTouchScore());
		scores.put(EScore.FEATURES, new FeatureScore());
		scores.put(EScore.KICKER_EMPTY, new KickerEmptyScore());
	}
	
	
	@Override
	public void assignRoles(final BotIDMap<TrackedTigerBot> assignees, final List<APlay> plays,
			final AthenaAiFrame frame)
	{
		this.frame = frame;
		List<APlay> activePlays = new ArrayList<APlay>(plays);
		Collections.sort(activePlays, new PlayPrioComparatorAPlay());
		int numRolesAvail = assignees.size();
		final Map<EPlay, RoleFinderInfo> roleFinderInfo = copyRoleFinderInfos(frame.getTacticalField()
				.getRoleFinderInfos());
		filterRoleFinderInfos(roleFinderInfo, frame);
		Map<EPlay, Integer> numRoles = findRoleBalancing(roleFinderInfo, numRolesAvail, getEPlays(plays));
		removeVanishedBots(assignees, activePlays);
		rebalanceRoleAssigment(activePlays, numRoles, frame);
		reassignment(assignees, activePlays, roleFinderInfo, frame);
		
		Set<BotID> botsAssigned = new HashSet<BotID>();
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			for (ARole role : play.getRoles())
			{
				BotID botId = role.getBotID();
				if (!botId.isBot())
				{
					log.error("Role " + role.getType() + " has no assigned bot id!");
				}
				if (botsAssigned.contains(botId))
				{
					log.error("Bot with id " + botId.getNumber() + " already has another role assigned. Can not assign "
							+ role.getType());
					continue;
				}
				botsAssigned.add(botId);
			}
		}
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
		final Map<EPlay, RoleFinderInfo> newInfo = new HashMap<EPlay, RoleFinderInfo>();
		for (Map.Entry<EPlay, RoleFinderInfo> entry : orig.entrySet())
		{
			newInfo.put(entry.getKey(), new RoleFinderInfo(entry.getValue()));
		}
		return newInfo;
	}
	
	
	/**
	 * Find the number of roles per play
	 * 
	 * @param infos
	 * @param numRolesAvail
	 * @return
	 */
	private Map<EPlay, Integer> findRoleBalancing(final Map<EPlay, RoleFinderInfo> infos, int numRolesAvail,
			final List<EPlay> plays)
	{
		Map<EPlay, Integer> numRoles = new LinkedHashMap<EPlay, Integer>(infos.size());
		
		List<Map.Entry<EPlay, RoleFinderInfo>> infoEntries = new ArrayList<Map.Entry<EPlay, RoleFinderInfo>>();
		for (Map.Entry<EPlay, RoleFinderInfo> entry : infos.entrySet())
		{
			if (plays.contains(entry.getKey()))
			{
				infoEntries.add(entry);
			}
		}
		Collections.sort(infoEntries, new PlayPrioComparatorInfo(frame.getTacticalField().getGameBehavior()));
		
		// remove desiredBots with lower Prio
		List<BotID> desiredBots = new ArrayList<BotID>(4);
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
			int roles = howManyRolesToAdd(entry.getValue().getDesiredRoles(), numRolesAvail + numRoles.get(entry.getKey()));
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
				// log.warn("desiredRoles is larger than maxRoles: " + diff + " (" + entry.getKey() + ")");
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
	private void removeVanishedBots(final BotIDMap<TrackedTigerBot> assignees, final List<APlay> activePlays)
	{
		for (APlay play : activePlays)
		{
			List<ARole> rolesToBeDeleted = new LinkedList<ARole>();
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
	private void reassignment(final BotIDMap<TrackedTigerBot> assignees, final List<APlay> activePlays,
			final Map<EPlay, RoleFinderInfo> infos, final MetisAiFrame frame)
	{
		List<BotID> availableBots = new ArrayList<BotID>(assignees.keySet());
		
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
				desiredBots = new ArrayList<BotID>(info.getDesiredBots());
				numForceDesiredBots = info.getForceNumDesiredBots();
			}
			
			List<ARole> roles = new ArrayList<ARole>(play.getRoles());
			
			// sort so that desiredBots - unassignedBots - assignedBots
			Collections.sort(roles, new RoleAssignedComparator());
			
			List<BotID> assignedBots = new ArrayList<>(roles.size());
			for (ARole role : roles)
			{
				if (role.getBotID().isBot())
				{
					assignedBots.add(role.getBotID());
				}
			}
			
			for (ARole role : roles)
			{
				BotID preferredBotId = role.getBotID();
				// if there is an unassigned desired bot, prefer this one
				if (!desiredBots.isEmpty() && !assignedBots.contains(desiredBots.get(0)))
				{
					preferredBotId = desiredBots.get(0);
				}
				// if the preferred bot is not desired by this play, but desired by any other, request a new bot
				if (!desiredBots.contains(preferredBotId) && allDesiredBots.contains(preferredBotId))
				{
					preferredBotId = BotID.createBotId();
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
					bestId = getBestSuitedBotID(availableBots, allDesiredBots, role, preferredBotId);
				}
				desiredBots.remove(bestId);
				allDesiredBots.remove(bestId);
				availableBots.remove(bestId);
				if (!role.getBotID().equals(bestId))
				{
					// change assignment
					play.removeRole(role);
					List<ARole> newRoles = play.addRoles(1, frame);
					newRoles.get(0).assignBotID(bestId, frame);
				}
			}
		}
	}
	
	
	/**
	 * Go through the score calculators and choose the bot with the lowest cost.
	 * If the current bot is at least limited useful, it will be reused
	 * 
	 * @param availableBots
	 * @param role
	 * @param preferredBotId
	 * @return
	 */
	private BotID getBestSuitedBotID(final List<BotID> availableBots, final List<BotID> allDesiredBots,
			final ARole role, final BotID preferredBotId)
	{
		BotID chosenOne = null;
		ScoreResult bestResult = new ScoreResult(EUsefulness.BAD, Integer.MAX_VALUE);
		ScoreResult curScoreResult = new ScoreResult(EUsefulness.BAD, Integer.MAX_VALUE);
		
		// preferred bot available?
		if (preferredBotId.isBot() && availableBots.contains(preferredBotId))
		{
			TrackedTigerBot curTiger = frame.getWorldFrame().getBot(preferredBotId);
			curScoreResult = AScore.getCumulatedResult(scores.values(), curTiger, role, frame);
			if (curScoreResult.getUsefulness().getLevel() <= EUsefulness.LIMITED.getLevel())
			{
				// current bot is at least limited useful, rather keep it, it may be useful again soon
				return preferredBotId;
			}
			// preferred bot is not useful, so rather look for another bot
		}
		
		List<BotID> candidates = new ArrayList<>(availableBots);
		// as long as there are candidates that are no desired bots from other plays, only use this
		candidates.removeAll(allDesiredBots);
		if (candidates.isEmpty())
		{
			// all available bots are desired bots, so use all bots as candidates
			candidates = availableBots;
		}
		
		// find a bot with best score. If same score, first match will be returned.
		for (BotID botId : candidates)
		{
			TrackedTigerBot tiger = frame.getWorldFrame().getBot(botId);
			ScoreResult result = AScore.getCumulatedResult(scores.values(), tiger, role, frame);
			if (result.moreUsefulThan(bestResult))
			{
				bestResult = result;
				chosenOne = botId;
			}
		}
		
		if (preferredBotId.isBot() && availableBots.contains(preferredBotId)
				&& !bestResult.moreUsefulThan(curScoreResult))
		{
			// the best alternative bot is still not better than the originally requested.
			// so there is no use in not giving the role the preferred bot id!
			return preferredBotId;
		}
		
		return chosenOne;
	}
	
	
	private List<EPlay> getEPlays(final List<APlay> plays)
	{
		List<EPlay> ePlays = new ArrayList<EPlay>(plays.size());
		for (APlay play : plays)
		{
			ePlays.add(play.getType());
		}
		return ePlays;
	}
	
	
	/**
	 * Comparator for RoleAssigner.
	 * Sorts the Plays in assigning-order.
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	private class PlayPrioComparatorAPlay implements Comparator<APlay>
	{
		@Override
		public int compare(final APlay a, final APlay b)
		{
			EGameBehavior gb = frame.getTacticalField().getGameBehavior();
			return Integer.compare(a.getType().getPrio(gb), b.getType().getPrio(gb));
		}
	}
	
	/**
	 * Compare roles, starting with all unassigned roles before assigned roles
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	private static class RoleAssignedComparator implements Comparator<ARole>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= -6806508037729222647L;
		
		
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
