/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.basestation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ABaseStation implements IBaseStation, IConfigObserver
{
	protected final List<IBaseStationObserver>	observers	= new CopyOnWriteArrayList<>();
	private final EBotType								botType;
	
	
	/**
	 * 
	 */
	protected ABaseStation(final EBotType botType)
	{
		this.botType = botType;
	}
	
	
	@Override
	public void addObserver(final IBaseStationObserver observer)
	{
		observers.add(observer);
	}
	
	
	@Override
	public void removeObserver(final IBaseStationObserver observer)
	{
		observers.remove(observer);
	}
	
	
	protected void notifyNetworkStateChanged(final ENetworkState netState)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onNetworkStateChanged(netState);
		}
	}
	
	
	/**
	 * @param id
	 * @param cmd
	 */
	protected void notifyIncommingBotCommand(final BotID id, final ACommand cmd)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onIncomingBotCommand(id, cmd);
		}
	}
	
	
	/**
	 * @param cmd
	 */
	protected void notifyIncommingBaseStationCommand(final ACommand cmd)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onIncommingBaseStationCommand(cmd);
		}
	}
	
	
	protected void notifyNewBaseStationWifiStats(final BaseStationWifiStats stats)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onNewBaseStationWifiStats(stats);
		}
	}
	
	
	protected void notifyNewBaseStationEthStats(final BaseStationEthStats stats)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onNewBaseStationEthStats(stats);
		}
	}
	
	
	protected void notifyNewPingDelay(final double delay)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onNewPingDelay(delay);
		}
	}
	
	
	protected void notifyBotOffline(final BotID id)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onBotOffline(id);
		}
	}
	
	
	protected void notifyBotOnline(final ABot bot)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onBotOnline(bot);
		}
	}
	
	
	protected void notifyNewMatchFeedback(final BotID botId, final TigerSystemMatchFeedback feedback)
	{
		for (IBaseStationObserver observer : observers)
		{
			observer.onNewMatchFeedback(botId, feedback);
		}
	}
	
	
	@Override
	public void startPing(final int numPings, final int payload)
	{
	}
	
	
	@Override
	public void stopPing()
	{
	}
	
	
	@Override
	public void enqueueCommand(final ACommand cmd)
	{
		// empty
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		reconnect();
	}
	
	
	private void reconnect()
	{
		disconnect();
		connect();
	}
	
	
	@Override
	public final void connect()
	{
		onConnect();
	}
	
	
	protected abstract void onConnect();
	
	
	protected abstract void onDisconnect();
	
	
	@Override
	public final void disconnect()
	{
		onDisconnect();
	}
	
	
	/**
	 * @return the botType
	 */
	public final EBotType getBotType()
	{
		return botType;
	}
}
