/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import java.util.Map;
import java.util.Optional;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.BotID;


public interface IBotProvider
{
	Optional<ABot> getBot(BotID botID);
	
	
	Map<BotID, ABot> getBots();
}
