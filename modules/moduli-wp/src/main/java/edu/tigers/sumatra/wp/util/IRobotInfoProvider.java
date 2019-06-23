/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.Set;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.ids.BotID;


public interface IRobotInfoProvider
{
	Set<BotID> getConnectedBotIds();
	
	
	RobotInfo getRobotInfo(BotID botID);
	
	
	void setLastWFTimestamp(long lastWFTimestamp);
}
