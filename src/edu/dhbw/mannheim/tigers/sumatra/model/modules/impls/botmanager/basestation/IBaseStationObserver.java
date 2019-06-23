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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationEthStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationWifiStats;


/**
 * BaseStation Observer.
 * 
 * @author AndreR
 */
public interface IBaseStationObserver
{
	/**
	 * New command incomming from base station (and thus from bot).
	 * 
	 * @param id
	 * @param command
	 */
	default void onIncommingBotCommand(final BotID id, final ACommand command)
	{
	}
	
	
	/**
	 * New command from base station (section BASE_STATION)
	 * 
	 * @param command
	 */
	default void onIncommingBaseStationCommand(final ACommand command)
	{
	}
	
	
	/**
	 * @param stats
	 */
	default void onNewBaseStationStats(final BaseStationStats stats)
	{
	}
	
	
	/**
	 * @param stats
	 */
	default void onNewBaseStationWifiStats(final BaseStationWifiStats stats)
	{
	}
	
	
	/**
	 * @param stats
	 */
	default void onNewBaseStationEthStats(final BaseStationEthStats stats)
	{
	}
	
	
	/**
	 * @param netState
	 */
	default void onNetworkStateChanged(final ENetworkState netState)
	{
	}
	
	
	/**
	 * @param delay
	 */
	default void onNewPingDelay(final float delay)
	{
	}
	
	
	/**
	 * @param id
	 */
	default void onBotOffline(final BotID id)
	{
	}
	
	
	/**
	 * @param id
	 */
	default void onBotOnline(final BotID id)
	{
	}
}
