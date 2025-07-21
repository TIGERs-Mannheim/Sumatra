/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.ERobotHealthState;
import edu.tigers.sumatra.botmanager.data.MatchCommand;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Dummy stub class for bots to prevent using null
 */
public class DummyBot extends ABot
{
	/**
	 * Default constructor.
	 */
	public DummyBot()
	{
		super(EBotType.UNKNOWN, BotID.noBot());
	}


	/**
	 * @param botId
	 */
	public DummyBot(final BotID botId)
	{
		super(EBotType.UNKNOWN, botId);
	}


	@Override
	public void sendMatchCommand(MatchCommand matchCommand)
	{
		// Cannot send anything, too dumb :/
	}


	@Override
	public ERobotHealthState getHealthState()
	{
		return ERobotHealthState.UNUSABLE;
	}


	@Override
	public EBotParamLabel getBotParamLabel()
	{
		return getBotId().getTeamColor() == ETeamColor.YELLOW
				? EBotParamLabel.SIMULATION_YELLOW
				: EBotParamLabel.SIMULATION_BLUE;
	}
}
