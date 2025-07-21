/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import edu.tigers.sumatra.ids.BotID;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;


/**
 * A offensive strategy holds information of the global strategy
 */
@Value
@AllArgsConstructor
public class OffensiveStrategy
{
	BotID attackerBot;
	Map<BotID, EOffensiveStrategy> currentOffensivePlayConfiguration;


	public OffensiveStrategy()
	{
		attackerBot = null;
		currentOffensivePlayConfiguration = Collections.emptyMap();
	}


	public Optional<BotID> getAttackerBot()
	{
		return Optional.ofNullable(attackerBot);
	}
}