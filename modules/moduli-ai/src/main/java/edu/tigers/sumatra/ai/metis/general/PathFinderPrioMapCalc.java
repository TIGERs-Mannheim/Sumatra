/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class PathFinderPrioMapCalc extends ACalculator
{
	private final Supplier<Map<EPlay, Set<BotID>>> desiredBotsMap;
	private final Supplier<Set<BotID>> crucialDefender;
	private final Supplier<Set<BotID>> crucialOffender;

	@Getter
	private PathFinderPrioMap pathFinderPrioMap;


	@Override
	protected void doCalc()
	{
		pathFinderPrioMap = buildPathFinderPrioMap();
	}


	@Override
	protected void reset()
	{
		pathFinderPrioMap = PathFinderPrioMap.byBotId(getAiFrame().getTeamColor());
	}


	private PathFinderPrioMap buildPathFinderPrioMap()
	{
		PathFinderPrioMap map = PathFinderPrioMap.byBotId(getAiFrame().getTeamColor());
		int prio = 100;

		var activeDefenders = desiredBotsMap.get().getOrDefault(EPlay.DEFENSIVE, Collections.emptySet());
		var activeOffenders = desiredBotsMap.get().getOrDefault(EPlay.OFFENSIVE, Collections.emptySet());
		var activeKeepers = desiredBotsMap.get().getOrDefault(EPlay.KEEPER, Collections.emptySet());
		var activeSupporters = desiredBotsMap.get().getOrDefault(EPlay.SUPPORT, Collections.emptySet());
		var remainingDefenders = new HashSet<>(activeDefenders);
		var remainingOffenders = new HashSet<>(activeOffenders);

		for (var botId : crucialDefender.get())
		{
			if (remainingDefenders.remove(botId))
			{
				map.setPriority(botId, prio--);
			}
		}

		for (var botId : crucialOffender.get())
		{
			if (remainingOffenders.remove(botId))
			{
				map.setPriority(botId, prio--);
			}
		}
		prio = updatePrio(map, prio, remainingOffenders);

		for (var botId : activeDefenders)
		{
			if (remainingDefenders.remove(botId))
			{
				map.setPriority(botId, prio--);
			}
		}
		prio = updatePrio(map, prio, remainingDefenders);
		prio = updatePrio(map, prio, activeKeepers);
		updatePrio(map, prio, activeSupporters);

		return map;
	}


	private int updatePrio(final PathFinderPrioMap map, int prio, final Set<BotID> bots)
	{
		var nextPrio = prio;
		var sortedBotIds = bots.stream().sorted().collect(Collectors.toList());
		for (BotID botId : sortedBotIds)
		{
			map.setPriority(botId, nextPrio--);
		}
		return nextPrio;
	}
}
