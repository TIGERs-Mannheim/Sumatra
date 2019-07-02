/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.basestation;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.communication.ENetworkState;
import edu.tigers.sumatra.ids.BotID;


/**
 * Observer for {@link TigersBaseStation}
 */
public interface ITigersBaseStationObserver
{
	/**
	 * New command incoming from base station (and thus from bot).
	 * 
	 * @param id
	 * @param command
	 */
	default void onIncomingBotCommand(final BotID id, final ACommand command)
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
	default void onNewPingDelay(final double delay)
	{
	}
}
