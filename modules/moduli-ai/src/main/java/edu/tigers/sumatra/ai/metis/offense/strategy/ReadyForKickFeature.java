/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * checks if offensive is ready for kick
 */
public class ReadyForKickFeature extends AOffensiveStrategyFeature
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final OffensiveStrategy strategy)
	{
		ITrackedBot primaryBot = getWFrame().getBot(strategy.getAttackerBot().orElse(BotID.noBot()));
		if (primaryBot != null)
		{
			strategy.setAttackerIsAllowedToKick(true);
		}
	}
}
