/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event;

import junit.framework.Assert;

import org.junit.Test;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class GameEventTest
{
	
	/**
	 * This will check the correct initialization in the game event constructor
	 */
	@Test
	public void shouldGetAnEventAtStart5AndEnd5ForOneFrameLongEventStartingAtFrame5()
	{
		GameEvent actualEvent = new GameEvent(5);
		
		Assert.assertEquals(5, actualEvent.getStartFrame());
		Assert.assertEquals(5, actualEvent.getEndFrame());
		Assert.assertEquals(1, actualEvent.getDuration());
	}
	
	
	/**
	 * This will check if the event counter is counting correctly
	 */
	@Test
	public void shouldGetStartOf0_EndOf2AndDurationOf3For3FramesLongEvent()
	{
		GameEvent actualGameEvent = new GameEvent(0);
		actualGameEvent.signalEventActiveAtFrame(1);
		actualGameEvent.signalEventActiveAtFrame(2);
		
		Assert.assertEquals(0, actualGameEvent.getStartFrame());
		Assert.assertEquals(2, actualGameEvent.getEndFrame());
		Assert.assertEquals(3, actualGameEvent.getDuration());
	}
	
	
	/**
	 * This will check the ending functionality of events.
	 * An event ended if it got a smaller id than the endFrame count.
	 */
	@Test
	public void shouldGetStartOf5_EndOf7For3FramesLongEventStartingAt5AndAnActiveStatusOfInactiveForEndedEvent()
	{
		GameEvent gameEvent = new GameEvent(5);
		gameEvent.signalEventActiveAtFrame(6);
		gameEvent.signalEventActiveAtFrame(7);
		gameEvent.signalEventActiveAtFrame(0);
		
		Assert.assertEquals(5, gameEvent.getStartFrame());
		Assert.assertEquals(7, gameEvent.getEndFrame());
		Assert.assertEquals(3, gameEvent.getDuration());
	}
}
