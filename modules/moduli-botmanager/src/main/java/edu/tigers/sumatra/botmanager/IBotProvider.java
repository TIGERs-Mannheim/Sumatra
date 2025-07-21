/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.BotID;

import java.util.Map;
import java.util.Optional;


@SuppressWarnings("java:S1452") // Use generic wildcards to allow overriding bot type in specialized bot managers
public interface IBotProvider
{
	Optional<? extends ABot> getBot(BotID botID);


	Map<BotID, ? extends ABot> getBots();
}
