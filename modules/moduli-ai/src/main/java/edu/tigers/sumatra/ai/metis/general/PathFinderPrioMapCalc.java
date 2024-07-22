/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


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
		pathFinderPrioMap.getMap().forEach(this::drawPriority);
	}


	private void drawPriority(BotID botId, Integer priority)
	{
		ITrackedBot bot = getWFrame().getBot(botId);
		if (bot == null)
		{
			return;
		}
		IVector2 pos = bot.getPos();
		getShapes(EAiShapesLayer.AI_PATH_FINDER_PRIORITIES).add(
				new DrawableAnnotation(pos, String.valueOf(priority))
						.withCenterHorizontally(true)
						.withOffset(Vector2.fromY(120))
		);
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

		var activeBallPlacers = desiredBotsMap.get().getOrDefault(EPlay.BALL_PLACEMENT, Collections.emptySet());
		var activeDefenders = desiredBotsMap.get().getOrDefault(EPlay.DEFENSIVE, Collections.emptySet());
		var activeOffenders = desiredBotsMap.get().getOrDefault(EPlay.OFFENSIVE, Collections.emptySet());
		var activeKeepers = desiredBotsMap.get().getOrDefault(EPlay.KEEPER, Collections.emptySet());
		var activeSupporters = desiredBotsMap.get().getOrDefault(EPlay.SUPPORT, Collections.emptySet());
		var remainingDefenders = new HashSet<>(activeDefenders);
		var remainingOffenders = new HashSet<>(activeOffenders);

		prio = updatePrio(map, prio, activeBallPlacers);

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
		prio = updatePrio(map, prio, activeKeepers);
		prio = updatePrio(map, prio, remainingOffenders);
		prio = updatePrio(map, prio, remainingDefenders);
		updatePrio(map, prio, activeSupporters);

		return map;
	}


	private int updatePrio(final PathFinderPrioMap map, int prio, final Set<BotID> bots)
	{
		var nextPrio = prio;
		var sortedBotIds = bots.stream()
				.sorted(Comparator.comparing(this::distanceToDest).thenComparing(BotID::compareTo))
				.toList();
		for (BotID botId : sortedBotIds)
		{
			map.setPriority(botId, nextPrio--);
		}
		return nextPrio;
	}


	private double distanceToDest(BotID id)
	{
		var bot = getWFrame().getBot(id);
		return bot.getDestinationPose()
				.map(Pose::getPos)
				.map(p -> p.distanceToSqr(bot.getPos()))
				.orElse(Double.POSITIVE_INFINITY);
	}
}
