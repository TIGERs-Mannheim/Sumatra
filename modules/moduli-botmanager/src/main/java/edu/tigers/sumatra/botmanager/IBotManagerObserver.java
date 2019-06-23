/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.ACommand;


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
	
	
	/**
	 * A new bot command has arrived
	 * 
	 * @param bot the bot
	 * @param command the bot command
	 */
	default void onIncomingBotCommand(final ABot bot, final ACommand command)
	{
	}
}
