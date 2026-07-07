package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.botmanager.commands.ACommand;


public interface ICommandSink
{
	void sendCommand(ACommand cmd);
}
