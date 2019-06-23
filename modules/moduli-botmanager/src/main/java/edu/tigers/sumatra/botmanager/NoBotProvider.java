/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.BotID;


public class NoBotProvider implements IBotProvider
{
	@Override
	public Optional<ABot> getBot(final BotID botID)
	{
		return Optional.empty();
	}
	
	
	@Override
	public Map<BotID, ABot> getBots()
	{
		return Collections.emptyMap();
	}
}
