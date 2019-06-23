/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.04.2013
 * Author(s): AndreR
 * *********************************************************
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
	 * 
	 */
	void connect();
	
	
	/**
	 * 
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
	 * 
	 */
	void stopPing();
	
	
	/**
	 * @return
	 */
	String getName();
}
