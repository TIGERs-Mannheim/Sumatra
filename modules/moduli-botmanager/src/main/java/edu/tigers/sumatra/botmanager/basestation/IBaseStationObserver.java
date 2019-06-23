/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.basestation;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.ids.BotID;


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
	
	
	/**
	 * @param id
	 */
	default void onBotOffline(final BotID id)
	{
	}
	
	
	/**
	 * @param bot
	 */
	default void onBotOnline(final ABot bot)
	{
	}
	
	
	/**
	 * @param botId
	 * @param feedback
	 */
	default void onNewMatchFeedback(final BotID botId, final TigerSystemMatchFeedback feedback)
	{
	}
}
