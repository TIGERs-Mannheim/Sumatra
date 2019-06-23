/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event;

import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.metis.testutils.BaseAIFrameTestUtils;
import edu.tigers.sumatra.ai.data.metis.testutils.TacticalFieldTestUtils;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import junit.framework.Assert;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class GameEventManagerTest
{
	/**
	 * This will test the creation process of the manager
	 * It will just test if the manager has now an entry of the specified event
	 */
	@Test
	public void testCreationOfManagerWithBallPossessionEvent()
	{
		Set<EGameEvent> expectedEvents = createSetWithBallPossession();
		
		GameEventManager manager = new GameEventManager(expectedEvents);
		
		Set<EGameEvent> actualEvents = manager.getTrackedEventTypes();
		
		Assert.assertEquals(expectedEvents, actualEvents);
	}
	
	
	private Set<EGameEvent> createSetWithBallPossession()
	{
		Set<EGameEvent> eventsWithBallPossession = new HashSet<EGameEvent>();
		eventsWithBallPossession.add(EGameEvent.BALL_POSSESSION);
		
		return eventsWithBallPossession;
	}
	
	
	/**
	 * Tests the creation of this set if the input is null
	 */
	@Test
	public void testCreationOfManagerWithNullInputSet()
	{
		GameEventManager manager = new GameEventManager(null);
		
		Set<EGameEvent> expectedEvents = new HashSet<>();
		
		Set<EGameEvent> actualEvents = manager.getTrackedEventTypes();
		
		Assert.assertEquals(expectedEvents, actualEvents);
	}
	
	
	/**
	 * This will test the detection of currently happening events
	 * There is a vision frame that should be processed in a way to update the ball possession event
	 */
	@Ignore
	@Test
	public void testDetectionOfEventsByManagerWithBallPossession()
	{
		Set<EGameEvent> inputEvents = createSetWithBallPossession();
		
		GameEventManager manager = new GameEventManager(inputEvents);
		
		TacticalField newTacticalField = TacticalFieldTestUtils
				.initializeTacticalFieldJustWithBallPossessionForSide(EBallPossession.WE);
		
		BaseAiFrame baseAiFrame = BaseAIFrameTestUtils.mockBaseAiFrameForWorldFrameID(5);
		
		manager.detectEvents(newTacticalField, baseAiFrame);
		
		IGameEventStorage trackedBallPossessionEvents = manager.getTrackedEventsForType(EGameEvent.BALL_POSSESSION);
		
		GameEvent expectedEvent = new GameEvent(5, BotID.createBotId(0, ETeamColor.BLUE));
		GameEvent actualEvent = trackedBallPossessionEvents.getEvents().get(0);
		
		Assert.assertEquals(expectedEvent, actualEvent);
	}
}
