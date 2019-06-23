/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.04.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;


/**
 * BaseStation Observer.
 * 
 * @author AndreR
 * 
 */
public interface IBaseStationObserver
{
	/**
	 * New command incomming from base station (and thus from bot).
	 * 
	 * @param id
	 * @param command
	 */
	void onIncommingBotCommand(BotID id, ACommand command);
	
	
	/**
	 * New command from base station (section BASE_STATION)
	 * 
	 * @param command
	 */
	void onIncommingBaseStationCommand(ACommand command);
	
	
	/**
	 * 
	 * @param stats
	 */
	void onNewBaseStationStats(BaseStationStats stats);
	
	
	/**
	 * 
	 * @param netState
	 */
	void onNetworkStateChanged(ENetworkState netState);
	
	
	/**
	 * 
	 * @param delay
	 */
	void onNewPingDelay(float delay);
	
	
	/**
	 * @param id
	 */
	void onBotOffline(BotID id);
}
