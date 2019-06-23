/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 29, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event;

import java.util.List;

import edu.tigers.sumatra.ai.data.statistics.calculators.StatisticData;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public interface IGameEventStorage
{
	/**
	 * Gets the count of events stored
	 * 
	 * @return The count of events
	 */
	int getGeneralEventCount();
	
	
	/**
	 * Gets the stored events
	 * 
	 * @return The stored events
	 */
	List<GameEvent> getEvents();
	
	
	/**
	 * Adds an event to the storage
	 * 
	 * @param event The event to be added
	 */
	void addEvent(final GameEvent event);
	
	
	/**
	 * This will get events mapped to a single bot
	 * 
	 * @param bot The bot to get statistics for
	 * @return The events for a specific bot
	 */
	List<GameEvent> getEventsForSingleBot(BotID bot);
	
	
	/**
	 * This will get events mapped to a specific team color
	 * 
	 * @param teamColor The team color to get events for
	 * @return The events that happened for this Team Color
	 */
	List<GameEvent> getEventsForTeamColor(ETeamColor teamColor);
	
	
	/**
	 * Converts this Game Event Storage to a Statistic Data structure
	 * 
	 * @return The resulting Statistic Data Structure
	 */
	StatisticData toStatisticData();
	
	
	/**
	 * This will get the active events for a specified frame ID
	 * 
	 * @param frameID The wished frame ID
	 * @return A list of Game Events
	 */
	List<GameEvent> getActiveEventsForFrame(long frameID);
}
