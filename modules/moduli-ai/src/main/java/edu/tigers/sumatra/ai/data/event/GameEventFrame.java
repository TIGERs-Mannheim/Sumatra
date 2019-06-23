/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 30, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.tigers.sumatra.ids.BotID;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class GameEventFrame
{
	private Map<BotID, List<BotID>> involvedBots = new HashMap<BotID, List<BotID>>();
	
	
	/**
	 * Empty constructor with no parameters
	 */
	public GameEventFrame()
	{
		
	}
	
	
	/**
	 * Constructs an instance that is responsible for a single bot
	 * 
	 * @param singleBot The single bot to be involved
	 */
	public GameEventFrame(final BotID singleBot)
	{
		putSingleBot(singleBot);
	}
	
	
	/**
	 * This is the constructor used for initialization with two bots
	 * 
	 * @param mainBot
	 * @param secondaryBot
	 */
	public GameEventFrame(final BotID mainBot, final BotID secondaryBot)
	{
		putBotPair(mainBot, secondaryBot);
	}
	
	
	/**
	 * Gets the involved bots for the
	 * 
	 * @param bot
	 * @return
	 */
	public List<BotID> getinvolvedBotsForBot(final BotID bot)
	{
		return involvedBots.get(bot);
	}
	
	
	/**
	 * @return
	 */
	public Set<BotID> getMappedBots()
	{
		return involvedBots.keySet();
	}
	
	
	/**
	 * @param mainBot
	 * @param additionalBots
	 */
	public void putInvolvedBots(final BotID mainBot, final List<BotID> additionalBots)
	{
		involvedBots.put(mainBot, additionalBots);
	}
	
	
	/**
	 * @param bot
	 */
	public void putSingleBot(final BotID bot)
	{
		involvedBots.put(bot, null);
	}
	
	
	/**
	 * Is used to add a pair of bots to this game event frame
	 * 
	 * @param mainBot The bot that is mainly responsible
	 * @param secondaryBot Te bot that is the passive participant or something like this
	 */
	public void putBotPair(final BotID mainBot, final BotID secondaryBot)
	{
		List<BotID> tempList = new ArrayList<BotID>();
		tempList.add(secondaryBot);
		
		putInvolvedBots(mainBot, tempList);
	}
	
	
	/**
	 * Returns the set of entries
	 * 
	 * @return The contained set of entries mapping bots with involved bots
	 */
	public Set<Entry<BotID, List<BotID>>> getEntrySet()
	{
		return involvedBots.entrySet();
	}
	
	
	/**
	 * Indicates if this event has happened.
	 * 
	 * @return Indicator if event happened
	 */
	public boolean hasHappened()
	{
		return !involvedBots.isEmpty();
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((involvedBots == null) ? 0 : involvedBots.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		GameEventFrame other = (GameEventFrame) obj;
		if (involvedBots == null)
		{
			if (other.involvedBots != null)
			{
				return false;
			}
		} else if (!involvedBots.equals(other.involvedBots))
		{
			return false;
		}
		return true;
	}
}
