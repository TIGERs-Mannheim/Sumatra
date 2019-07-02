/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.basestation;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.BotID;


/**
 * Abstract base station
 */
public abstract class ABaseStation implements IBaseStation
{
	private final List<IBaseStationObserver> baseStationObservers = new ArrayList<>();
	
	
	protected void botOnline(final ABot bot)
	{
		baseStationObservers.forEach(c -> c.onBotOnline(bot));
	}
	
	
	protected void botOffline(final BotID botId)
	{
		baseStationObservers.forEach(c -> c.onBotOffline(botId));
	}
	
	
	public void addObserver(final IBaseStationObserver baseStationObserver)
	{
		baseStationObservers.add(baseStationObserver);
	}
	
	
	public void removeObserver(final IBaseStationObserver baseStationObserver)
	{
		baseStationObservers.remove(baseStationObserver);
	}
}
