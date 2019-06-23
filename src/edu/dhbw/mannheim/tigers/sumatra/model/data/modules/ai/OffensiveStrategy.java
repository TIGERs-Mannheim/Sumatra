/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 30, 2014
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Data holder for OffensiveStrategyCalc elements for persisting them.
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
@Persistent(version = 2)
public class OffensiveStrategy
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Defines the offensiveStrategy
	 */
	public enum EOffensiveStrategy
	{
		/**  */
		GET,
		/**  */
		KICK,
		/**  */
		STOP,
		/**  */
		INTERCEPT,
		/**  */
		REDIRECT_CATCH_SPECIAL_MOVE,
		/**  */
		DELAY,
		/**  */
		SUPPORTIVE_ATTACKER,
		/** */
		@Deprecated
		PLAY,
		/**  */
		@Deprecated
		REDIRECT,
		/**  */
		@Deprecated
		CATCH,
		/**  */
		@Deprecated
		SPECIAL_COMMAND,
		/**  */
		@Deprecated
		CATCH_REDIRECT,
		/**  */
		@Deprecated
		SPECIAL_MOVE,
	}
	
	private int										minNumberOfBots							= 0;
	private int										maxNumberOfBots							= 1;
	private List<BotID>							desiredBots									= new ArrayList<BotID>();
	private Map<BotID, EOffensiveStrategy>	currentOffensivePlayConfiguration	= new HashMap<BotID, EOffensiveStrategy>();
	private List<EOffensiveStrategy>			unassignedStrategies						= new ArrayList<EOffensiveStrategy>();
	private List<SpecialMoveCommand>			specialMoveCommands						= new ArrayList<SpecialMoveCommand>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Stores lots of Information for offensive strategies
	 */
	public OffensiveStrategy()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
	public List<BotID> getDesiredBots()
	{
		return desiredBots;
	}
	
	
	/**
	 * @param desiredBots the desiredBots to set
	 */
	public void setDesiredBots(final List<BotID> desiredBots)
	{
		this.desiredBots = desiredBots;
	}
	
	
	/**
	 * @return the currentOffensivePlayConfiguration
	 */
	public Map<BotID, EOffensiveStrategy> getCurrentOffensivePlayConfiguration()
	{
		return currentOffensivePlayConfiguration;
	}
	
	
	/**
	 * @param currentOffensivePlayConfiguration the currentOffensivePlayConfiguration to set
	 */
	public void setCurrentOffensivePlayConfiguration(
			final Map<BotID, EOffensiveStrategy> currentOffensivePlayConfiguration)
	{
		this.currentOffensivePlayConfiguration = currentOffensivePlayConfiguration;
	}
	
	
	/**
	 * @return the helperDestinations
	 */
	public List<SpecialMoveCommand> getSpecialMoveCommands()
	{
		return specialMoveCommands;
	}
	
	
	/**
	 * @param specialMoveCommands the helperDestinations to set
	 */
	public void setSpecialMoveCommands(final List<SpecialMoveCommand> specialMoveCommands)
	{
		this.specialMoveCommands = specialMoveCommands;
	}
	
	
	/**
	 * @return the unassignedStrategies
	 */
	public List<EOffensiveStrategy> getUnassignedStrategies()
	{
		return unassignedStrategies;
	}
	
	
	/**
	 * @param unassignedStrategies the helperDestinations to set
	 */
	public void setUnassignedStrategies(final List<EOffensiveStrategy> unassignedStrategies)
	{
		this.unassignedStrategies = unassignedStrategies;
	}
	
}