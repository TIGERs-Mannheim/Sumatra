/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ids.BotID;


/**
 * Initialize play configuration with default strategy
 */
public class InitFeature extends AOffensiveStrategyFeature
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final OffensiveStrategy strategy)
	{
		for (BotID key : getWFrame().tigerBotsAvailable.keySet())
		{
			strategy.putPlayConfiguration(key, EOffensiveStrategy.KICK);
		}
	}
}
