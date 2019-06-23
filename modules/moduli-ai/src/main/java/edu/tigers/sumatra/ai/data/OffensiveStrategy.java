/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.statemachine.IEvent;


/**
 * Data holder for OffensiveStrategyCalc elements for persisting them.
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
@Persistent(version = 2)
public class OffensiveStrategy
{
	/**
	 * Defines the offensiveStrategy
	 */
	public enum EOffensiveStrategy implements IEvent
	{
		/**  */
		KICK,
		/**  */
		STOP,
		/**  */
		INTERCEPT,
		/** */
		SPECIAL_MOVE,
		/**  */
		DELAY,
		/**  */
		SUPPORTIVE_ATTACKER,
		/** */
		FREE_SKIRMISH
	}
	
	private final Set<BotID> desiredBots = new HashSet<>();
	private final Map<BotID, EOffensiveStrategy> currentOffensivePlayConfiguration = new HashMap<>();
	private final List<EOffensiveStrategy> unassignedStrategies = new ArrayList<>();
	private final List<SpecialMoveCommand> specialMoveCommands = new ArrayList<>();
	private int minNumberOfBots = 0;
	private int maxNumberOfBots = 1;
	
	
	/**
	 * @return the minNumberOfBots
	 */
	public int getMinNumberOfBots()
	{
		return minNumberOfBots;
	}
	
	
	/**
	 * @param minNumberOfBots the minNumberOfBots to set
	 */
	public void setMinNumberOfBots(final int minNumberOfBots)
	{
		this.minNumberOfBots = minNumberOfBots;
	}
	
	
	/**
	 * @return the maxNumberOfBots
	 */
	public int getMaxNumberOfBots()
	{
		return maxNumberOfBots;
	}
	
	
	/**
	 * @param maxNumberOfBots the maxNumberOfBots to set
	 */
	public void setMaxNumberOfBots(final int maxNumberOfBots)
	{
		this.maxNumberOfBots = maxNumberOfBots;
	}
	
	
	/**
	 * @return the desiredBots
	 */
	public Set<BotID> getDesiredBots()
	{
		return desiredBots;
	}
	
	
	/**
	 * @return the currentOffensivePlayConfiguration
	 */
	public Map<BotID, EOffensiveStrategy> getCurrentOffensivePlayConfiguration()
	{
		return currentOffensivePlayConfiguration;
	}
	
	
	/**
	 * @return the helperDestinations
	 */
	public List<SpecialMoveCommand> getSpecialMoveCommands()
	{
		return specialMoveCommands;
	}
	
	
	/**
	 * @return the unassignedStrategies
	 */
	public List<EOffensiveStrategy> getUnassignedStrategies()
	{
		return unassignedStrategies;
	}
}