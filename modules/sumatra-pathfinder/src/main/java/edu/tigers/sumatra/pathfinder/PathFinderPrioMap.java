package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * A Priority map that prioritizes bots before others in path planning
 */
@Value
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PathFinderPrioMap
{
	@Getter
	Map<BotID, Integer> map;


	/**
	 * Higher botIds are preferred.
	 *
	 * @param myTeam
	 * @return
	 */
	public static Map<BotID, Integer> mapByBotId(final ETeamColor myTeam)
	{
		Map<BotID, Integer> map = new HashMap<>();
		for (BotID botId : BotID.getAll(myTeam))
		{
			map.put(botId, botId.getNumber());
		}
		return map;
	}


	public static PathFinderPrioMap byBotId(ETeamColor myTeam)
	{
		return of(mapByBotId(myTeam));
	}


	/**
	 * No bot is preferred
	 *
	 * @return
	 */
	public static PathFinderPrioMap empty()
	{
		return new PathFinderPrioMap(Map.of());
	}


	public static PathFinderPrioMap of(final Map<BotID, Integer> map)
	{
		return new PathFinderPrioMap(Collections.unmodifiableMap(map));
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
