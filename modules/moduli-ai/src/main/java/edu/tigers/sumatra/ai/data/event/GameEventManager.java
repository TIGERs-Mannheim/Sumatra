/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.event.impl.MarkingDetector;
import edu.tigers.sumatra.ai.data.event.storage.GameEventStorageBotIDOptimized;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ids.BotID;


/**
 * This class should
 * 
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class GameEventManager
{
	Map<EGameEvent, IGameEventStorage>	trackedEvents		= new HashMap<>();
	Map<EGameEvent, IGameEventDetector>	presentDetectors	= new HashMap<EGameEvent, IGameEventDetector>();
	Map<EGameEvent, Set<GameEvent>>		activeEvents		= new HashMap<>();
	
	
	/**
	 * @param eventsToBeCreated
	 */
	public GameEventManager(final Set<EGameEvent> eventsToBeCreated)
	{
		if (eventsToBeCreated != null)
		{
			for (EGameEvent eGameEvent : eventsToBeCreated)
			{
				trackedEvents.put(eGameEvent, new GameEventStorageBotIDOptimized());
			}
			
			createDetectorsForEvents(trackedEvents.keySet());
		}
	}
	
	
	/**
	 * Will create a detector for the specified events
	 * 
	 * @param eventsToCreateDetectorFor The events to create a detector for
	 */
	public void createDetectorsForEvents(final Set<EGameEvent> eventsToCreateDetectorFor)
	{
		for (EGameEvent gameEvent : eventsToCreateDetectorFor)
		{
			createDetector(gameEvent);
		}
	}
	
	
	/**
	 * This will construct a detector instance for the given event type
	 * 
	 * @param eventToCreateDetectorFor
	 */
	public void createDetector(final EGameEvent eventToCreateDetectorFor)
	{
		if (!presentDetectors.containsKey(eventToCreateDetectorFor))
		{
			switch (eventToCreateDetectorFor)
			{
			// case BALL_POSSESSION:
			// presentDetectors.put(EGameEvent.BALL_POSSESSION, new BallPossessionDetector());
			// break;
			// case TACKLE:
			// presentDetectors.put(EGameEvent.TACKLE, new TackleDetector());
			// break;
				case MARKING:
					presentDetectors.put(EGameEvent.MARKING, new MarkingDetector());
					break;
				default:
					break;
			}
		}
	}
	
	
	/**
	 * This will run all available Detectors to detect the different events
	 * 
	 * @param baseAiFrame The base AI Frame of this event
	 * @param newTacticalField The temporary tactical Field
	 */
	public void detectEvents(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		long worldFrameID = baseAiFrame.getWorldFrame().getId();
		
		for (EGameEvent eGameEvent : presentDetectors.keySet())
		{
			IGameEventDetector detector = presentDetectors.get(eGameEvent);
			
			GameEventFrame detectedBots = detector.getActiveParticipant(newTacticalField, baseAiFrame);
			
			if (detectedBots != null)
			{
				Set<GameEvent> nowActiveEvents = handleActiveDetectedEvent(eGameEvent, worldFrameID,
						detectedBots);
				
				activeEvents.put(eGameEvent, nowActiveEvents);
			} else
			{
				Set<GameEvent> temporaryEvents = activeEvents.get(eGameEvent);
				
				if (temporaryEvents != null)
				{
					temporaryEvents.clear();
				}
			}
		}
	}
	
	
	private Set<GameEvent> handleActiveDetectedEvent(final EGameEvent eGameEvent,
			final long worldFrameID,
			final GameEventFrame detectedBots)
	{
		Set<GameEvent> previouslyActiveEvents = activeEvents.get(eGameEvent);
		Set<GameEvent> returnedActiveEvents = new HashSet<>();
		
		for (BotID bot : detectedBots.getMappedBots())
		{
			List<BotID> additionalBots = detectedBots.getinvolvedBotsForBot(bot);
			
			GameEvent activeEvent = getGameEventFromBotAndAccordingBots(previouslyActiveEvents, bot, additionalBots);
			
			if (activeEvent != null)
			{
				activeEvent.signalEventActiveAtFrame(worldFrameID);
			} else
			{
				activeEvent = new GameEvent(worldFrameID, bot, additionalBots);
				trackedEvents.get(eGameEvent).addEvent(activeEvent);
			}
			
			returnedActiveEvents.add(activeEvent);
		}
		
		return returnedActiveEvents;
	}
	
	
	private GameEvent getGameEventFromBotAndAccordingBots(final Set<GameEvent> activeEvents, final BotID bot,
			final List<BotID> additionalBots)
	{
		boolean hasSameBotsInvolved = false;
		
		if (activeEvents == null)
		{
			return null;
		}
		
		for (GameEvent event : activeEvents)
		{
			BotID affectedBot = event.getAffectedBot();
			List<BotID> myAdditionalBots = event.getAdditionalBots();
			
			if ((affectedBot != null) && (myAdditionalBots != null))
			{
				hasSameBotsInvolved = affectedBot.equals(bot) && additionalBots.equals(additionalBots);
			} else if ((affectedBot != null) && (myAdditionalBots == null))
			{
				hasSameBotsInvolved = affectedBot.equals(bot) && (additionalBots == null);
			} else if ((affectedBot == null) && (myAdditionalBots != null))
			{
				hasSameBotsInvolved = myAdditionalBots.equals(additionalBots) && (bot == null);
			} else
			{
				hasSameBotsInvolved = (bot == null) && (additionalBots == null);
			}
			
			if (hasSameBotsInvolved)
			{
				return event;
			}
		}
		
		return null;
	}
	
	
	/**
	 * @return the trackedEvents
	 */
	public Map<EGameEvent, IGameEventStorage> getTrackedEvents()
	{
		return trackedEvents;
	}
	
	
	/**
	 * @return
	 */
	public Set<EGameEvent> getTrackedEventTypes()
	{
		return trackedEvents.keySet();
	}
	
	
	/**
	 * This method will return the events tracked for a specific event type
	 * 
	 * @param ballPossession
	 * @return
	 */
	public IGameEventStorage getTrackedEventsForType(final EGameEvent ballPossession)
	{
		return trackedEvents.get(ballPossession);
	}
	
}
