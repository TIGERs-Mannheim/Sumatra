package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.ids.BotID;


public interface ISimulatorObserver
{
	void onBotAdded(BotID botID);
	
	
	void onBotRemove(BotID botID);
}
