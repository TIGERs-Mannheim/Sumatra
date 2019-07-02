package edu.tigers.sumatra.botmanager.bots;

import edu.tigers.sumatra.botmanager.commands.ACommand;


public interface ITigerBotObserver
{
	void onIncomingBotCommand(final TigerBot tigerBot, final ACommand cmd);
}
