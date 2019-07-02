/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import edu.tigers.sumatra.ai.pandora.roles.support.ESupportBehavior;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.BreakthroughDefensive;
import edu.tigers.sumatra.ids.BotID;


/**
 * Support play manages the support roles. This are all roles that are not offensive or defensive.
 * The purpose of the play is to manage the behaviors ({@link ESupportBehavior}) for all
 * the support roles.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 *         Simon Sander <Simon.Sander@dlr.de>
 *         ChrisC
 *         Marius Messerschmidt
 */
public class SupportPlay extends APlay
{
	private static final int UNLIMITED = Integer.MAX_VALUE;
	private static Logger logger = Logger.getLogger(SupportPlay.class);
	
	/**
	 * This map contains all behaviors and their score for each SupportRole.
	 */
	private final Map<BotID, EnumMap<ESupportBehavior, Double>> viabilityMap = new HashMap<>();
	
	
	/**
	 * Default constructor
	 */
	public SupportPlay()
	{
		super(EPlay.SUPPORT);
	}
	
	
	public Map<BotID, EnumMap<ESupportBehavior, Double>> getViabilityMap()
	{
		return viabilityMap;
	}
	
	
	@Override
	public void updateBeforeRoles(final AthenaAiFrame frame)
	{
		super.updateBeforeRoles(frame);
		
		// Viability of all Supporter have to be calculated before assignment
		viabilityMap.clear();
		getRoles().forEach(r -> viabilityMap.put(r.getBotID(),
				(EnumMap<ESupportBehavior, Double>) ((SupportRole) r).calculateViabilities()));
		
		assignRoles(viabilityMap);
	}
	
	
	private int getMaxRolesForBehaviour(ESupportBehavior behavior)
	{
		switch (behavior)
		{
			case BREAKTHROUGH_DEFENSIVE:
				return BreakthroughDefensive.getMaxNumberAtPenaltyArea();
			case PENALTY_AREA_ATTACKER:
			case DIRECT_REDIRECTOR:
				return 1;
			default:
				return UNLIMITED;
		}
	}
	
	
	/**
	 * Filter function that checks if an {@link ASupportBehavior} could be assigned to a {@link SupportRole}
	 *
	 * @param viabilityMap map containing the viabilities for each behavior / botID
	 * @param botID the botID that should be checked
	 * @param behavior the behavior that should be checked
	 * @return True if behavior could be assigned to the bot
	 */
	private boolean roleCanDoBehavior(Map<BotID, EnumMap<ESupportBehavior, Double>> viabilityMap,
			BotID botID, ESupportBehavior behavior)
	{
		return viabilityMap.get(botID).get(behavior) > 0;
	}
	
	
	/**
	 * Filter function that checks if a BotID has already been handled
	 *
	 * @param alreadyHandled list of all bots that have been handled so far
	 * @param botID that should be checked
	 * @return True if bot has not been handled before
	 */
	private boolean roleIsNotHandledYet(List<BotID> alreadyHandled, BotID botID)
	{
		return !alreadyHandled.contains(botID);
	}
	
	
	/**
	 * Assign the behaviors to the Roles. If the behavior allow unlimited (-1) roles, it will just assign
	 * all Roles that
	 * - Have a viability > 0
	 * - Have not been assigned before
	 * Otherwise it will only assign the best N roles.
	 * 
	 * @param viabilityMap
	 */
	private void assignRoles(final Map<BotID, EnumMap<ESupportBehavior, Double>> viabilityMap)
	{
		List<BotID> handledBots = new ArrayList<>();
		
		for (ESupportBehavior behavior : ESupportBehavior.values())
		{
			int limit = getMaxRolesForBehaviour(behavior);
			
			viabilityMap.keySet().stream()
					.filter(botID -> roleIsNotHandledYet(handledBots, botID))
					.filter(botID -> roleCanDoBehavior(viabilityMap, botID, behavior))
					.sorted(Comparator.comparingDouble(role -> viabilityMap.get(role).get(behavior)).reversed())
					.limit(limit)
					.forEach(botID -> {
						Optional<ARole> role = getRoles().stream().filter(r -> r.getBotID() == botID).findAny();
						if (role.isPresent())
						{
							((SupportRole) role.get()).selectBehavior(behavior);
							handledBots.add(botID);
						} else
						{
							logger.error("Could not assign SupportBehavior " + behavior.name() + " to bot " + botID.toString()
									+ ": Bot is not known to the play");
						}
					});
		}
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		return new SupportRole();
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
}
