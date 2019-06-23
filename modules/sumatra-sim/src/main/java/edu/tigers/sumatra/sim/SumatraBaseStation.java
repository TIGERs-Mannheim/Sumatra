/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 2, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sim;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.basestation.ABaseStation;
import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SumatraBaseStation extends ABaseStation
{
	@Configurable(defValue = "6")
	private static int numBots = 6;
	
	
	static
	{
		ConfigRegistration.registerClass("botmgr", SumatraBaseStation.class);
	}
	
	
	/**
	 * 
	 */
	public SumatraBaseStation()
	{
		super(EBotType.SUMATRA);
	}
	
	
	@Override
	public void enqueueCommand(final BotID id, final ACommand cmd)
	{
	}
	
	
	@Override
	public ENetworkState getNetState()
	{
		return ENetworkState.ONLINE;
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
	protected void onConnect()
	{
		for (int i = 0; i < numBots; i++)
		{
			notifyBotOnline(new SumatraBot(BotID.createBotId(i, ETeamColor.YELLOW), this));
			notifyBotOnline(new SumatraBot(BotID.createBotId(i, ETeamColor.BLUE), this));
		}
	}
	
	
	@Override
	protected void onDisconnect()
	{
		for (int i = 0; i < numBots; i++)
		{
			notifyBotOffline(BotID.createBotId(i, ETeamColor.YELLOW));
			notifyBotOffline(BotID.createBotId(i, ETeamColor.BLUE));
		}
	}
	
	
	@Override
	public String getName()
	{
		return "SUMATRA BS";
	}
}
