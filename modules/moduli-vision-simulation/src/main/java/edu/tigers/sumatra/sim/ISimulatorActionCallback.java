/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;

import java.util.Map;
import java.util.Set;


public interface ISimulatorActionCallback
{
	Map<BotID, SimBotAction> nextSimBotActions(Map<BotID, SimBotState> botStates, long timestamp);


	void updateConnectedBotList(Set<BotID> botSet);
}
