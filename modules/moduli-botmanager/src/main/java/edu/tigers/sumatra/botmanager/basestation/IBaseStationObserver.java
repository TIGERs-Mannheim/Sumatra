package edu.tigers.sumatra.botmanager.basestation;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.BotID;


/**
 * Observer for generic base stations
 */
public interface IBaseStationObserver
{
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
}
