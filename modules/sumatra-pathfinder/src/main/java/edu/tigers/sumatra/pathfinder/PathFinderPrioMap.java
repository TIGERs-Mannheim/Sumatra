/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * A Priority map that prioritizes bots before others in path planning
 */
public class PathFinderPrioMap
{
	@Getter
	private final Map<BotID, Integer> map = new HashMap<>();


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
	 * @return true if priority of <code>botId</code> is higher than <code>otherBotId</code>
	 */
	public boolean isPreferred(final BotID botId, final BotID otherBotId)
	{
		Integer prio = map.get(botId);
		if (prio == null)
		{
			return false;
		}
		Integer otherPrio = map.get(otherBotId);
		return otherPrio == null || prio > otherPrio;
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
		return prio.equals(otherPrio);
	}


	/**
	 * @return all contained bot ids
	 */
	public Set<BotID> getBots()
	{
		return map.keySet();
	}
}
