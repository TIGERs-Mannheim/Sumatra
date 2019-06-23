/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.04.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;


/**
 * Delegate TigerBotV2 commands to base station connection.
 * 
 * @author AndreR
 */
public interface IBaseStation
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
}
