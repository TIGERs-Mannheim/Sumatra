/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 8, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj;

import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PathFinderPrioMap
{
	private final Map<BotID, Integer> map = new HashMap<>();
	
	
	/**
	 * 
	 */
	private PathFinderPrioMap()
	{
	}
	
	
	/**
	 * Other team is preferred (we circumvent other bots).
	 * Higher botIds are preferred
	 * 
	 * @param myTeam
	 * @return
	 */
	public static PathFinderPrioMap byBotId(final ETeamColor myTeam)
	{
		PathFinderPrioMap map = new PathFinderPrioMap();
		for (BotID botId : BotID.getAll(myTeam))
		{
			map.map.put(botId, botId.getNumber());
		}
		// for (BotID botId : BotID.getAll(myTeam.opposite()))
		// {
		// map.map.put(botId, botId.getNumber() + 100);
		// }
		return map;
	}
	
	
	/**
	 * No other bot is preferred over botId
	 * 
	 * @param botId
	 * @return
	 */
	public static PathFinderPrioMap onlyMe(final BotID botId)
	{
		PathFinderPrioMap map = new PathFinderPrioMap();
		map.map.put(botId, 1);
		return map;
	}
	
	
	/**
	 * No bot is preferred
	 * 
	 * @return
	 */
	public static PathFinderPrioMap empty()
	{
		return new PathFinderPrioMap();
	}
	
	
	/**
	 * @param botId
	 * @param prio
	 */
	public void setPriority(final BotID botId, final int prio)
	{
		map.put(botId, prio);
	}
	
	
	/**
	 * @param botId
	 * @param otherBotId
	 * @return
	 */
	public boolean isPreferred(final BotID botId, final BotID otherBotId)
	{
		Integer prio = map.get(botId);
		if (prio == null)
		{
			return false;
		}
		Integer otherPrio = map.get(otherBotId);
		if (otherPrio == null)
		{
			return true;
		}
		return prio > otherPrio;
	}
	
	
	/**
	 * @param botId
	 * @param otherBotId
	 * @return
	 */
	public boolean isEqual(final BotID botId, final BotID otherBotId)
	{
		Integer prio = map.get(botId);
		if (prio == null)
		{
			return false;
		}
		Integer otherPrio = map.get(otherBotId);
		if (otherPrio == null)
		{
			return false;
		}
		return prio == otherPrio;
	}
}
