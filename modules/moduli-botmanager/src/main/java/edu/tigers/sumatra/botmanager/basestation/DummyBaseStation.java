package edu.tigers.sumatra.botmanager.basestation;

import edu.tigers.sumatra.botmanager.botskills.data.MatchCommand;
import edu.tigers.sumatra.ids.BotID;


public class DummyBaseStation extends ABaseStation
{
	@Override
	public void acceptMatchCommand(final BotID botId, final MatchCommand matchCommand)
	{
		// empty
	}
	
	
	@Override
	public void connect()
	{
		// empty
	}
	
	
	@Override
	public void disconnect()
	{
		// empty
	}
}
