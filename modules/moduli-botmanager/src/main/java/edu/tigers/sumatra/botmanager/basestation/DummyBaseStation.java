/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.basestation;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DummyBaseStation extends ABaseStation
{
	/**
	 * Default constructor.
	 */
	public DummyBaseStation()
	{
		super(EBotType.UNKNOWN);
	}
	
	
	@Override
	public void enqueueCommand(final BotID id, final ACommand cmd)
	{
		// Dummy
	}
	
	
	@Override
	public void onConnect()
	{
		// Dummy
	}
	
	
	@Override
	public void onDisconnect()
	{
		// Dummy
	}
	
	
	@Override
	public ENetworkState getNetState()
	{
		return ENetworkState.ONLINE;
	}
	
	
	@Override
	public String getName()
	{
		return "Dummy";
	}
}
