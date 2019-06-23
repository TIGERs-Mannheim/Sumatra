/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 29, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.data.event.GameEvent;
import edu.tigers.sumatra.ai.data.event.IGameEventStorage;
import edu.tigers.sumatra.ai.data.statistics.calculators.StatisticData;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
@Persistent
public class GameEventStorageBotIDOptimized implements IGameEventStorage
{
	private transient Map<BotID, List<GameEvent>>	events		= new HashMap<>();
	
	private Integer											countEvents	= 0;
	
	
	/**
	 * 
	 */
	public GameEventStorageBotIDOptimized()
	{
	}
	
	
	/**
	 * Converts the GameEventStorage to a simple StatisticData.
	 * This will just take the counts of events into consideration for now.
	 * 
	 * @return The statistic data containing the counts of specific events
	 */
	@Override
	public StatisticData toStatisticData()
	{
		StatisticData data = new StatisticData(getMapWithCountsOfEvents(), countEvents);
		return data;
	}
	
	
	private Map<BotID, Integer> getMapWithCountsOfEvents()
	{
		Map<BotID, Integer> mapWithCounts = new HashMap<>();
		
		for (BotID bot : events.keySet())
		{
			int countEvents = events.get(bot).size();
			
			mapWithCounts.put(bot, countEvents);
		}
		
		return mapWithCounts;
	}
	
	
	@Override
	public List<GameEvent> getEventsForSingleBot(final BotID bot)
	{
		return events.get(bot);
	}
	
	
	@Override
	public List<GameEvent> getEventsForTeamColor(final ETeamColor teamColor)
	{
		return null;
	}
	
	
	/**
	 * This function will add an event to the stored events.
	 * It is optimized to be mapped by the BotID
	 */
	@Override
	public void addEvent(final GameEvent event)
	{
		BotID affectedBot = event.getAffectedBot();
		List<GameEvent> listForBot = events.get(affectedBot);
		
		if (listForBot == null)
		{
			listForBot = new ArrayList<GameEvent>();
			events.put(affectedBot, listForBot);
		}
		
		listForBot.add(event);
		countEvents++;
	}
	
	
	@Override
	public int getGeneralEventCount()
	{
		return countEvents;
	}
	
	
	@Override
	public List<GameEvent> getEvents()
	{
		List<GameEvent> events = this.events.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
		
		return events;
	}
	
	
	@Override
	public List<GameEvent> getActiveEventsForFrame(final long frameID)
	{
		List<GameEvent> activeEvents = new ArrayList<GameEvent>();
		
		for (GameEvent gameEvent : getEvents())
		{
			if ((gameEvent.getStartFrame() <= frameID) && (gameEvent.getEndFrame() >= frameID))
			{
				activeEvents.add(gameEvent);
			}
		}
		
		return activeEvents;
	}
}
