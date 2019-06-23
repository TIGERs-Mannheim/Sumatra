/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.basestation;

import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.ids.BotID;


/**
 * Delegate TigerBotV2 commands to base station connection.
 * 
 * @author AndreR
 */
public interface IBaseStation extends IConfigObserver
{
	/**
	 * Send a bot command via base station.
	 * 
	 * @param id
	 * @param cmd
	 */
	void enqueueCommand(BotID id, ACommand cmd);
	
	
	/**
	 * Send a command to base station.
	 * 
	 * @param cmd
	 */
	void enqueueCommand(final ACommand cmd);
	
	
	/**
	 * Connect to BS
	 */
	void connect();
	
	
	/**
	 * Disconnect from BS
	 */
	void disconnect();
	
	
	/**
	 * Add observer.
	 * 
	 * @param observer
	 */
	void addObserver(IBaseStationObserver observer);
	
	
	/**
	 * Remove observer.
	 * 
	 * @param observer
	 */
	void removeObserver(IBaseStationObserver observer);
	
	
	/**
	 * @return
	 */
	ENetworkState getNetState();
	
	
	/**
	 * @param numPings
	 * @param payload
	 */
	void startPing(int numPings, int payload);
	
	
	/**
	 * end of ping
	 */
	void stopPing();
	
	
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
	
	
	/**
	 * @return
	 */
	String getName();
}
