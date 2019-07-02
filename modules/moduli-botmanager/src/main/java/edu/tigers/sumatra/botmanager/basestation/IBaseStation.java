/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.basestation;

import edu.tigers.sumatra.botmanager.botskills.data.MatchCommand;
import edu.tigers.sumatra.ids.BotID;


/**
 * Delegate TigerBotV2 commands to base station connection.
 * 
 * @author AndreR
 */
public interface IBaseStation
{
	/**
	 * @param botId
	 * @param matchCommand
	 */
	void acceptMatchCommand(final BotID botId, final MatchCommand matchCommand);
	
	
	/**
	 * Connect to BS
	 */
	void connect();
	
	
	/**
	 * Disconnect from BS
	 */
	void disconnect();
	
	
	/**
	 * Reconnect to BS
	 */
	default void reconnect()
	{
		disconnect();
		connect();
	}
	
	
	/**
	 * Add the given bot
	 * 
	 * @param botID
	 */
	default void addBot(BotID botID)
	{
	}
	
	
	/**
	 * Remove the given bot
	 * 
	 * @param botID
	 */
	default void removeBot(BotID botID)
	{
	}
}
