/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.basestation.ABaseStation;
import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SumatraBaseStation extends ABaseStation
{
	/**
	 * Default
	 */
	public SumatraBaseStation()
	{
		super(EBotType.SUMATRA);
	}
	
	
	/**
	 * @param id
	 * @param feedback
	 */
	public void notifyMatchFeedback(final BotID id, final TigerSystemMatchFeedback feedback)
	{
		notifyIncommingBotCommand(id, feedback);
		notifyNewMatchFeedback(id, feedback);
	}
	
	
	/**
	 * @param id
	 * @param cmd
	 */
	public void notifyCommand(final BotID id, final ACommand cmd)
	{
		notifyIncommingBotCommand(id, cmd);
	}
	
	
	@Override
	public void enqueueCommand(final BotID id, final ACommand cmd)
	{
		// ignore all commands
	}
	
	
	@Override
	public ENetworkState getNetState()
	{
		return ENetworkState.ONLINE;
	}
	
	
	@Override
	protected void onConnect()
	{
		for (int i = 0; i < RuleConstraints.getBotsPerTeam(); i++)
		{
			addBot(BotID.createBotId(i, ETeamColor.YELLOW));
			addBot(BotID.createBotId(i, ETeamColor.BLUE));
		}
	}
	
	
	@Override
	protected void onDisconnect()
	{
		for (int i = 0; i < RuleConstraints.getBotsPerTeam(); i++)
		{
			removeBot(BotID.createBotId(i, ETeamColor.YELLOW));
			removeBot(BotID.createBotId(i, ETeamColor.BLUE));
		}
	}
	
	
	/**
	 * Add a new bot
	 *
	 * @param botID
	 */
	@Override
	public void addBot(final BotID botID)
	{
		notifyBotOnline(new SumatraBot(botID, this));
	}
	
	
	/**
	 * Remove an existing bot
	 *
	 * @param botID
	 */
	@Override
	public void removeBot(final BotID botID)
	{
		notifyBotOffline(botID);
	}
	
	
	@Override
	public String getName()
	{
		return "SUMATRA BS";
	}
}
