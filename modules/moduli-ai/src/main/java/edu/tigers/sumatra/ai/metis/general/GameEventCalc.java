/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.event.EGameEvent;
import edu.tigers.sumatra.ai.data.event.GameEventManager;
import edu.tigers.sumatra.ai.data.event.GameEvents;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class GameEventCalc extends ACalculator
{
	GameEventManager	gameEventManager;
	
	
	/**
	 * 
	 */
	public GameEventCalc()
	{
		Set<EGameEvent> eventsToListenTo = new HashSet<EGameEvent>(Arrays.asList(EGameEvent.values()));
		
		gameEventManager = new GameEventManager(eventsToListenTo);
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		gameEventManager.detectEvents(newTacticalField, baseAiFrame);
		
		GameEvents gameEvents = newTacticalField.getGameEvents();
		gameEvents.storedEvents = gameEventManager.getTrackedEvents();
	}
}
