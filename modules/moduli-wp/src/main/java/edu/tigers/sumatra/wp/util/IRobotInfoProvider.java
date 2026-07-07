package edu.tigers.sumatra.wp.util;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.ids.BotID;

import java.util.Set;


public interface IRobotInfoProvider
{
	Set<BotID> getConnectedBotIds();


	RobotInfo getRobotInfo(BotID botID);


	void setLastWFTimestamp(long lastWFTimestamp);
}
