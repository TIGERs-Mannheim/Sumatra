/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.bots;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.ICommandSink;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.tigers.sumatra.botmanager.data.MatchCommand;
import edu.tigers.sumatra.ids.BotID;


/**
 * A special robot type that looks like a TIGERs bot.
 * All its incoming and outgoing commands are coming from a gamelog created by Sumatra.
 */
public class GameLogBot extends CommandBasedBot
{
	public GameLogBot(final BotID id, final ICommandSink commandSink)
	{
		super(EBotType.TIGERS, id, commandSink);
	}


	@Override
	public void sendMatchCommand(MatchCommand matchCommand)
	{
		// This does nothing. When using a gamelog all outputs from skill system are ignored.
		// GameLogBotManager will set and send MatchCommand directly from gamelog.
	}

	public void processOutgoingCommand(ACommand cmd)
	{
		if(cmd.getType() == ECommand.CMD_SYSTEM_MATCH_CTRL)
			lastSentMatchCommand = getMatchCommand((TigerSystemMatchCtrl)cmd);

		sendCommand(cmd);
	}

	private MatchCommand getMatchCommand(TigerSystemMatchCtrl ctrl)
	{
		MatchCommand cmd = new MatchCommand();
		cmd.setMultimediaControl(ctrl.getMultimediaControl());
		cmd.setSkill(ctrl.getSkill());

		return cmd;
	}
}
