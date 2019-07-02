/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


public abstract class ADesiredBotCalc extends ACalculator
{
	protected EPlay play;
	
	
	protected ADesiredBotCalc(EPlay play)
	{
		super();
		this.play = play;
	}
	
	
	/**
	 * Returns a list of available bots not already assigned to another play
	 *
	 * @return List of bot ids
	 */
	protected Set<BotID> getUnassignedBots()
	{
		return getAiFrame().getWorldFrame().getTigerBotsAvailable().keySet().stream()
				.filter(bot -> getNewTacticalField().getDesiredBotMap().values().stream()
						.noneMatch(set -> set.contains(bot)))
				.collect(Collectors.toSet());
	}
	
	
	/**
	 * Get all bots already assigned to some play
	 *
	 * @return Set of BotIDs
	 */
	protected Set<BotID> getAlreadyAssignedBots()
	{
		return getNewTacticalField().getDesiredBotMap().values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}
	
	
	/**
	 * @param botID
	 * @return true, if the bot id is not yet assigned and available
	 */
	protected boolean isAssignable(BotID botID)
	{
		return getUnassignedBots().stream().anyMatch(b -> b == botID);
	}
	
	
	/**
	 * Add desired bots to the given play
	 *
	 * @param play The play to add desired bots to
	 * @param botsToAdd A set of BotIDs
	 */
	protected void addDesiredBots(EPlay play, Set<BotID> botsToAdd)
	{
		getNewTacticalField().addDesiredBots(play, botsToAdd);
	}
	
	
	/**
	 * Add desired bots to this calculator's play
	 *
	 * @param botsToAdd A set of BotIDs
	 */
	protected void addDesiredBots(Set<BotID> botsToAdd)
	{
		addDesiredBots(play, botsToAdd);
	}
	
}
