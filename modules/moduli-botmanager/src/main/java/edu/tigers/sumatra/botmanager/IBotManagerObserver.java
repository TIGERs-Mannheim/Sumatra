/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.botmanager.bots.ABot;


/**
 * Bot manager observer.
 */
public interface IBotManagerObserver
{
	/**
	 * @param bot
	 */
	default void onBotAdded(ABot bot)
	{
	}
	
	
	/**
	 * @param bot
	 */
	default void onBotRemoved(ABot bot)
	{
	}
}
