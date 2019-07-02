package edu.tigers.sumatra.sim;

import java.util.Map;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;


public interface ISimulatorActionCallback
{
	Map<BotID, SimBotAction> nextSimBotActions(Map<BotID, SimBotState> botStates, long timestamp);
	
	
	void updateConnectedBotList(Map<BotID, SimBotState> botStates);
}
