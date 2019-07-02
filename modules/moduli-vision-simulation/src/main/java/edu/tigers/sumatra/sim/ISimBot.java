package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;


public interface ISimBot
{
	BotID getBotId();
	
	
	double getMass();
	
	
	double getCenter2DribblerDist();
	
	
	double getRadius();
	
	
	SimBotState getState();
	
	
	SimBotAction getAction();
}
